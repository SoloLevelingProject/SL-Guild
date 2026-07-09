package com.banghoi.file.inventory;

import com.banghoi.BangHoi;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ManageMemberInventoryFile {
    private static File file;
    private static FileConfiguration fileConfiguration;
    private static final String fileName = "manage-member-inventory.yml";

    public static void setupFile() {
        createFileAndDir();
        saveDefault();
        File addMemberListFile = InventoryFileHelper.getFile(fileName);
        try {
            ConfigUpdater.update(BangHoi.plugin, InventoryFileHelper.getPath(fileName), addMemberListFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reload();
    }

    private static void createFileAndDir() {
        file = InventoryFileHelper.getFile(fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return fileConfiguration;
    }

    public static void saveDefault() {
        try {
            if (!file.exists()) {
                BangHoi.plugin.saveResource(InventoryFileHelper.getPath(fileName), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}
