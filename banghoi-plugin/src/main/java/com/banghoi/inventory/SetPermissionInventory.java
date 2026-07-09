package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.subject.SetPermission;
import com.banghoi.file.inventory.SetPermissionInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SetPermissionInventory extends PaginatedInventory {

    FileConfiguration fileConfiguration = SetPermissionInventoryFile.get();
    private List<Subject> subjects = new ArrayList<>();

    public SetPermissionInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return;
        }
        if (PluginDataManager.getPlayerDatabase(getOwner().getName()).getRank() != Rank.LEADER) {
            MessageUtil.sendMessage(getOwner(), Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(Rank.LEADER)));
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
            getOwner().closeInventory();
            return false;
        }

        if (PluginDataManager.getPlayerDatabase(getOwner().getName()).getRank() != Rank.LEADER) {
            MessageUtil.sendMessage(getOwner(), Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(Rank.LEADER)));
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);
        IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("prevPage")) {
            if (getPage() != 0) {
                setPage(getPage() - 1);
                open();
            }
        }

        if (itemCustomData.equals("nextPage")) {
            if (!((index + 1) >= subjects.size())) {
                setPage(getPage() + 1);
                open();
            } else {
                MessageUtil.sendMessage(getOwner(), Messages.LAST_PAGE);
            }
        }

        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new ClanSettingsInventory(getOwner()).open();
        if (itemCustomData.equals("reset")) {
            HashMap<Subject, Rank> newPermissionDefault = new HashMap<>();
            for (Subject subject : Subject.values())
                newPermissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));
            PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).setSubjectPermission(newPermissionDefault);
            open();
            return true;
        }

        try {
            if (Arrays.stream(Subject.values()).toList().contains(Subject.valueOf(itemCustomData))) {
                Subject clickedSubject = Subject.valueOf(itemCustomData);
                playClickSound(fileConfiguration, "subject");

                if (playerClanData.getSubjectPermission().get(clickedSubject) == Rank.LEADER) {
                    new SetPermission(Rank.LEADER, getOwner(), getOwner().getName(), clickedSubject, Rank.MANAGER).execute();
                } else if (playerClanData.getSubjectPermission().get(clickedSubject) == Rank.MANAGER) {
                    new SetPermission(Rank.LEADER, getOwner(), getOwner().getName(), clickedSubject, Rank.MEMBER).execute();
                } else
                    new SetPermission(Rank.LEADER, getOwner(), getOwner().getName(), clickedSubject, Rank.LEADER).execute();
                open();
            }
        } catch (Exception exception) {
            return false;
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
            addBasicButton(fileConfiguration, true);

            ItemStack restItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.reset.type").toUpperCase()),
                    fileConfiguration.getString("items.reset.value"),
                    fileConfiguration.getInt("items.reset.customModelData"),
                    fileConfiguration.getString("items.reset.name"),
                    fileConfiguration.getStringList("items.reset.lore"), false), "reset");
            int resetItemSlot = fileConfiguration.getInt("items.reset.slot");
            inventory.setItem(resetItemSlot, restItem);

            IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());
            if (playerClanData == null)
                return;

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

            subjects.clear();
            this.subjects.addAll(Arrays.asList(Subject.values()));

            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * getPage() + i;
                if (index >= subjects.size())
                    break;
                if (subjects.get(index) != null) {
                    Subject subject = subjects.get(index);

                    Rank subjectRank = playerClanData.getSubjectPermission().get(subject);

                    List<String> subjectItemLore = new ArrayList<>();
                    for (String lore : fileConfiguration.getStringList("items.subject.lore." + subjectRank)) {
                        lore = lore.replace("%description%", subject.getDescription());
                        subjectItemLore.add(lore);
                    }
                    ItemStack subjectItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                            ItemType.valueOf(fileConfiguration.getString("items.subject.type." + subjectRank).toUpperCase()),
                            fileConfiguration.getString("items.subject.value." + subjectRank),
                            fileConfiguration.getInt("items.subject.customModelData." + subjectRank),
                            fileConfiguration.getString("items.subject.name").replace("%name%", subject.getName()),
                            subjectItemLore, false), subject.toString());
                    inventory.setItem(transferSlot(i), subjectItem);
                }
            }
        });
    }

    public int transferSlot(int number) {
        int index = number % getSubjectTrack().length;
        return getSubjectTrack()[index];
    }

    public int[] getSubjectTrack() {
        List<Integer> subjectTrackList = fileConfiguration.getIntegerList("subject-track");
        return subjectTrackList.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int getMaxItemsPerPage() {
        return getSubjectTrack().length;
    }

}
