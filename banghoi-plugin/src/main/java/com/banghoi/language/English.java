package com.banghoi.language;

import com.banghoi.BangHoi;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class English {
    private static File file;
    private static FileConfiguration messageFile;

    public static void setup() {
        file = new File(BangHoi.plugin.getDataFolder() + "/languages/language_en.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        messageFile = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return messageFile;
    }

    public static void saveDefault() {
        try {
            if (!file.exists()) {
                BangHoi.plugin.saveResource("language_en.yml", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        messageFile = YamlConfiguration.loadConfiguration(file);
    }
}
