package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.ItemType;
import com.banghoi.file.inventory.AlliesMenuInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AlliesMenuInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = AlliesMenuInventoryFile.get();

    public AlliesMenuInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return;
        }
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

        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);
        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new ClanMenuInventory(getOwner()).open();
        if (itemCustomData.equals("addAlly"))
            new AddAllyListInventory(getOwner()).open();
        if (itemCustomData.equals("allyInvitation"))
            new AllyInvitationListInventory(getOwner()).open();
        if (itemCustomData.equals("allyList"))
            new AllyListInventory(getOwner(), PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getName(), false).open();
        //new MemberListInventory(getOwner(), PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName())).open();

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, true);

            ItemStack addAllyItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.addAlly.type").toUpperCase()),
                    fileConfiguration.getString("items.addAlly.value"),
                    fileConfiguration.getInt("items.addAlly.customModelData"),
                    fileConfiguration.getString("items.addAlly.name"),
                    fileConfiguration.getStringList("items.addAlly.lore"), false), "addAlly");
            int addAllyItemSlot = fileConfiguration.getInt("items.addAlly.slot");
            inventory.setItem(addAllyItemSlot, addAllyItem);

            List<String> allyInvitationLore = new ArrayList<>();
            int totalAllyInvitations = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getAllyInvitation().size();
            for (String lore : fileConfiguration.getStringList("items.allyInvitation.lore")) {
                lore = lore.replace("%totalAllyInvitations%", String.valueOf(totalAllyInvitations));
                allyInvitationLore.add(lore);
            }
            ItemStack allyInvitationItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.allyInvitation.type").toUpperCase()),
                    fileConfiguration.getString("items.allyInvitation.value"),
                    fileConfiguration.getInt("items.allyInvitation.customModelData"),
                    fileConfiguration.getString("items.allyInvitation.name"),
                    allyInvitationLore, totalAllyInvitations > 0), "allyInvitation");
            int allyInvitationItemSlot = fileConfiguration.getInt("items.allyInvitation.slot");
            inventory.setItem(allyInvitationItemSlot, allyInvitationItem);

            ItemStack allyListItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.allyList.type").toUpperCase()),
                    fileConfiguration.getString("items.allyList.value"),
                    fileConfiguration.getInt("items.allyList.customModelData"),
                    fileConfiguration.getString("items.allyList.name"),
                    fileConfiguration.getStringList("items.allyList.lore"), false), "allyList");
            int allyListItemSlot = fileConfiguration.getInt("items.allyList.slot");
            inventory.setItem(allyListItemSlot, allyListItem);
        });
    }

}
