package com.banghoi.util;

import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.storage.PluginDataManager;

import java.util.Set;
import java.util.TreeSet;

/**
 * Calculates current guild contribution from each current member's stored
 * contribution score.
 */
public class ScoreCalculator {

    public static long calculateScore(IClanData clanData) {
        long total = 0;
        Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (clanData.getOwner() != null) {
            members.add(clanData.getOwner());
        }
        if (clanData.getMembers() != null) {
            members.addAll(clanData.getMembers());
        }
        for (String member : members) {
            if (!PluginDataManager.isPlayerInCurrentClan(member, clanData.getName())) {
                continue;
            }
            total = safeAdd(total, getPlayerPoint(member));
        }
        return total;
    }

    public static long getPlayerPoint(String playerName) {
        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
        if (playerData == null || !PluginDataManager.isPlayerInCurrentClan(playerName)) {
            return 0;
        }
        return Math.max(0, playerData.getScoreCollected());
    }

    private static long safeAdd(long current, long value) {
        if (value > 0 && current > Long.MAX_VALUE - value) {
            return Long.MAX_VALUE;
        }
        return current + value;
    }
}
