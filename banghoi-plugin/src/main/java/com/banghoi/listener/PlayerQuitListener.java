package com.banghoi.listener;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.clan.ClanManager;
import com.banghoi.command.ClanAdminCommand;
import com.banghoi.command.ClanCommand;
import com.banghoi.storage.PluginDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class PlayerQuitListener implements Listener {

    public PlayerQuitListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Update last activated and save player data
        if (PluginDataManager.getPlayerDatabase(playerName) != null) {
            PluginDataManager.getPlayerDatabase(playerName).setLastActivated(new Date().getTime());
            PluginDataManager.savePlayerDatabaseToStorage(playerName);

            // Evict from in-memory cache if smart-loading is enabled
            if (Settings.DATABASE_SETTING_SMART_LOADING_ENABLED) {
                PluginDataManager.getPlayerDatabase().remove(playerName);
            }
        }

        // --- Clean static Player reference lists (prevent GC root leaks) ---

        // ClanManager lists
        ClanManager.playerUsingClanChat.remove(player);
        ClanManager.playerTogglingPvP.remove(player);
        ClanManager.playerUsingChatSpy.remove(player);
        ClanManager.beingInvitedPlayers.remove(playerName);

        // ChatListenerHandler lists
        ChatListenerHandler.createClan.remove(player);
        ChatListenerHandler.setCustomName.remove(player);
        ChatListenerHandler.setMessage.remove(player);

        // PlayerMovementListener
        PlayerMovementListener.spawnCountDownPlayers.remove(player);

        // Command confirmation lists
        ClanCommand.commandConfirmation.remove(player);
        ClanAdminCommand.commandConfirmation.remove(player);
        ClanAdminCommand.transferDataCommandNotifying.remove(player);

        // Sign search query cleanup
        SignChangeListener.removeSearchPlayerQuery(player);
    }

}
