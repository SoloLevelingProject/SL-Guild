package com.banghoi.listener;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.clan.ClanManager;
import com.banghoi.storage.PluginDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Date;

public class PlayerJoinListener implements Listener {

    public PlayerJoinListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
            Player player = event.getPlayer();
            PluginDataManager.loadPlayerDatabase(player.getName());
            PluginDataManager.getPlayerDatabase(player.getName()).setLastActivated(new Date().getTime());

            if (Settings.CLAN_SETTINGS_MESSAGES_SETTINGS_ON_JOIN_CLAN_BROADCAST_ENABLED)
                BangHoi.support.getFoliaLib().getScheduler().runLaterAsync(task2 -> ClanManager.sendClanBroadCast(player), 20 * Settings.CLAN_SETTINGS_MESSAGES_SETTINGS_ON_JOIN_CLAN_BROADCAST_DELAY);
        });
    }

}
