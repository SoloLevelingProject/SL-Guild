package com.banghoi.clan.subject;

import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class SetPermission extends SubjectManager {

    private final Subject subject;
    private final Rank subjectRank;

    public SetPermission(Rank rank, Player player, String playerName, Subject subject, Rank subjectRank) {
        super(rank, player, playerName, null, null);
        this.subject = subject;
        this.subjectRank = subjectRank;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        setRequiredRank(Rank.LEADER);

        String commandPermission = "banghoi.setpermission";
        if (!player.hasPermission(commandPermission)) {
            MessageUtil.sendMessage(player, Messages.PERMISSION_REQUIRED.replace("%permission%", commandPermission));
            return false;
        }

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();
        playerClanData.getSubjectPermission().put(subject, subjectRank);
        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);

        MessageUtil.sendMessage(player, Messages.CHANGE_PERMISSION.replace("%name%", subject.getName()).replace("%newRank%", ClanManager.getFormatRank(subjectRank)));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_CHANGE_PERMISSION.replace("%player%", playerName).replace("%name%", subject.getName()).replace("%newRank%", ClanManager.getFormatRank(subjectRank)).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));
        return true;
    }
}
