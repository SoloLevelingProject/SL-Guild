package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.file.inventory.ContributeInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.ScoreCalculator;
import com.banghoi.util.StringUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class ContributeInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = ContributeInventoryFile.get();

    public ContributeInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return;
        }
        super.open();
    }

    @Override
    public String getMenuName() {
        String title = fileConfiguration.getString("title");
        String playerClanName = PluginDataManager.getPlayerDatabase(getOwner().getName()).getClan();
        if (playerClanName != null)
            title = StringUtil.setClanNamePlaceholder(title, playerClanName);
        return BangHoi.nms.addColor(title);
    }

    @Override
    public int getSlots() {
        int rows = fileConfiguration.getInt("rows") * 9;
        if (rows < 27 || rows > 54)
            return 36;
        return rows;
    }

    @Override
    public boolean handleMenu(InventoryClickEvent event) {
        if (!super.handleMenu(event))
            return false;

        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new ClanMenuInventory(getOwner()).open();
        if (itemCustomData.equals("money-contribute"))
            handleMoneyContribution();
        return true;
    }

    private void handleMoneyContribution() {
        Player player = getOwner();
        String playerName = player.getName();

        if (!Settings.CONTRIBUTION_ENABLED || !Settings.CONTRIBUTION_MONEY_ENABLED) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_DISABLED);
            return;
        }

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(playerName);

        if (playerData == null || clanData == null || !PluginDataManager.isPlayerInCurrentClan(playerName)) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }

        // Check daily limit
        resetDailyCountIfNewDay(playerData);

        int maxTimes = Settings.CONTRIBUTION_MONEY_MAX_TIMES_PER_DAY;
        if (maxTimes > 0 && playerData.getMoneyContributeCountToday() >= maxTimes) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_MONEY_MAX_REACHED);
            return;
        }

        // Check if player has enough money via Vault
        Economy economy = BangHoi.support.getVault();
        if (economy == null) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_NOT_ENOUGH_MONEY
                    .replace("%amount%", String.valueOf(Settings.CONTRIBUTION_MONEY_AMOUNT)));
            return;
        }

        double balance = economy.getBalance(player);
        if (balance < Settings.CONTRIBUTION_MONEY_AMOUNT) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_NOT_ENOUGH_MONEY
                    .replace("%amount%", String.valueOf(Settings.CONTRIBUTION_MONEY_AMOUNT)));
            return;
        }

        EconomyResponse response = economy.withdrawPlayer(player, Settings.CONTRIBUTION_MONEY_AMOUNT);
        if (!response.transactionSuccess()) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_NOT_ENOUGH_MONEY
                    .replace("%amount%", String.valueOf(Settings.CONTRIBUTION_MONEY_AMOUNT)));
            return;
        }

        // Add contribution points
        long congHienReward = Math.max(0, Settings.CONTRIBUTION_MONEY_CONGHIEN_REWARD);
        long currentContribution = Math.max(0, playerData.getScoreCollected());
        long newContribution = currentContribution > Long.MAX_VALUE - congHienReward
                ? Long.MAX_VALUE
                : currentContribution + congHienReward;
        playerData.setScoreCollected(newContribution);
        playerData.setMoneyContributeCountToday(playerData.getMoneyContributeCountToday() + 1);
        playerData.setLastContributeTime(new Date().getTime());

        PluginDataManager.savePlayerDatabaseToStorage(playerName, playerData);
        ClanManager.invalidateCache();

        // Add to TurtleTop if enabled
        if (Settings.CONTRIBUTION_TURTLETOP_ENABLED
                && Bukkit.getPluginManager().isPluginEnabled("TurtleTop")) {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "tt add " + playerName + " " + Settings.SCORE_TURTLETOP_POINT + " " + congHienReward);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MessageUtil.sendMessage(player, Messages.CONTRIBUTION_SUCCESS_MONEY
                .replace("%amount%", String.valueOf(Settings.CONTRIBUTION_MONEY_AMOUNT))
                .replace("%conghien%", String.valueOf(congHienReward)));

        // Refresh GUI
        new ContributeInventory(player).open();
    }

    private String formatCooldownTime(IPlayerData playerData) {
        long lastTime = playerData.getLastContributeTime();
        if (lastTime <= 0)
            return "&aSẵn sàng";

        int maxTimes = Settings.CONTRIBUTION_MONEY_MAX_TIMES_PER_DAY;
        resetDailyCountIfNewDay(playerData);
        if (maxTimes < 0 || playerData.getMoneyContributeCountToday() < maxTimes) {
            return "&aSẵn sàng";
        }

        // Calculate remaining time until midnight
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        long secondsUntilReset = java.time.LocalDateTime.now()
                .until(tomorrow.atStartOfDay(), ChronoUnit.SECONDS);

        long hours = secondsUntilReset / 3600;
        long minutes = (secondsUntilReset % 3600) / 60;
        return "&c" + hours + "h " + minutes + "m";
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
            IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());
            if (clanData == null)
                return;

            IPlayerData playerData = PluginDataManager.getPlayerDatabase(getOwner().getName());

            addBasicButton(fileConfiguration, false);

            // Back button
            ItemStack backItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.back.type").toUpperCase()),
                    fileConfiguration.getString("items.back.value"),
                    fileConfiguration.getInt("items.back.customModelData"),
                    fileConfiguration.getString("items.back.name"),
                    fileConfiguration.getStringList("items.back.lore"), false), "back");
            int backSlot = fileConfiguration.getInt("items.back.slot");
            inventory.setItem(backSlot, backItem);

            // Money contribute button
            resetDailyCountIfNewDay(playerData);
            int moneyMaxTimes = Settings.CONTRIBUTION_MONEY_MAX_TIMES_PER_DAY;
            int moneyRemaining = moneyMaxTimes < 0 ? -1
                    : Math.max(0, moneyMaxTimes - playerData.getMoneyContributeCountToday());
            String moneyTimesStr = moneyRemaining < 0 ? "Không giới hạn" : String.valueOf(moneyRemaining);
            String moneyMaxStr = moneyMaxTimes < 0 ? "Không giới hạn" : String.valueOf(moneyMaxTimes);
            String cooldownStr = formatCooldownTime(playerData);

            List<String> moneyLore = fileConfiguration.getStringList("items.money-contribute.lore");
            moneyLore.replaceAll(string -> BangHoi.nms.addColor(string
                    .replace("%amount%", String.valueOf(Settings.CONTRIBUTION_MONEY_AMOUNT))
                    .replace("%conghien%", String.valueOf(Settings.CONTRIBUTION_MONEY_CONGHIEN_REWARD))
                    .replace("%timesRemaining%", moneyTimesStr)
                    .replace("%maxTimes%", moneyMaxStr)
                    .replace("%cooldown%", cooldownStr)));

            ItemStack moneyItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.money-contribute.type").toUpperCase()),
                    fileConfiguration.getString("items.money-contribute.value"),
                    fileConfiguration.getInt("items.money-contribute.customModelData"),
                    fileConfiguration.getString("items.money-contribute.name"),
                    moneyLore, false), "money-contribute");
            int moneySlot = fileConfiguration.getInt("items.money-contribute.slot");
            inventory.setItem(moneySlot, moneyItem);

            // Contribution info display
            List<String> congHienLore = fileConfiguration.getStringList("items.conghien-info.lore");
            congHienLore.replaceAll(string -> BangHoi.nms.addColor(string
                    .replace("%totalCongHien%", String.valueOf(ScoreCalculator.calculateScore(clanData)))
                    .replace("%yourCongHien%", String.valueOf(ScoreCalculator.getPlayerPoint(getOwner().getName())))));

            ItemStack congHienItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.conghien-info.type").toUpperCase()),
                    fileConfiguration.getString("items.conghien-info.value"),
                    fileConfiguration.getInt("items.conghien-info.customModelData"),
                    fileConfiguration.getString("items.conghien-info.name"),
                    congHienLore, false), "conghien-info");
            int congHienSlot = fileConfiguration.getInt("items.conghien-info.slot");
            inventory.setItem(congHienSlot, congHienItem);
        });
    }

    private static void resetDailyCountIfNewDay(IPlayerData playerData) {
        long lastTime = playerData.getLastContributeTime();
        if (lastTime <= 0) {
            return;
        }

        LocalDate lastDate = new Date(lastTime).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (!lastDate.equals(LocalDate.now())) {
            playerData.setMoneyContributeCountToday(0);
        }
    }
}
