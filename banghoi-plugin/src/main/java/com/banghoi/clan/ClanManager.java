package com.banghoi.clan;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.event.ClanMemberJoinEvent;
import com.banghoi.api.event.ClanMemberLeaveEvent;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ClanManager {

    // playerName, clanName
    public static HashMap<String, String> beingInvitedPlayers = new HashMap<>();
    // playerName, clanName
    public static HashMap<String, String> managersFromOldData = new HashMap<>();
    public static List<Player> playerUsingClanChat = new ArrayList<>();
    public static List<Player> playerTogglingPvP = new ArrayList<>();
    public static List<Player> playerUsingChatSpy = new ArrayList<>();
    public static boolean consoleUsingChatSpy = true;

    public static boolean isClanExisted(String clanName) {
        return PluginDataManager.getClanDatabase().containsKey(clanName);
    }

    public static boolean isPlayerInClan(String playerName) {
        if (!PluginDataManager.getPlayerDatabase().containsKey(playerName))
            return false;
        return PluginDataManager.getPlayerDatabase(playerName).getClan() != null;
    }

    public static boolean isPlayerInClan(Player player) {
        if (player == null)
            return false;
        if (!PluginDataManager.getPlayerDatabase().containsKey(player.getName()))
            return false;
        if (PluginDataManager.getPlayerDatabase(player.getName()).getClan() == null)
            return false;
        return PluginDataManager.getClanDatabaseByPlayerName(player.getName()) != null;
    }

    public static void alertClan(String clanName, String message) {
        if (!isClanExisted(clanName) || message == null)
            return;

        IClanData clanData = PluginDataManager.getClanDatabase(clanName);
        for (String playerInClan : clanData.getMembers()) {
            Player player = Bukkit.getPlayer(playerInClan);
            MessageUtil.sendMessage(player, StringUtil
                    .setClanNamePlaceholder(message.replace("%prefix%", Messages.CLAN_BROADCAST_PREFIX), clanName));
        }
    }

    public static void addPlayerToAClan(String playerName, String clanName, boolean forceToLeaveOldClan) {
        if (!PluginDataManager.getClanDatabase().containsKey(clanName)
                || !PluginDataManager.getPlayerDatabase().containsKey(playerName)) {
            return;
        }

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);

        if (playerData.getClan() != null && playerData.getClan().equalsIgnoreCase(clanName)) {
            return;
        }
        if (playerData.getClan() != null && !forceToLeaveOldClan) {
            return;
        }
        if (playerData.getClan() != null) {
            String oldClan = playerData.getClan();
            IClanData oldClanData = PluginDataManager.getClanDatabase(oldClan);
            if (oldClanData != null) {
                oldClanData.getMembers().removeIf(member -> member.equalsIgnoreCase(playerName));
                PluginDataManager.saveClanDatabaseToStorage(oldClan, oldClanData);
            }
            PluginDataManager.clearPlayerDatabase(playerName);
            Bukkit.getPluginManager().callEvent(new ClanMemberLeaveEvent(playerName, oldClan));
        }

        IClanData clanData = PluginDataManager.getClanDatabase(clanName);
        if (clanData.getMembers().stream().noneMatch(member -> member.equalsIgnoreCase(playerName))) {
            clanData.getMembers().add(playerName);
        }
        playerData.setClan(clanName);
        playerData.setRank(Rank.MEMBER);
        playerData.setJoinDate(new Date().getTime());
        PluginDataManager.resetPlayerContribution(playerName);
        PluginDataManager.savePlayerDatabaseToStorage(playerName, playerData);
        PluginDataManager.saveClanDatabaseToStorage(clanName);
        Bukkit.getPluginManager().callEvent(new ClanMemberJoinEvent(playerName, clanName));
        invalidateCache();
    }

    // === Cached ranking maps (5-second TTL to avoid HashMap storms from PlaceholderAPI) ===
    private static final long CACHE_TTL_MS = 5000;

    private static HashMap<String, Integer> cachedClansScore;
    private static long clansScoreCacheExpiry = 0;

    private static HashMap<String, Integer> cachedClansPlayerSize;
    private static long clansPlayerSizeCacheExpiry = 0;

    private static HashMap<String, Long> cachedClansCreatedDate;
    private static long clansCreatedDateCacheExpiry = 0;

    /**
     * Invalidate all cached ranking maps. Call on disable/reload.
     */
    public static void invalidateCache() {
        cachedClansScore = null;
        cachedClansPlayerSize = null;
        cachedClansCreatedDate = null;
    }

    public static HashMap<String, Integer> getClansScoreHashMap() {
        long now = System.currentTimeMillis();
        if (cachedClansScore != null && now < clansScoreCacheExpiry)
            return cachedClansScore;

        HashMap<String, Integer> clansScore = new HashMap<>();
        if (PluginDataManager.getClanDatabase().isEmpty())
            return clansScore;

        for (String clanName : PluginDataManager.getClanDatabase().keySet()) {
            long score = com.banghoi.util.ScoreCalculator.calculateScore(PluginDataManager.getClanDatabase(clanName));
            clansScore.put(clanName, score > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) score);
        }

        cachedClansScore = clansScore;
        clansScoreCacheExpiry = now + CACHE_TTL_MS;
        return clansScore;
    }

    public static HashMap<String, Integer> getClansPlayerSize() {
        long now = System.currentTimeMillis();
        if (cachedClansPlayerSize != null && now < clansPlayerSizeCacheExpiry)
            return cachedClansPlayerSize;

        HashMap<String, Integer> clansPlayerSize = new HashMap<>();
        if (PluginDataManager.getClanDatabase().isEmpty())
            return clansPlayerSize;

        for (String clanName : PluginDataManager.getClanDatabase().keySet())
            clansPlayerSize.put(clanName, PluginDataManager.getClanDatabase(clanName).getMembers().size());

        cachedClansPlayerSize = clansPlayerSize;
        clansPlayerSizeCacheExpiry = now + CACHE_TTL_MS;
        return clansPlayerSize;
    }

    public static HashMap<String, Long> getClansCreatedDate() {
        long now = System.currentTimeMillis();
        if (cachedClansCreatedDate != null && now < clansCreatedDateCacheExpiry)
            return cachedClansCreatedDate;

        HashMap<String, Long> clansCreatedDate = new HashMap<>();
        if (PluginDataManager.getClanDatabase().isEmpty())
            return clansCreatedDate;

        for (String clanName : PluginDataManager.getClanDatabase().keySet())
            clansCreatedDate.put(clanName, PluginDataManager.getClanDatabase(clanName).getCreatedDate());

        cachedClansCreatedDate = clansCreatedDate;
        clansCreatedDateCacheExpiry = now + CACHE_TTL_MS;
        return clansCreatedDate;
    }

    public static List<String> getClansCustomName() {
        if (PluginDataManager.getClanDatabase().isEmpty())
            return null;

        List<String> clansCustomName = new ArrayList<>();
        for (String clanName : PluginDataManager.getClanDatabase().keySet()) {
            String clanCustomName = PluginDataManager.getClanDatabase(clanName).getCustomName();
            if (clanCustomName != null)
                clansCustomName.add(clanCustomName);
        }
        return clansCustomName;
    }

    public static boolean isPlayerRankSatisfied(String playerName, Rank requiredRank) {
        if (!isPlayerInClan(playerName))
            return false;

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);

        if (playerData.getRank() == null)
            return false;

        if (playerData.getRank() == Rank.LEADER)
            return true;

        if (playerData.getRank().equals(Rank.MANAGER) && requiredRank == Rank.MEMBER)
            return true;
        else
            return playerData.getRank() == requiredRank;
    }

    public static String getFormatClanName(IClanData clanData) {
        return clanData.getCustomName() != null ? BangHoi.nms.addColor(clanData.getCustomName()) : clanData.getName();
    }

    public static void sendClanBroadCast(Player player) {
        if (PluginDataManager.getClanDatabaseByPlayerName(player.getName()) == null)
            return;

        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());

        if (clanData.getMessage() == null) {
            return;
        }

        for (String clanMessageFormat : Messages.CLAN_MESSAGE) {
            clanMessageFormat = StringUtil.setClanNamePlaceholder(clanMessageFormat, clanData.getName());
            clanMessageFormat = clanMessageFormat.replace("%message%", clanData.getMessage());
            MessageUtil.sendMessage(player, clanMessageFormat);
        }
    }

    public static String getFormatClanMessage(IClanData clanData) {
        if (clanData.getMessage() == null)
            return BangHoi.nms.addColor(Messages.NO_MESSAGES);
        return BangHoi.nms.addColor(clanData.getMessage());
    }

    public static String getFormatClanCustomName(IClanData clanData) {
        if (clanData.getCustomName() == null)
            return BangHoi.nms.addColor(Messages.NO_CUSTOMNAME);
        return BangHoi.nms.addColor(clanData.getCustomName());
    }

    public static String getFormatRank(Rank rank) {
        if (rank == Rank.MANAGER)
            return Messages.RANK_DISPLAY_MANAGER;
        if (rank == Rank.LEADER)
            return Messages.RANK_DISPLAY_LEADER;
        return Messages.RANK_DISPLAY_MEMBER;
    }

    public static List<Player> getPlayerUsingClanChat() {
        return playerUsingClanChat;
    }

    public static List<Player> getPlayerTogglingPvP() {
        return playerTogglingPvP;
    }

    public static List<Player> getPlayerUsingChatSpy() {
        return playerUsingChatSpy;
    }

    public static boolean isConsoleUsingChatSpy() {
        return consoleUsingChatSpy;
    }
}
