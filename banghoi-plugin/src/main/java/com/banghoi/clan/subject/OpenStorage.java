package com.banghoi.clan.subject;

import com.banghoi.Settings;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.util.MessageUtil;
import org.bukkit.entity.Player;

public class OpenStorage extends SubjectManager {

    private int storageNumber;

    public OpenStorage(Rank rank, Player player, String playerName, int storageNumber) {
        super(rank, player, playerName, null, null);
        this.storageNumber = storageNumber;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.OPENSTORAGE));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        ClanManager.openClanStorage(player, getPlayerClanData().getName(), storageNumber, false);
        return true;
    }
}
