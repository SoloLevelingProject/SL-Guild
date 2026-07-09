package com.banghoi.listener;

import com.banghoi.BangHoi;
import com.banghoi.inventory.BangHoiInventoryBase;
import com.banghoi.inventory.BangHoiStorageInventoryBase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryClickListener implements Listener {


    public InventoryClickListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BangHoiInventoryBase) ((BangHoiInventoryBase) holder).handleMenu(event);
        if (holder instanceof BangHoiStorageInventoryBase) ((BangHoiStorageInventoryBase) holder).handleMenu(event);
    }

}
