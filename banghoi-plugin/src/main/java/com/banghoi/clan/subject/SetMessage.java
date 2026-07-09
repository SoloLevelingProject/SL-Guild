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

public class SetMessage extends SubjectManager {

    private final String clanMessage;

    public SetMessage(Rank rank, Player player, String playerName, String clanMessage) {
        super(rank, player, playerName, null, null);
        this.clanMessage = clanMessage;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.SETMESSAGE));

        String commandPermission = "banghoi.setmessage";
        if (!player.hasPermission(commandPermission)) {
            MessageUtil.sendMessage(player, Messages.PERMISSION_REQUIRED.replace("%permission%", commandPermission));
            return false;
        }

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();
        playerClanData.setMessage(clanMessage);
        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);

        MessageUtil.sendMessage(player, Messages.SET_MESSAGE_SUCCESS.replace("%newMessage%", clanMessage));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_SET_MESSAGE.replace("%player%", playerName).replace("%newMessage%", clanMessage).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));

        return true;
    }
}
