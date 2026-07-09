package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.file.inventory.AllyListInventoryFile;
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

public class AllyListInventory extends PaginatedInventory {

    FileConfiguration fileConfiguration = AllyListInventoryFile.get();
    private List<String> allies = new ArrayList<>();
    private String clanName;
    private boolean fromViewClan;

    public AllyListInventory(Player owner, String clanName, boolean fromViewClan) {
        super(owner);
        this.clanName = clanName;
        this.fromViewClan = fromViewClan;
    }

    @Override
    public void open() {
        if (PluginDataManager.getClanDatabase(clanName) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.CLAN_DOES_NOT_EXIST.replace("%clan%", clanName));
            return;
        }
        super.open();
    }

    @Override
    public String getMenuName() {
        String title = fileConfiguration.getString("title");
        title = title.replace("%search%", getSearch() != null ? fileConfiguration.getString("title-placeholders.search").replace("%search%", getSearch()) : "");
        title = title.replace("%totalMembers%", String.valueOf(PluginDataManager.getClanDatabase().size()));
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

        if (PluginDataManager.getClanDatabase(clanName) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.CLAN_DOES_NOT_EXIST.replace("%clan%", clanName));
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("prevPage")) {
            if (getPage() != 0) {
                setPage(getPage() - 1);
                open();
            }
        }
        if (itemCustomData.equals("nextPage")) {
            if (!((index + 1) >= allies.size())) {
                setPage(getPage() + 1);
                open();
            } else {
                MessageUtil.sendMessage(getOwner(), Messages.LAST_PAGE);
            }
        }
        if (itemCustomData.equals("close"))
            getOwner().closeInventory();

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(getOwner().getName());

        if (itemCustomData.equals("back")) {
            if (fromViewClan) {
                new ViewClanInformationInventory(getOwner(), clanName).open();
                return true;
            }
            if (playerData.getClan() != null) {
                if (playerData.getClan().equals(clanName)) {
                    new AlliesMenuInventory(getOwner()).open();
                    return true;
                }
            }
            new ViewClanInformationInventory(getOwner(), clanName).open();
        }

        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null)
            return false;

        if (!PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getName().equals(clanName))
            return false;

        if (itemCustomData.contains("ally=")) {
            playClickSound(fileConfiguration, "clan");
            if (playerData.getClan() != null) {
                if (playerData.getClan().equals(clanName)) {
                    itemCustomData = itemCustomData.replace("ally=", "");
                    new ManageAllyInventory(getOwner(), itemCustomData).open();
                    return true;
                }
            }
            MessageUtil.sendMessage(getOwner(), Messages.TARGET_CLAN_ALLY_MEMBERSHIP_ERROR.replace("%clan%", itemCustomData.replace("ally=", "")));
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addPaginatedMenuItems(fileConfiguration, true);

            if (PluginDataManager.getClanDatabase().isEmpty())
                return;

            allies.clear();
            if (!PluginDataManager.getClanDatabase(clanName).getAllies().isEmpty()) {
                allies.addAll(PluginDataManager.getClanDatabase(clanName).getAllies());
            }

            if (getSearch() != null) {
                List<String> newAllies = new ArrayList<>();
                for (String ally : allies) {
                    if (ally.toLowerCase().contains(getSearch().toLowerCase())) {
                        newAllies.add(ally);
                    }
                }
                allies.clear();
                allies.addAll(newAllies);
            }

            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * getPage() + i;
                if (index >= allies.size())
                    break;
                if (allies.get(index) != null) {
                    String clanName = allies.get(index);
                    IClanData clanData = PluginDataManager.getClanDatabase(clanName);
                    ItemStack clanItem = ItemUtil.getItem(
                            clanData.getIconType(),
                            clanData.getIconValue(),
                            0,
                            fileConfiguration.getString("items.clan.name"),
                            fileConfiguration.getStringList("items.clan.lore"), false);
                    ItemStack itemStack = BangHoi.nms.addCustomData(ItemUtil.getClanItemStack(clanItem, clanData), "ally=" + clanName);
                    inventory.addItem(itemStack);
                }
            }
        });
    }
}
