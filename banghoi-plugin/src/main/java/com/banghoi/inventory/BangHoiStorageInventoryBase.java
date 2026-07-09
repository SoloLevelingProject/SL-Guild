package com.banghoi.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BangHoiStorageInventoryBase implements InventoryHolder {

    protected Inventory inventory;
    protected int storageNumber;
    protected String clanName;

    public BangHoiStorageInventoryBase(int storageNumber) {
        this.storageNumber = storageNumber;
    }

    @Override
    public Inventory getInventory() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        setMenuItems();
        return inventory;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public int getStorageNumber() {
        return storageNumber;
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent event);

    public abstract void setMenuItems();

}
