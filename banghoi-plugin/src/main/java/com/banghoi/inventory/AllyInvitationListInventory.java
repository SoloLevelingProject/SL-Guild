package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.file.inventory.AllyInvitationInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AllyInvitationListInventory extends PaginatedInventory {

    FileConfiguration fileConfiguration = AllyInvitationInventoryFile.get();
    private List<String> clans = new ArrayList<>();

    public AllyInvitationListInventory(Player owner) {
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
        title = title.replace("%search%", getSearch() != null ? fileConfiguration.getString("title-placeholders.search").replace("%search%", getSearch()) : "");
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

        if (itemCustomData.equals("prevPage")) {
            if (getPage() != 0) {
                setPage(getPage() - 1);
                open();
            }
        }
        if (itemCustomData.equals("nextPage")) {
            if (!((index + 1) >= clans.size())) {
                setPage(getPage() + 1);
                open();
            } else {
                MessageUtil.sendMessage(getOwner(), Messages.LAST_PAGE);
            }
        }
        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new AlliesMenuInventory(getOwner()).open();
        if (itemCustomData.contains("manage=")) {
            IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());
            if (ClanManager.isPlayerRankSatisfied(getOwner().getName(), playerClanData.getSubjectPermission().get(Subject.MANAGEALLY))) {
                itemCustomData = itemCustomData.replace("manage=", "");
                new AllyInvitationConfirmInventory(getOwner(), playerClanData.getName(), itemCustomData).open();
            }
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addPaginatedMenuItems(fileConfiguration, true);

            if (PluginDataManager.getClanDatabase().isEmpty())
                return;

            clans.clear();
            IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

            if (!playerClanData.getAllyInvitation().isEmpty())
                clans.addAll(playerClanData.getAllyInvitation());

            if (getSearch() != null) {
                List<String> newClans = new ArrayList<>();
                for (String clan : clans) {
                    if (clan.toLowerCase().contains(getSearch().toLowerCase())) {
                        newClans.add(clan);
                    }
                }
                clans.clear();
                clans.addAll(newClans);
            }

            Rank requiredRank = playerClanData.getSubjectPermission().get(Subject.MANAGEALLY);
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * getPage() + i;
                if (index >= clans.size())
                    break;
                if (clans.get(index) != null) {
                    String clanName = clans.get(index);
                    IClanData clanData = PluginDataManager.getClanDatabase(clanName);
                    ArrayList<String> clanItemLore = new ArrayList<>();
                    ItemStack clanItem = ItemUtil.getItem(
                            clanData.getIconType(),
                            clanData.getIconValue(),
                            0,
                            fileConfiguration.getString("items.clan.name"),
                            fileConfiguration.getStringList("items.clan.lore"), false);
                    ItemMeta clanItemItemMeta = clanItem.getItemMeta();
                    for (String lore : clanItemItemMeta.getLore()) {
                        lore = lore.replace("%checkPermission%", ClanManager.isPlayerRankSatisfied(getOwner().getName(), requiredRank) ? fileConfiguration.getString("items.clan.placeholders.checkPermission.true")
                                : fileConfiguration.getString("items.clan.placeholders.checkPermission.false").replace("%getRequiredRank%", ClanManager.getFormatRank(requiredRank)));
                        clanItemLore.add(lore);
                    }
                    clanItemItemMeta.setLore(clanItemLore);
                    clanItem.setItemMeta(clanItemItemMeta);
                    ItemStack itemStack = BangHoi.nms.addCustomData(ItemUtil.getClanItemStack(clanItem, clanData), "manage=" + clanName);
                    inventory.addItem(itemStack);
                }
            }
        });
    }

}
