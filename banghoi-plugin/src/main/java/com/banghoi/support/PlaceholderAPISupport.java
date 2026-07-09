package com.banghoi.support;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.HashMapUtil;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.ScoreCalculator;
import com.banghoi.util.StringUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPISupport extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return "SoloLevelingProject";
    }

    @Override
    public String getIdentifier() {
        return "guild";
    }

    @Override
    public String getVersion() {
        return BangHoi.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        if (s == null)
            return null;

        // top
        if (!PluginDataManager.getClanDatabase().isEmpty()) {
            if (s.startsWith("top")) {
                try {
                    int value = Integer.parseInt(s.replace("top_score_name_", "").replace("top_score_value_", ""));
                    value = value - 1;

                    if (value < 0 || PluginDataManager.getClanDatabase().size() <= value)
                        return Settings.SOFT_DEPEND_PLACEHOLDERAPI_NO_CLAN;

                    if (ClanManager.getClansScoreHashMap() == null || ClanManager.getClansScoreHashMap().isEmpty())
                        return Settings.SOFT_DEPEND_PLACEHOLDERAPI_NO_CLAN;

                    IClanData clanData = PluginDataManager.getClanDatabase(
                            HashMapUtil.sortFromGreatestToLowestI(ClanManager.getClansScoreHashMap()).get(value));

                    if (s.startsWith("top_score_name_"))
                        return BangHoi.nms.addColor(
                                StringUtil.setClanNamePlaceholder(Settings.SOFT_DEPEND_PLACEHOLDERAPI_TOP_SCORE_NAME_,
                                        clanData.getName()).replace("%top%", String.valueOf(value + 1)));
                    if (s.startsWith("top_score_value_"))
                        return BangHoi.nms.addColor(Settings.SOFT_DEPEND_PLACEHOLDERAPI_TOP_SCORE_VALUE_
                                .replace("%value%", String.valueOf(ScoreCalculator.calculateScore(clanData))));

                } catch (Exception exception) {
                    MessageUtil.throwErrorMessage(
                            "[PlaceholderAPI] Value typed for PlaceholderAPI is not available! (papi: " + s + ") ("
                                    + exception.getMessage() + ")");
                }

            }
        }

        if (!ClanManager.isPlayerInClan(player))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_NO_CLAN;

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(player.getName());
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());

        if (clanData == null)
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_NO_CLAN;

        if (s.equalsIgnoreCase("name"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_NAME.replace("%value%", clanData.getName());
        if (s.equalsIgnoreCase("customname"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_CUSTOMNAME.replace("%value%",
                    clanData.getCustomName() != null ? BangHoi.nms.addColor(clanData.getCustomName()) : "");
        if (s.equalsIgnoreCase("formatname"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_FORMATNAME.replace("%value%",
                    clanData.getCustomName() != null ? BangHoi.nms.addColor(clanData.getCustomName())
                            : clanData.getName());
        if (s.equalsIgnoreCase("owner"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_OWNER.replace("%value%", clanData.getOwner());
        if (s.equalsIgnoreCase("message"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_MESSAGE.replace("%value%",
                    clanData.getMessage() != null ? BangHoi.nms.addColor(clanData.getMessage()) : "");
        if (s.equalsIgnoreCase("score"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_SCORE.replace("%value%",
                    String.valueOf(ScoreCalculator.calculateScore(clanData)));
        if (s.equalsIgnoreCase("warning"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_WARNING.replace("%value%",
                    String.valueOf(clanData.getWarning()));
        if (s.equalsIgnoreCase("maxmembers"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_MAXMEMBERS.replace("%value%",
                    String.valueOf(clanData.getMaxMembers()));
        if (s.equalsIgnoreCase("createddate"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_CREATEDDATE.replace("%value%",
                    String.valueOf(clanData.getCreatedDate()));
        if (s.equalsIgnoreCase("format_createddate"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_FORMAT_CREATEDDATE.replace("%value%",
                    StringUtil.dateTimeToDateFormat(clanData.getCreatedDate()));
        if (s.equalsIgnoreCase("members"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_MEMBERS.replace("%value%",
                    String.valueOf(clanData.getMembers()));
        if (s.equalsIgnoreCase("allies"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_ALLIES.replace("%value%",
                    !clanData.getAllies().isEmpty() ? String.valueOf(clanData.getAllies()) : "");
        if (s.startsWith("subjectpermission_")) {
            String subject = s.replace("subjectpermission_", "");
            try {
                return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_SUBJECTPERMISSION_.replace("%value%",
                        String.valueOf(clanData.getSubjectPermission().get(Subject.valueOf(subject.toUpperCase()))));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (s.startsWith("format_subjectpermission_")) {
            String subject = s.replace("format_subjectpermission_", "");
            try {
                return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_FORMAT_SUBJECTPERMISSION_.replace("%value%",
                        BangHoi.nms.addColor(ClanManager.getFormatRank(
                                clanData.getSubjectPermission().get(Subject.valueOf(subject.toUpperCase())))));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (s.equalsIgnoreCase("discordchannelid"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_DISCORDCHANNELID.replace("%value%",
                    String.valueOf(clanData.getDiscordChannelID()));
        if (s.equalsIgnoreCase("discordjoinlink"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_CLAN_DISCORDJOINLINK.replace("%value%",
                    clanData.getDiscordJoinLink() != null ? clanData.getDiscordJoinLink() : "");

        // player placeholders
        if (s.equalsIgnoreCase("player_rank"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_RANK.replace("%value%",
                    String.valueOf(playerData.getRank()));
        if (s.equalsIgnoreCase("player_format_rank"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_FORMAT_RANK.replace("%value%",
                    BangHoi.nms.addColor(ClanManager.getFormatRank(playerData.getRank())));
        if (s.equalsIgnoreCase("player_joindate"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_JOINDATE.replace("%value%",
                    String.valueOf(playerData.getJoinDate()));
        if (s.equalsIgnoreCase("player_format_joindate"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_FORMAT_JOINDATE.replace("%value%",
                    StringUtil.dateTimeToDateFormat(playerData.getJoinDate()));
        if (s.equalsIgnoreCase("player_scorecollected"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_SCORECOLLECTED.replace("%value%",
                    String.valueOf(playerData.getScoreCollected()));
        if (s.equalsIgnoreCase("player_lastactivated"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_LASTACTIVATED.replace("%value%",
                    String.valueOf(playerData.getLastActivated()));
        if (s.equalsIgnoreCase("player_format_lastactivated"))
            return Settings.SOFT_DEPEND_PLACEHOLDERAPI_PLAYER_FORMAT_LASTACTIVATED.replace("%value%",
                    StringUtil.dateTimeToDateFormat(playerData.getLastActivated()));

        return null;
    }
}
