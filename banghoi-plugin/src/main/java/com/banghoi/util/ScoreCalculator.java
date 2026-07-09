package com.banghoi.util;

import com.banghoi.Settings;
import com.banghoi.api.storage.IClanData;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

/**
 * Calculates clan score by reading from TurtleTop.
 * All conghuan points (from contributions, ThanhChien, etc.) flow into
 * TurtleTop,
 * so we sum each clan member's TurtleTop point value for the configured point
 * name.
 */
public class ScoreCalculator {

    private static Method getApiInstanceMethod;
    private static Method getPlayerDataListMethod;
    private static Method getDataMethod;
    private static Method getPointMethod;
    private static boolean reflectionInitialized = false;
    private static boolean reflectionAvailable = false;

    /**
     * Calculate the clan score by summing all members' TurtleTop points.
     * Falls back to legacy clanData.getScore() if TurtleTop is not available.
     */
    public static long calculateScore(IClanData clanData) {
        if (!isTurtleTopAvailable()) {
            return clanData.getScore();
        }

        String pointName = Settings.SCORE_TURTLETOP_POINT;
        if (pointName == null || pointName.isEmpty()) {
            return clanData.getScore();
        }

        long total = 0;

        // Sum owner's points
        if (clanData.getOwner() != null) {
            total += getPlayerTurtleTopPoint(clanData.getOwner(), pointName);
        }

        // Sum all members' points
        if (clanData.getMembers() != null) {
            for (String member : clanData.getMembers()) {
                total += getPlayerTurtleTopPoint(member, pointName);
            }
        }

        return total;
    }

    /**
     * Get a single player's TurtleTop point value for the configured point name.
     */
    public static long getPlayerPoint(String playerName) {
        if (!isTurtleTopAvailable()) {
            return 0;
        }
        String pointName = Settings.SCORE_TURTLETOP_POINT;
        if (pointName == null || pointName.isEmpty()) {
            return 0;
        }
        return getPlayerTurtleTopPoint(playerName, pointName);
    }

    private static boolean isTurtleTopAvailable() {
        return Bukkit.getPluginManager().getPlugin("TurtleTop") != null && initReflection();
    }

    private static boolean initReflection() {
        if (reflectionInitialized) {
            return reflectionAvailable;
        }
        reflectionInitialized = true;
        try {
            // TurtleTopApi.getInstance()
            Class<?> apiClass = Class.forName("com.turtle.turtletop.core.TurtleTopApi");
            getApiInstanceMethod = apiClass.getMethod("getInstance");

            // api.getPlayerDataList()
            getPlayerDataListMethod = apiClass.getMethod("getPlayerDataList");

            // playerDataList.getData(String name)
            Class<?> playerDataListClass = Class.forName("com.turtle.turtletop.core.data.player.PlayerDataList");
            getDataMethod = playerDataListClass.getMethod("getData", String.class);

            // playerData.getPoint(String pointName)
            Class<?> playerDataClass = Class.forName("com.turtle.turtletop.core.data.player.PlayerData");
            getPointMethod = playerDataClass.getMethod("getPoint", String.class);

            reflectionAvailable = true;
        } catch (Exception e) {
            reflectionAvailable = false;
            MessageUtil.debug("ScoreCalculator", "TurtleTop reflection init failed: " + e.getMessage());
        }
        return reflectionAvailable;
    }

    private static long getPlayerTurtleTopPoint(String playerName, String pointName) {
        try {
            Object api = getApiInstanceMethod.invoke(null);
            if (api == null)
                return 0;

            Object playerDataList = getPlayerDataListMethod.invoke(api);
            if (playerDataList == null)
                return 0;

            Object playerData = getDataMethod.invoke(playerDataList, playerName);
            if (playerData == null)
                return 0;

            Object point = getPointMethod.invoke(playerData, pointName);
            if (point instanceof Number) {
                return ((Number) point).longValue();
            }
        } catch (Exception e) {
            MessageUtil.debug("ScoreCalculator",
                    "Failed to get TurtleTop point for " + playerName + ": " + e.getMessage());
        }
        return 0;
    }
}
