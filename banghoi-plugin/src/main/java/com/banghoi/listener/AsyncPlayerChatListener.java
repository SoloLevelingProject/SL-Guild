package com.banghoi.listener;

import com.banghoi.BangHoi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    public AsyncPlayerChatListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        if (ChatListenerHandler.handlePlayerChat(event.getPlayer(), event.getMessage()))
            event.setCancelled(true);
    }
}
