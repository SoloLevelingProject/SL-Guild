package com.banghoi.util;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.language.Messages;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void throwErrorMessage(String message) {
        Bukkit.getLogger().severe(BangHoi.nms.addColor(message));
        log("&4&l[GUILD ERROR] &c&lIf this affects players, please contact your server staff.");
    }

    public static void sendBroadCast(String message) {
        if (message.equals("")) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            sendMessage(p, message);
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(BangHoi.nms.addColor(message));
    }

    public static void debug(String prefix, String message) {
        if (!Settings.DEBUG_ENABLED) return;
        Bukkit.getConsoleSender().sendMessage(BangHoi.nms.addColor(Settings.DEBUG_PREFIX + prefix.toUpperCase() + " >>> " + message));
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (message.equals("")) return;
        message = message.replace("%prefix%", Messages.PREFIX);
        sender.sendMessage(BangHoi.nms.addColor(message));
    }

    public static void sendMessage(Player player, String message) {
        if (player == null | message.equals("")) return;

        message = message.replace("%prefix%", Messages.PREFIX);

        if (!BangHoi.support.isPlaceholderAPISupported()) {
            String finalMessage = message;
            BangHoi.support.getFoliaLib().getScheduler().runAtEntity(player, task -> player.sendMessage(BangHoi.nms.addColor(finalMessage)));
        } else {
            String finalMessage = message;
            BangHoi.support.getFoliaLib().getScheduler().runAtEntity(player, task -> player.sendMessage(BangHoi.nms.addColor(PlaceholderAPI.setPlaceholders(player, finalMessage))));
        }
    }

    // only use for testing plugin
    public static void devMessage(String message) {
        log("[DEV] " + message);
    }

    public static void devMessage(Player player, String message) {
        if (player != null) player.sendMessage(BangHoi.nms.addColor("[DEV] " + message));
    }

}
