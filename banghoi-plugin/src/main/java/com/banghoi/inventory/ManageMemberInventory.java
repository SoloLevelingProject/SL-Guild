package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.subject.Kick;
import com.banghoi.file.inventory.ManageMemberInventoryFile;
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
import java.util.List;

public class ManageMemberInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = ManageMemberInventoryFile.get();
    private String playerName;

    public ManageMemberInventory(Player owner, String playerName) {
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
            new MemberListInventory(getOwner(), PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getName(), false).open();
        if (itemCustomData.contains("manageMembersRank=")) {
            playClickSound(fileConfiguration, "manageMembersRank");
            itemCustomData = itemCustomData.replace("manageMembersRank=", "");
            new ManageMemberRankInventory(getOwner(), itemCustomData).open();
        }
        if (itemCustomData.contains("kick=")) {
            playClickSound(fileConfiguration, "kickMember");
            itemCustomData = itemCustomData.replace("kick=", "");
            if (new Kick(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.KICK), getOwner(), getOwner().getName(), Bukkit.getPlayer(itemCustomData), itemCustomData).execute())
                new MemberListInventory(getOwner(), PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()).getName(), false).open();
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addBasicButton(fileConfiguration, true);

            ItemStack memberItem = ItemUtil.getPlayerItemStack(ItemUtil.getItem(
                    ItemType.PLAYERHEAD,
                    playerName,
                    fileConfiguration.getInt("items.member.customModelData"),
                    fileConfiguration.getString("items.member.name"),
                    fileConfiguration.getStringList("items.member.lore"), false), playerName);
            int memberItemSlot = fileConfiguration.getInt("items.member.slot");
            inventory.setItem(memberItemSlot, memberItem);

            ItemStack manageMembersRankItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.manageMembersRank.type").toUpperCase()),
                    fileConfiguration.getString("items.manageMembersRank.value"),
                    fileConfiguration.getInt("items.manageMembersRank.customModelData"),
                    fileConfiguration.getString("items.manageMembersRank.name"),
                    fileConfiguration.getStringList("items.manageMembersRank.lore"), false), "manageMembersRank=" + playerName);
            int manageMembersRankItemSlot = fileConfiguration.getInt("items.manageMembersRank.slot");
            inventory.setItem(manageMembersRankItemSlot, manageMembersRankItem);

            List<String> kicKmMemberItemLore = new ArrayList<>();
            Rank kickMemberRequiredRank = PluginDataManager.getClanDatabaseByPlayerName(playerName).getSubjectPermission().get(Subject.KICK);
            for (String lore : fileConfiguration.getStringList("items.kickMember.lore")) {
                lore = lore.replace("%player%", playerName);
                lore = lore.replace("%checkPermission%", ClanManager.isPlayerRankSatisfied(getOwner().getName(), kickMemberRequiredRank) ? fileConfiguration.getString("items.kickMember.placeholders.checkPermission.true")
                        : fileConfiguration.getString("items.kickMember.placeholders.checkPermission.false").replace("%getRequiredRank%", ClanManager.getFormatRank(kickMemberRequiredRank)));
                kicKmMemberItemLore.add(lore);
            }
            ItemStack kickMemberItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.kickMember.type").toUpperCase()),
                    fileConfiguration.getString("items.kickMember.value"),
                    fileConfiguration.getInt("items.kickMember.customModelData"),
                    fileConfiguration.getString("items.kickMember.name"),
                    kicKmMemberItemLore, false), "kick=" + playerName);
            int kickMemberItemSlot = fileConfiguration.getInt("items.kickMember.slot");
            inventory.setItem(kickMemberItemSlot, kickMemberItem);

        });
    }

}
