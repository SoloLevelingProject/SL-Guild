package com.banghoi.clan.subject;

import com.banghoi.Settings;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.event.ClanMemberLeaveEvent;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Kick extends SubjectManager {

    public Kick(Rank rank, Player player, String playerName, Player target, String targetName) {
        super(rank, player, playerName, target, targetName);
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.KICK));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        if (playerName.equals(targetName)) {
            MessageUtil.sendMessage(player, Messages.SELF_KICK_ERROR);
            return false;
        }

        if (!isTargetInClan()) {
            MessageUtil.sendMessage(player, Messages.TARGET_MUST_BE_IN_CLAN.replace("%player%", targetName));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();

        if (!isTargetAndPlayerInTheSameClan()) {
            MessageUtil.sendMessage(player, Messages.TARGET_CLAN_MEMBERSHIP_ERROR.replace("%player%", targetName));
            return false;
        }

        if (PluginDataManager.getPlayerDatabase(targetName).getRank() == Rank.LEADER) {
            MessageUtil.sendMessage(player, Messages.KICK_LEADER_ERROR);
            return false;
        }

        PluginDataManager.clearPlayerDatabase(targetName);
        playerClanData.getMembers().removeIf(memberName -> memberName.equalsIgnoreCase(targetName));
        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);
        Bukkit.getPluginManager().callEvent(new ClanMemberLeaveEvent(targetName, playerClanData.getName()));

        MessageUtil.sendMessage(player, Messages.TARGET_REMOVAL_SUCCESS.replace("%player%", targetName));
        if (target != null)
            MessageUtil.sendMessage(target, Messages.KICKED_FROM_CLAN.replace("%clan%", playerClanData.getName()).replace("%player%", playerName));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_PLAYER_REMOVED_FROM_CLAN.replace("%player%", targetName).replace("%by%", playerName).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));

        return true;
    }
}
