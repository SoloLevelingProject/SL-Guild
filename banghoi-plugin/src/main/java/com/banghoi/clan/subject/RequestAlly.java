package com.banghoi.clan.subject;

import com.banghoi.Settings;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class RequestAlly extends SubjectManager {

    private String targetClanName;

    public RequestAlly(Rank rank, Player player, String playerName, String targetClanName) {
        super(rank, player, playerName, null, null);
        this.targetClanName = targetClanName;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.MANAGEALLY));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        if (!PluginDataManager.getClanDatabase().containsKey(targetClanName)) {
            MessageUtil.sendMessage(player, Messages.CLAN_DOES_NOT_EXIST.replace("%clan%", targetClanName));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();

        if (playerClanData.getName().equals(targetClanName)) {
            MessageUtil.sendMessage(player, Messages.CLAN_CANNOT_BE_THE_SAME);
            return false;
        }

        if (playerClanData.getAllies().contains(targetClanName)) {
            MessageUtil.sendMessage(player, Messages.ALREADY_AN_ALLY.replace("%clan%", targetClanName));
            return false;
        }

        IClanData targetClanData = PluginDataManager.getClanDatabase(targetClanName);

        if (targetClanData.getAllyInvitation().contains(playerClanData.getName())) {
            MessageUtil.sendMessage(player, Messages.ALREADY_SENT_ALLY_INVITE.replace("%clan%", targetClanName));
            return false;
        }

        targetClanData.getAllyInvitation().add(playerClanData.getName());
        PluginDataManager.saveClanDatabaseToStorage(targetClanName, targetClanData);

        MessageUtil.sendMessage(player, Messages.SEND_ALLY_INVITE_SUCCESS.replace("%clan%", targetClanName));
        ClanManager.alertClan(targetClanName, Messages.CLAN_BROADCAST_RECEIVE_ALLY_INVITE.replace("%clan%", playerClanData.getName()));

        return true;
    }
}
