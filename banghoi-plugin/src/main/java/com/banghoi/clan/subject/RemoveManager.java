package com.banghoi.clan.subject;

import com.banghoi.Settings;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class RemoveManager extends SubjectManager {

    public RemoveManager(Rank rank, Player player, String playerName, Player target, String targetName) {
        super(rank, player, playerName, target, targetName);
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.REMOVEMANAGER));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        if (playerName.equals(targetName)) {
            MessageUtil.sendMessage(player, Messages.SELF_TARGETED_ERROR);
            return false;
        }

        if (!isTargetInClan()) {
            MessageUtil.sendMessage(player, Messages.TARGET_MUST_BE_IN_CLAN.replace("%player%", targetName));
            return false;
        }

        IPlayerData targetData = PluginDataManager.getPlayerDatabase(targetName);
        IClanData playerClanData = getPlayerClanData();

        if (!isTargetAndPlayerInTheSameClan()) {
            MessageUtil.sendMessage(player, Messages.TARGET_CLAN_MEMBERSHIP_ERROR.replace("%player%", targetName));
            return false;
        }

        if (targetData.getRank() != Rank.MANAGER) {
            MessageUtil.sendMessage(player, Messages.NOT_A_MANAGER.replace("%player%", targetName));
            return false;
        }

        targetData.setRank(Rank.MEMBER);
        PluginDataManager.savePlayerDatabaseToStorage(targetName, targetData);
        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);

        MessageUtil.sendMessage(player, Messages.REMOVE_A_MANAGER_SUCCESS.replace("%clan%", getPlayerClanData().getName()).replace("%player%", targetName));
        MessageUtil.sendMessage(target, Messages.MANAGER_REMOVED.replace("%clan%", getPlayerClanData().getName()).replace("%player%", playerName));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_MANAGER_REMOVED.replace("%player%", playerName).replace("%target%", targetName).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));

        return true;
    }
}
