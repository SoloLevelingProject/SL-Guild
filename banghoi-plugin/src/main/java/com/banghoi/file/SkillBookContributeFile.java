package com.banghoi.file;

import com.banghoi.BangHoi;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SkillBookContributeFile {
    private static final String FILE_NAME = "configs/skillbook-contribute.yml";
    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void setupFile() {
        file = new File(BangHoi.plugin.getDataFolder(), FILE_NAME);
        file.getParentFile().mkdirs();
        if (!file.exists())
            BangHoi.plugin.saveResource(FILE_NAME, false);
        try {
            ConfigUpdater.update(BangHoi.plugin, FILE_NAME, file, "skillbooks");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        reload();
    }

    public static FileConfiguration get() {
        return fileConfiguration;
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}
