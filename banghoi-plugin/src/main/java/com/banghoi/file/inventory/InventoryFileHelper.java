package com.banghoi.file.inventory;

import com.banghoi.BangHoi;

import java.io.File;

final class InventoryFileHelper {
    private InventoryFileHelper() {
    }

    static File getFile(String fileName) {
        File file = new File(BangHoi.plugin.getDataFolder(), getPath(fileName));
        File parent = file.getParentFile();
        if (parent != null)
            parent.mkdirs();
        File oldFile = new File(BangHoi.plugin.getDataFolder(), "gui/" + fileName);
        if (!file.exists() && oldFile.exists()) {
            oldFile.renameTo(file);
        }
        return file;
    }

    static String getPath(String fileName) {
        return "gui/" + getGroup(fileName) + "/" + fileName;
    }

    private static String getGroup(String fileName) {
        return switch (fileName) {
            case "members-menu-inventory.yml", "add-member-list-inventory.yml", "member-list-inventory.yml",
                    "manage-member-inventory.yml", "manage-member-rank-inventory.yml" -> "members";
            case "allies-menu-inventory.yml", "add-ally-list-inventory.yml", "ally-invitation-list-inventory.yml",
                    "ally-invitation-confirm-inventory.yml", "ally-list-inventory.yml", "manage-ally-inventory.yml" -> "allies";
            case "clan-settings-inventory.yml", "set-icon-custom-head-list-inventory.yml",
                    "set-icon-material-list-inventory.yml", "set-icon-menu-inventory.yml",
                    "set-permission-inventory.yml" -> "settings";
            case "upgrade-menu-inventory.yml" -> "upgrade";
            case "disband-confirmation-inventory.yml", "leave-confirmation-inventory.yml" -> "confirm";
            default -> "main";
        };
    }
}
