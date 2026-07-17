package com.banghoi.file.inventory;

import com.banghoi.BangHoi;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SkillBookListInventoryFile {
    private static final String FILE_NAME = "skillbook-list-inventory.yml";
    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void setupFile() {
        file = InventoryFileHelper.getFile(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try {
            ConfigUpdater.update(BangHoi.plugin, InventoryFileHelper.getPath(FILE_NAME), file);
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
