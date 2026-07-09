package com.banghoi.clan.subject;

import com.banghoi.api.enums.Rank;
import com.banghoi.api.event.ClanMemberLeaveEvent;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Leave extends SubjectManager {

    public Leave(Player player, String playerName) {
        super(null, player, playerName, null, null);
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
        if (playerData.getRank() == Rank.LEADER) {
            MessageUtil.sendMessage(player, Messages.LEADER_CANNOT_LEAVE);
            return false;
        }

        IClanData playerClanData = getPlayerClanData();
        String clanName = playerClanData.getName();
        playerClanData.getMembers().remove(playerName);
        PluginDataManager.saveClanDatabaseToStorage(clanName, playerClanData);
        PluginDataManager.clearPlayerDatabase(playerName);
        Bukkit.getPluginManager().callEvent(new ClanMemberLeaveEvent(playerName, clanName));

        MessageUtil.sendMessage(player, Messages.LEAVE_CLAN_SUCCESS.replace("%clan%", clanName));
        ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_PLAYER_LEAVE_CLAN.replace("%player%", playerName));

        return true;
    }
}
