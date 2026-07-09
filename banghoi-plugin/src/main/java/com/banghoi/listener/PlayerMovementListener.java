package com.banghoi.listener;

import com.banghoi.BangHoi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerMovementListener implements Listener {

    public static List<Player> spawnCountDownPlayers = new ArrayList<>();

    public PlayerMovementListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        spawnCountDownPlayers.remove(event.getPlayer());
    }

}
