package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.subject.Spawn;
import com.banghoi.file.inventory.ClanMenuInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClanMenuInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = ClanMenuInventoryFile.get();

    public ClanMenuInventory(Player owner) {
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
        String playerClanName = PluginDataManager.getPlayerDatabase(getOwner().getName()).getClan();
        if (playerClanName != null)
            title = StringUtil.setClanNamePlaceholder(title, playerClanName);
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
        if (itemCustomData.equals("members"))
            new MembersMenuInventory(getOwner()).open();
        if (itemCustomData.equals("clanList"))
            new ClanListInventory(getOwner()).open();
        if (itemCustomData.equals("allies"))
            new AlliesMenuInventory(getOwner()).open();
        if (itemCustomData.equals("upgrade"))
            new UpgradeMenuInventory(getOwner()).open();
        if (itemCustomData.equals("settings"))
            new ClanSettingsInventory(getOwner()).open();
        if (itemCustomData.equals("spawn"))
            new Spawn(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SPAWN), getOwner(), getOwner().getName())
                    .execute();
        if (itemCustomData.equals("leave"))
            new LeaveConfirmationInventory(getOwner()).open();
        if (itemCustomData.equals("contribute")) {
            if (Settings.CONTRIBUTION_ENABLED)
                new ContributeInventory(getOwner()).open();
            else
                MessageUtil.sendMessage(getOwner(), Messages.FEATURE_DISABLED);
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, false);

            IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

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

            ItemStack upgradeItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.upgrade.type").toUpperCase()),
                    fileConfiguration.getString("items.upgrade.value"),
                    fileConfiguration.getInt("items.upgrade.customModelData"),
                    fileConfiguration.getString("items.upgrade.name"),
                    fileConfiguration.getStringList("items.upgrade.lore"), false), "upgrade");
            int upgradeItemSlot = fileConfiguration.getInt("items.upgrade.slot");
            inventory.setItem(upgradeItemSlot, upgradeItem);

            ItemStack settingsItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.settings.type").toUpperCase()),
                    fileConfiguration.getString("items.settings.value"),
                    fileConfiguration.getInt("items.settings.customModelData"),
                    fileConfiguration.getString("items.settings.name"),
                    fileConfiguration.getStringList("items.settings.lore"), false), "settings");
            int settingsItemSlot = fileConfiguration.getInt("items.settings.slot");
            inventory.setItem(settingsItemSlot, settingsItem);

            ItemStack leaveItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.leave.type").toUpperCase()),
                    fileConfiguration.getString("items.leave.value"),
                    fileConfiguration.getInt("items.leave.customModelData"),
                    fileConfiguration.getString("items.leave.name"),
                    fileConfiguration.getStringList("items.leave.lore"), false), "leave");
            int leaveItemSlot = fileConfiguration.getInt("items.leave.slot");
            inventory.setItem(leaveItemSlot, leaveItem);

            ItemStack clanInfoItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    clanData.getIconType(),
                    clanData.getIconValue(),
                    fileConfiguration.getInt("items.clanInfo.customModelData"),
                    fileConfiguration.getString("items.clanInfo.name"),
                    fileConfiguration.getStringList("items.clanInfo.lore"), false), "clanInfo");
            int clanInfoItemSlot = fileConfiguration.getInt("items.clanInfo.slot");
            inventory.setItem(clanInfoItemSlot, ItemUtil.getClanItemStack(clanInfoItem, clanData));

            List<String> spawnItemLore = new ArrayList<>();
            Location spawnPoint = clanData.getSpawnPoint();
            boolean validSpawnPoint = spawnPoint != null && spawnPoint.getWorld() != null;
            for (String lore : fileConfiguration.getStringList("items.spawn.lore."
                    + (validSpawnPoint ? "valid-spawn-point" : "invalid-spawn-point"))) {
                if (validSpawnPoint) {
                    lore = lore.replace("%x%", String.valueOf((int) spawnPoint.getX()));
                    lore = lore.replace("%y%", String.valueOf((int) spawnPoint.getY()));
                    lore = lore.replace("%z%", String.valueOf((int) spawnPoint.getZ()));
                    lore = lore.replace("%worldName%", spawnPoint.getWorld().getName());
                }
                spawnItemLore.add(lore);
            }
            ItemStack spawnItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.spawn.type").toUpperCase()),
                    fileConfiguration.getString("items.spawn.value"),
                    fileConfiguration.getInt("items.spawn.customModelData"),
                    fileConfiguration.getString("items.spawn.name"),
                    spawnItemLore, false), "spawn");
            int spawnItemSlot = fileConfiguration.getInt("items.spawn.slot");
            inventory.setItem(spawnItemSlot, spawnItem);

            // Contribute button
            if (Settings.CONTRIBUTION_ENABLED) {
                ItemStack contributeItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                        ItemType.valueOf(fileConfiguration.getString("items.contribute.type").toUpperCase()),
                        fileConfiguration.getString("items.contribute.value"),
                        fileConfiguration.getInt("items.contribute.customModelData"),
                        fileConfiguration.getString("items.contribute.name"),
                        fileConfiguration.getStringList("items.contribute.lore"), false), "contribute");
                int contributeItemSlot = fileConfiguration.getInt("items.contribute.slot");
                inventory.setItem(contributeItemSlot, contributeItem);
            }
        });
    }
}
