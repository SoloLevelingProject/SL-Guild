package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.subject.OpenStorage;
import com.banghoi.file.inventory.StorageListInventoryFile;
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

public class StorageListInventory extends PaginatedInventory {

    FileConfiguration fileConfiguration = StorageListInventoryFile.get();
    private List<Integer> storages = new ArrayList<>();

    public StorageListInventory(Player owner) {
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
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("prevPage")) {
            if (getPage() != 0) {
                setPage(getPage() - 1);
                open();
            }
        }

        if (itemCustomData.equals("nextPage")) {
            if (!((index + 1) >= storages.size())) {
                setPage(getPage() + 1);
                open();
            } else {
                MessageUtil.sendMessage(getOwner(), Messages.LAST_PAGE);
            }
        }
        if (itemCustomData.equals("close"))
            getOwner().closeInventory();

        if (itemCustomData.equals("back"))
            new ClanMenuInventory(getOwner()).open();

        if (!PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getName().equals(clanData.getName()))
            return false;

        if (itemCustomData.startsWith("storage=")) {
            int storageNumber = Integer.parseInt(itemCustomData.replace("storage=" , ""));
            if (clanData.getMaxStorage() < storageNumber) {
                if (event.getClick().isRightClick()) {
                    new UpgradeMenuInventory(getOwner()).open();
                    return true;
                }
            }
            new OpenStorage(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.OPENSTORAGE), getOwner(), getOwner().getName(), storageNumber).execute();
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
            addBasicButton(fileConfiguration, true);

            ItemStack prevItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.prevPage.type").toUpperCase()),
                    fileConfiguration.getString("items.prevPage.value"),
                    fileConfiguration.getInt("items.prevPage.customModelData"),
                    fileConfiguration.getString("items.prevPage.name"),
                    fileConfiguration.getStringList("items.prevPage.lore"), false), "prevPage");
            int prevPageItemSlot = fileConfiguration.getInt("items.prevPage.slot");

            ItemStack nextItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.nextPage.type").toUpperCase()),
                    fileConfiguration.getString("items.nextPage.value"),
                    fileConfiguration.getInt("items.nextPage.customModelData"),
                    fileConfiguration.getString("items.nextPage.name"),
                    fileConfiguration.getStringList("items.nextPage.lore"), false), "nextPage");
            int nextPageItemSlot = fileConfiguration.getInt("items.nextPage.slot");

            if (page > 0)
                inventory.setItem(prevPageItemSlot, getPageItemStack(prevItem));
            inventory.setItem(nextPageItemSlot, getPageItemStack(nextItem));

            IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

            // LAZY: count items from serialized data without inflating inventories
            int itemsStored = clanData.getTotalStorageItemCount();

            List<String> clanStorageInfoLore = fileConfiguration.getStringList("items.clanStorageInfo.lore");
            int finalItemsStored = itemsStored;
            clanStorageInfoLore.replaceAll(string -> BangHoi.nms.addColor(string
                    .replace("%clanMaxStorage%", String.valueOf(clanData.getMaxStorage()))
                    .replace("%serverMaxStorage%", String.valueOf(Settings.STORAGE_SETTINGS_MAX_INVENTORY))
                    .replace("%itemsStored%", String.valueOf(finalItemsStored))));

            ItemStack clanStorageInfoItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.clanStorageInfo.type").toUpperCase()),
                    fileConfiguration.getString("items.clanStorageInfo.value"),
                    fileConfiguration.getInt("items.clanStorageInfo.customModelData"),
                    fileConfiguration.getString("items.clanStorageInfo.name"),
                    clanStorageInfoLore, false), "clanStorageInfo");
            int clanStorageInfoItemSlot = fileConfiguration.getInt("items.clanStorageInfo.slot");
            inventory.setItem(clanStorageInfoItemSlot, ItemUtil.getClanItemStack(clanStorageInfoItem, clanData));

            storages.clear();
            for (int maxStorage = 1; maxStorage <= Settings.STORAGE_SETTINGS_MAX_INVENTORY; maxStorage++) {
                storages.add(maxStorage);
            }

            String storageItemPath = "items.storage.";

            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * getPage() + i;
                if (index >= storages.size())
                    break;
                if (storages.get(index) != null) {
                    int storageNumber = storages.get(index);

                    String storageType;
                    if (clanData.getMaxStorage() < storageNumber)
                        storageType = "locked.";
                    else {
                        storageType = "unlocked.";
                    }

                    List<String> storageItemLore = fileConfiguration.getStringList(storageItemPath + storageType + "lore");
                    Rank openStorageRequiredRank = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getSubjectPermission().get(Subject.OPENSTORAGE);
                    storageItemLore.replaceAll(string -> BangHoi.nms.addColor(string
                            .replace("%usedSlots%", String.valueOf(getUsedSlot(storageNumber, clanData)))
                            .replace( "%checkPermission%", ClanManager.isPlayerRankSatisfied(getOwner().getName(), openStorageRequiredRank) ?
                                    fileConfiguration.getString(storageItemPath + "placeholders.checkPermission.true") :
                                    fileConfiguration.getString(storageItemPath + "placeholders.checkPermission.false")
                                            .replace("%getRequiredRank%", ClanManager.getFormatRank(openStorageRequiredRank)))));
                    ItemStack storageItem = ItemUtil.getItem(
                            ItemType.valueOf(fileConfiguration.getString(storageItemPath + storageType + "type").toUpperCase()),
                            fileConfiguration.getString(storageItemPath + storageType + "value"),
                            fileConfiguration.getInt(storageItemPath + storageType + "customModelData"),
                            fileConfiguration.getString(storageItemPath + storageType + "name").replace("%storageNumber%", String.valueOf(storageNumber)),
                            storageItemLore, false);
                    ItemStack itemStack = BangHoi.nms.addCustomData(storageItem, "storage=" + storageNumber);
                    inventory.setItem(transferSlot(i), itemStack);
                }
            }
        });
    }

    public int transferSlot(int number) {
        int index = number % getStorageTrack().length;
        return getStorageTrack()[index];
    }

    public int[] getStorageTrack() {
        List<Integer> storageTrackList = fileConfiguration.getIntegerList("storage-track");
        return storageTrackList.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int getMaxItemsPerPage() {
        return getStorageTrack().length;
    }

    // LAZY: delegate to IClanData.getStorageItemCount() which reads from serialized data
    int getUsedSlot(int storageNumber, IClanData clanData) {
        return clanData.getStorageItemCount(storageNumber);
    }
}
