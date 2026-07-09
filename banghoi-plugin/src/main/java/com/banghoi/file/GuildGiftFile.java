package com.banghoi.file;

import com.banghoi.BangHoi;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GuildGiftFile {
    private static File file;
    private static File claimsFile;
    private static FileConfiguration fileConfiguration;
    private static FileConfiguration claimsConfiguration;

    public static void setup() {
        file = new File(BangHoi.plugin.getDataFolder() + "/configs/guild-gift.yml");
        file.getParentFile().mkdirs();
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        claimsFile = new File(BangHoi.plugin.getDataFolder() + "/guild-gift-claims.yml");
        if (!claimsFile.exists()) {
            try {
                claimsFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        claimsConfiguration = YamlConfiguration.loadConfiguration(claimsFile);
    }

    public static FileConfiguration get() {
        return fileConfiguration;
    }

    public static FileConfiguration getClaims() {
        return claimsConfiguration;
    }

    public static void saveDefault() {
        try {
            if (!file.exists())
                BangHoi.plugin.saveResource("configs/guild-gift.yml", false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void saveClaims() {
        try {
            claimsConfiguration.save(claimsFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        claimsConfiguration = YamlConfiguration.loadConfiguration(claimsFile);
    }
}
