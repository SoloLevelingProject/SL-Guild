package com.banghoi.listener;

import com.banghoi.BangHoi;
import com.banghoi.api.storage.IClanData;
import com.banghoi.inventory.BangHoiStorageInventoryBase;
import com.banghoi.storage.ClanData;
import com.banghoi.storage.PluginDataManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryCloseListener implements Listener {

    public InventoryCloseListener() {
        Bukkit.getPluginManager().registerEvents(this, BangHoi.plugin);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof BangHoiStorageInventoryBase inventoryBase) {
            String clanName = inventoryBase.getClanName();
            int storageNumber = inventoryBase.getStorageNumber();

            // Mark dirty and save
            IClanData clanData = PluginDataManager.getClanDatabase(clanName);
            if (clanData instanceof ClanData cd) {
                cd.markStorageDirty(storageNumber);
            }
            PluginDataManager.saveClanDatabaseToStorage(clanName);

            // Deferred eviction: evict the storage page after 60 seconds if no other player is viewing it.
            // This keeps hot storage in memory briefly for quick re-access, then frees it.
            if (clanData instanceof ClanData cd) {
                BangHoi.support.getFoliaLib().getScheduler().runLater(task -> {
                    // Check that no one is currently viewing this storage page
                    Inventory liveInv = cd.getStorageHashMap().get(storageNumber);
                    if (liveInv != null && liveInv.getViewers().isEmpty()) {
                        cd.evictStorage(storageNumber);
                    }
                }, 60 * 20L); // 60 seconds
            }
        }
    }
}
