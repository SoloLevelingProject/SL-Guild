package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.storage.IClanData;
import com.banghoi.file.inventory.ViewClanInventoryFile;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ViewClanInformationInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = ViewClanInventoryFile.get();
    private String clanName;

    public ViewClanInformationInventory(Player owner, String clanName) {
        super(owner);
        this.clanName = clanName;
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public String getMenuName() {
        String title = fileConfiguration.getString("title");
        title = StringUtil.setClanNamePlaceholder(title, clanName);
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
        if (itemCustomData.equals("back"))
            new ClanListInventory(getOwner()).open();
        if (itemCustomData.equals("members"))
            new MemberListInventory(getOwner(), clanName, true).open();
        if (itemCustomData.equals("allies"))
            new AllyListInventory(getOwner(), clanName, true).open();

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, true);

            IClanData clanData = PluginDataManager.getClanDatabase(clanName);

            ItemStack clanItem = ItemUtil.getClanItemStack(ItemUtil.getItem(
                    clanData.getIconType(),
                    clanData.getIconValue(),
                    0,
                    fileConfiguration.getString("items.clan.name"),
                    fileConfiguration.getStringList("items.clan.lore"), false), clanData);
            int clanItemSlot = fileConfiguration.getInt("items.clan.slot");
            inventory.setItem(clanItemSlot, clanItem);

            List<String> membersItemLore = new ArrayList<>();
            for (String lore : fileConfiguration.getStringList("items.members.lore")) {
                lore = lore.replace("%totalMembers%", String.valueOf(clanData.getMembers().size()));
                membersItemLore.add(lore);
            }
            ItemStack membersClanItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.members.type").toUpperCase()),
                    fileConfiguration.getString("items.members.value"),
                    fileConfiguration.getInt("items.members.customModelData"),
                    fileConfiguration.getString("items.members.name"),
                    membersItemLore, false), "members");
            int membersItemSlot = fileConfiguration.getInt("items.members.slot");
            inventory.setItem(membersItemSlot, membersClanItem);

            List<String> alliesItemLore = new ArrayList<>();
            for (String lore : fileConfiguration.getStringList("items.allies.lore")) {
                lore = lore.replace("%totalAllies%", String.valueOf(clanData.getAllies().size()));
                alliesItemLore.add(lore);
            }
            ItemStack alliesClanItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.allies.type").toUpperCase()),
                    fileConfiguration.getString("items.allies.value"),
                    fileConfiguration.getInt("items.allies.customModelData"),
                    fileConfiguration.getString("items.allies.name"),
                    alliesItemLore, false), "allies");
            int alliesItemSlot = fileConfiguration.getInt("items.allies.slot");
            inventory.setItem(alliesItemSlot, alliesClanItem);

        });
    }

}
