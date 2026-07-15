package com.banghoi.file.inventory;

import com.banghoi.BangHoi;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ContributeInventoryFile {
    private static File file;
    private static FileConfiguration fileConfiguration;
    private static final String fileName = "contribute-inventory.yml";

    public static void setupFile() {
        createFileAndDir();
        migrateLegacyNames();
        saveDefault();
        File contributeFile = InventoryFileHelper.getFile(fileName);
        try {
            ConfigUpdater.update(BangHoi.plugin, InventoryFileHelper.getPath(fileName), contributeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reload();
    }

    private static void migrateLegacyNames() {
        try {
            String content = Files.readString(file.toPath());
            String updated = content.replace("CONGHUAN", "CONGHIEN")
                    .replace("Conghuan", "CongHien")
                    .replace("conghuan", "conghien");
            if (!content.equals(updated)) {
                Files.writeString(file.toPath(), updated);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
