package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.DatabaseType;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;

import java.io.File;
import java.util.List;

public class PluginDataStorage {

    private static PluginStorage STORAGE;

    public static void init(DatabaseType databaseType) {
        if (databaseType == DatabaseType.YAML) {
            File clanDataFolder = new File(BangHoi.plugin.getDataFolder() + "/banghoiData/");
            if (!clanDataFolder.exists()) {
                clanDataFolder.mkdirs();
            }

            File playerDataFolder = new File(BangHoi.plugin.getDataFolder() + "/playerData/");
            if (!playerDataFolder.exists()) {
                playerDataFolder.mkdirs();
            }
            STORAGE = new PluginDataYAMLStorage();
            BangHoi.databaseType = DatabaseType.YAML;
        }

        if (databaseType == DatabaseType.H2) {
            STORAGE = new PluginDataH2Storage(Settings.DATABASE_SETTINGS_H2_FILE_NAME, Settings.DATABASE_SETTINGS_H2_TABLE_CLAN, Settings.DATABASE_SETTINGS_H2_TABLE_PLAYER);
            BangHoi.databaseType = DatabaseType.H2;
        }

        if (databaseType == DatabaseType.SQLITE) {
            STORAGE = new PluginDataSQLiteStorage(Settings.DATABASE_SETTINGS_SQLITE_FILE_NAME, Settings.DATABASE_SETTINGS_SQLITE_TABLE_CLAN, Settings.DATABASE_SETTINGS_SQLITE_TABLE_PLAYER);
            BangHoi.databaseType = DatabaseType.SQLITE;
        }
    }

    public static ClanData getClanData(String clanName) {
        return STORAGE.getClanData(clanName);
    }

    public static void saveClanData(String clanName, IClanData clanData) {
        STORAGE.saveClanData(clanName, clanData);
    }

    public static PlayerData getPlayerData(String playerName) {
        return STORAGE.getPlayerData(playerName);
    }

    public static void savePlayerData(String playerName, IPlayerData playerData) {
        STORAGE.savePlayerData(playerName, playerData);
    }

    public static boolean deleteClanData(String clanName) {
        return STORAGE.deleteClanData(clanName);
    }

    public static List<String> getAllClans() {
        return STORAGE.getAllClans();
    }

    public static List<String> getAllPlayers() {
        return STORAGE.getAllPlayers();
    }

    public static void disableStorage() {
        STORAGE.disableStorage();
    }
}
