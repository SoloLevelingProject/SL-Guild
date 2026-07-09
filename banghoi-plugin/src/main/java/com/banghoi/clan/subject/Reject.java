package com.banghoi.clan.subject;

import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class Reject extends SubjectManager {

    public Reject(Player player, String playerName) {
        super(null, player, playerName, null, null);
    }

    @Override
    public boolean execute() {
        if (!ClanManager.beingInvitedPlayers.containsKey(playerName)) {
            MessageUtil.sendMessage(player, Messages.INVITATION_REJECTION);
            return false;
        }

        String clanName = ClanManager.beingInvitedPlayers.get(playerName);
        ClanManager.beingInvitedPlayers.remove(playerName);

        if (isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.ALREADY_IN_CLAN);
            return false;
        }

        if (!PluginDataManager.getClanDatabase().containsKey(clanName)) {
            MessageUtil.sendMessage(player, Messages.CLAN_DOES_NOT_EXIST.replace("%clan%", clanName));
            return false;
        }

        MessageUtil.sendMessage(player, Messages.REJECTED_CLAN_INVITE.replace("%clan%", clanName));
        ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_PLAYER_REJECT_TO_JOIN.replace("%player%", playerName));

        return true;
    }
}
