package com.banghoi.file;

import com.banghoi.BangHoi;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GuildUpgradeFile {
    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void setup() {
        file = new File(BangHoi.plugin.getDataFolder() + "/configs/guild-upgrade.yml");
        File oldFile = new File(BangHoi.plugin.getDataFolder() + "/guild-upgrade.yml");
        if (!file.exists() && oldFile.exists()) {
            file.getParentFile().mkdirs();
            oldFile.renameTo(file);
        }
        file.getParentFile().mkdirs();
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return fileConfiguration;
    }

    public static void saveDefault() {
        try {
            if (!file.exists()) {
                BangHoi.plugin.saveResource("configs/guild-upgrade.yml", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}
