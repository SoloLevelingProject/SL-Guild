package com.banghoi.clan.subject;

import com.banghoi.api.enums.Rank;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class SetOwner extends SubjectManager {

    public SetOwner(Rank rank, Player player, String playerName, Player target, String targetName) {
        super(rank, player, playerName, target, targetName);
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        setRequiredRank(Rank.LEADER);

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

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
        IPlayerData targetData = PluginDataManager.getPlayerDatabase(targetName);
        IClanData playerClanData = getPlayerClanData();
        IClanData targetClanData = getTargetClanData();

        if (!playerClanData.getName().equals(targetClanData.getName())) {
            MessageUtil.sendMessage(player, Messages.TARGET_CLAN_MEMBERSHIP_ERROR.replace("%player%", targetName));
            return false;
        }

        targetData.setRank(Rank.LEADER);
        playerData.setRank(Rank.MEMBER);
        PluginDataManager.savePlayerDatabaseToStorage(playerName, playerData);
        PluginDataManager.savePlayerDatabaseToStorage(targetName, targetData);
        playerClanData.setOwner(targetName);
        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);

        MessageUtil.sendMessage(player, Messages.PROMOTE_TO_OWNER_SUCCESS.replace("%clan%", playerData.getClan()).replace("%player%", targetName));
        MessageUtil.sendMessage(target, Messages.PROMOTED_TO_OWNER.replace("%clan%", playerData.getClan()).replace("%player%", playerName));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_MEMBER_PROMOTED_TO_OWNER.replace("%player%", playerName).replace("%target%", targetName));

        return true;
    }
}
