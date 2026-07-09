package com.banghoi.clan;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.storage.IClanData;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GuildMaintenanceManager {

    public static void start() {
        if (!Settings.GUILD_MAINTENANCE_ENABLED)
            return;

        long warningTicks = 20L * 60L * Math.max(1, Settings.GUILD_MAINTENANCE_WARNING_INTERVAL_MINUTES);
        BangHoi.support.getFoliaLib().getScheduler().runLater(GuildMaintenanceManager::processDailyMaintenance, 20L * 5L);
        BangHoi.support.getFoliaLib().getScheduler().runTimer(GuildMaintenanceManager::processDailyMaintenance, 20L * 60L, 20L * 60L * 5L);
        BangHoi.support.getFoliaLib().getScheduler().runTimer(GuildMaintenanceManager::warnOnlineDebtors, warningTicks, warningTicks);
    }

    public static void processDailyMaintenance() {
        long today = LocalDate.now().toEpochDay();
        for (IClanData clanData : new ArrayList<>(PluginDataManager.getClanDatabase().values())) {
            if (clanData.getLastMaintenanceDay() == 0) {
                clanData.setLastMaintenanceDay(today);
                PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
                continue;
            }
            while (clanData.getLastMaintenanceDay() < today && PluginDataManager.getClanDatabase().containsKey(clanData.getName())) {
                chargeOneDay(clanData, clanData.getLastMaintenanceDay() + 1);
            }
        }
    }

    public static boolean payDebtFromFund(IClanData clanData, String playerName) {
        if (clanData.getMaintenanceDebt() <= 0)
            return false;
        long debt = clanData.getMaintenanceDebt();
        if (clanData.getGuildFund() < debt)
            return false;

        clanData.setGuildFund(clanData.getGuildFund() - debt);
        clanData.setMaintenanceDebt(0);
        clanData.setMaintenanceDebtDays(0);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
        PluginDataManager.addGuildFundTransaction(clanData.getName(), playerName, "PAY_DEBT", debt, clanData.getGuildFund());
        return true;
    }

    public static void sendDebtWarning(Player player) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null || clanData.getMaintenanceDebt() <= 0)
            return;
        MessageUtil.sendMessage(player, formatDebtMessage(Messages.GUILD_MAINTENANCE_DEBT_WARNING, clanData));
    }

    public static void sendDebtTitle(Player player) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null || clanData.getMaintenanceDebt() <= 0)
            return;

        String title = BangHoi.nms.addColor(formatDebtMessage(Messages.GUILD_MAINTENANCE_DEBT_TITLE, clanData));
        String subtitle = BangHoi.nms.addColor(formatDebtMessage(Messages.GUILD_MAINTENANCE_DEBT_SUBTITLE, clanData));
        BangHoi.support.getFoliaLib().getScheduler().runAtEntity(player, task -> player.sendTitle(title, subtitle, 10, 70, 20));
    }

    private static void chargeOneDay(IClanData clanData, long day) {
        long due = clanData.getMaintenanceDebt() + Settings.GUILD_MAINTENANCE_DAILY_FEE;
        clanData.setLastMaintenanceDay(day);

        if (clanData.getGuildFund() >= due) {
            clanData.setGuildFund(clanData.getGuildFund() - due);
            clanData.setMaintenanceDebt(0);
            clanData.setMaintenanceDebtDays(0);
            PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
            PluginDataManager.addGuildFundTransaction(clanData.getName(), "SYSTEM", "MAINTENANCE", due, clanData.getGuildFund());
            return;
        }

        long paid = clanData.getGuildFund();
        clanData.setGuildFund(0);
        clanData.setMaintenanceDebt(due - paid);
        clanData.setMaintenanceDebtDays(clanData.getMaintenanceDebtDays() + 1);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
        PluginDataManager.addGuildFundTransaction(clanData.getName(), "SYSTEM", "MAINTENANCE_DEBT", clanData.getMaintenanceDebt(), clanData.getGuildFund());

        if (clanData.getMaintenanceDebtDays() >= Settings.GUILD_MAINTENANCE_MAX_DEBT_DAYS) {
            String clanName = clanData.getName();
            List<String> members = new ArrayList<>(clanData.getMembers());
            PluginDataManager.deleteClanData(clanName);
            for (String memberName : members) {
                Player player = Bukkit.getPlayerExact(memberName);
                if (player != null)
                    MessageUtil.sendMessage(player, Messages.GUILD_MAINTENANCE_DISBANDED.replace("%clan%", clanName));
            }
        }
    }

    private static void warnOnlineDebtors() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendDebtWarning(player);
        }
    }

    private static String formatDebtMessage(String message, IClanData clanData) {
        int daysLeft = Math.max(0, Settings.GUILD_MAINTENANCE_MAX_DEBT_DAYS - clanData.getMaintenanceDebtDays());
        return message
                .replace("%debt%", String.valueOf(clanData.getMaintenanceDebt()))
                .replace("%days%", String.valueOf(clanData.getMaintenanceDebtDays()))
                .replace("%daysLeft%", String.valueOf(daysLeft))
                .replace("%maxDays%", String.valueOf(Settings.GUILD_MAINTENANCE_MAX_DEBT_DAYS))
                .replace("%fee%", String.valueOf(Settings.GUILD_MAINTENANCE_DAILY_FEE));
    }
}
