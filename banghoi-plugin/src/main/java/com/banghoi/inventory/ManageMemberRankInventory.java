package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.subject.RemoveManager;
import com.banghoi.clan.subject.SetManager;
import com.banghoi.clan.subject.SetOwner;
import com.banghoi.file.inventory.ManageMemberRankInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManageMemberRankInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = ManageMemberRankInventoryFile.get();
    private String playerName;

    public ManageMemberRankInventory(Player owner, String playerName) {
        super(owner);
        this.playerName = playerName;
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
        String title = fileConfiguration.getString("title").replace("%player%", playerName);
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

        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null || PluginDataManager.getClanDatabaseByPlayerName(playerName) == null) {
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new ManageMemberInventory(getOwner(), playerName).open();
        if (itemCustomData.contains("setOwner=")) {
            playClickSound(fileConfiguration, "setOwner");
            itemCustomData = itemCustomData.replace("setOwner=", "");
            new SetOwner(Rank.LEADER, getOwner(), getOwner().getName(), Bukkit.getPlayer(itemCustomData), itemCustomData).execute();
            super.open();
        }
        if (itemCustomData.contains("setManager=")) {
            playClickSound(fileConfiguration, "setManager");
            itemCustomData = itemCustomData.replace("setManager=", "");
            new SetManager(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETMANAGER), getOwner(), getOwner().getName(), Bukkit.getPlayer(itemCustomData), itemCustomData).execute();
            super.open();
        }
        if (itemCustomData.contains("removeManager=")) {
            playClickSound(fileConfiguration, "setManager");
            itemCustomData = itemCustomData.replace("removeManager=", "");
            new RemoveManager(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.REMOVEMANAGER), getOwner(), getOwner().getName(), Bukkit.getPlayer(itemCustomData), itemCustomData).execute();
            super.open();
        }

        return false;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, true);

            List<String> setOwnerItemLore = new ArrayList<>();
            for (String lore : fileConfiguration.getStringList("items.setOwner.lore")) {
                lore = lore.replace("%player%", playerName);
                lore = lore.replace("%checkPermission%", ClanManager.isPlayerRankSatisfied(getOwner().getName(), Rank.LEADER) ? fileConfiguration.getString("items.setOwner.placeholders.checkPermission.true")
                        : fileConfiguration.getString("items.setOwner.placeholders.checkPermission.false").replace("%getRequiredRank%", ClanManager.getFormatRank(Rank.LEADER)));
                setOwnerItemLore.add(lore);
            }
            ItemStack setOwnerItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.setOwner.type").toUpperCase()),
                    fileConfiguration.getString("items.setOwner.value"),
                    fileConfiguration.getInt("items.setOwner.customModelData"),
                    fileConfiguration.getString("items.setOwner.name"),
                    setOwnerItemLore, false), "setOwner=" + playerName);
            int setOwnerItemSlot = fileConfiguration.getInt("items.setOwner.slot");
            inventory.setItem(setOwnerItemSlot, setOwnerItem);

            IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(playerName);
            boolean isPlayerAManager = (PluginDataManager.getPlayerDatabase(playerName).getRank() == Rank.MANAGER);
            String getPlayerRankPath = isPlayerAManager ? "isAManager" : "isAMember";
            HashMap<Subject, Rank> clanSubjectPermission = playerClanData.getSubjectPermission();
            List<String> setManagerItemLore = new ArrayList<>();
            for (String lore : fileConfiguration.getStringList("items.setManager." + getPlayerRankPath + ".lore")) {
                lore = lore.replace("%player%", playerName);
                lore = lore.replace("%checkPermission%", ClanManager.isPlayerRankSatisfied(getOwner().getName(), (isPlayerAManager) ? clanSubjectPermission.get(Subject.REMOVEMANAGER) : playerClanData.getSubjectPermission().get(Subject.SETMANAGER)) ? fileConfiguration.getString("items.setManager." + getPlayerRankPath + ".placeholders.checkPermission.true")
                        : fileConfiguration.getString("items.setManager." + getPlayerRankPath + ".placeholders.checkPermission.false").replace("%getRequiredRank%", ClanManager.getFormatRank(clanSubjectPermission.get(isPlayerAManager ? Subject.REMOVEMANAGER : Subject.SETMANAGER))));
                setManagerItemLore.add(lore);
            }
            ItemStack setManagerItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.setManager." + getPlayerRankPath + ".type").toUpperCase()),
                    fileConfiguration.getString("items.setManager." + getPlayerRankPath + ".value"),
                    fileConfiguration.getInt("items.setManager." + getPlayerRankPath + ".customModelData"),
                    fileConfiguration.getString("items.setManager." + getPlayerRankPath + ".name"),
                    setManagerItemLore, false), (isPlayerAManager ? "removeManager=" : "setManager=") + playerName);
            int setManagerItemSlot = fileConfiguration.getInt("items.setManager.slot");
            inventory.setItem(setManagerItemSlot, setManagerItem);

        });
    }

}
