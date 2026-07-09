package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.file.inventory.NoClanInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.listener.ChatListenerHandler;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NoClanInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = NoClanInventoryFile.get();

    public NoClanInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public String getMenuName() {
        String title = fileConfiguration.getString("title");
        return BangHoi.nms.addColor(title);
    }

    @Override
    public int getSlots() {
        int rows = fileConfiguration.getInt("rows") * 9;
        if (rows < 27 || rows > 54)
            return 54;
        return rows;
    }

    @Override
    public boolean handleMenu(InventoryClickEvent event) {
        if (!super.handleMenu(event))
            return false;

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("createNewClan")) {
            getOwner().closeInventory();
            if (!ChatListenerHandler.createClan.contains(getOwner()))
                ChatListenerHandler.createClan.add(getOwner());
            MessageUtil.sendMessage(getOwner(), Messages.USING_CHAT_BOX_CREATE_CLAN.replace("%seconds%", String.valueOf(Settings.CHAT_SETTING_TIME_OUT)));
            MessageUtil.sendMessage(getOwner(), Messages.USING_CHAT_BOX_CANCEL_USING_CHAT_BOX.replace("%word%", Settings.CHAT_SETTING_STOP_USING_CHAT_WORD));
        }
        if (itemCustomData.equals("clanList"))
            new ClanListInventory(getOwner()).open();

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, false);

            ItemStack createNewClanItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.createNewClan.type").toUpperCase()),
                    fileConfiguration.getString("items.createNewClan.value"),
                    fileConfiguration.getInt("items.createNewClan.customModelData"),
                    fileConfiguration.getString("items.createNewClan.name"),
                    fileConfiguration.getStringList("items.createNewClan.lore"), false), "createNewClan");
            int createNewClanItemSlot = fileConfiguration.getInt("items.createNewClan.slot");
            inventory.setItem(createNewClanItemSlot, createNewClanItem);

            List<String> listClanItemLore = new ArrayList<>();
            for (String lore : fileConfiguration.getStringList("items.clanList.lore")) {
                lore = lore.replace("%totalClans%", String.valueOf(PluginDataManager.getClanDatabase().size()));
                listClanItemLore.add(lore);
            }
            ItemStack listClanItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.clanList.type").toUpperCase()),
                    fileConfiguration.getString("items.clanList.value"),
                    fileConfiguration.getInt("items.clanList.customModelData"),
                    fileConfiguration.getString("items.clanList.name"),
                    listClanItemLore, false), "clanList");
            int listClanItemSlot = fileConfiguration.getInt("items.clanList.slot");
            inventory.setItem(listClanItemSlot, listClanItem);
        });
    }

}
