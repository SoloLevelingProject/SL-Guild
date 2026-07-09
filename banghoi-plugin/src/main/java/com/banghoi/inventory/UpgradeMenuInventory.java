package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.CurrencyType;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.UpgradeManager;
import com.banghoi.file.inventory.UpgradeMenuInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenuInventory extends BangHoiInventoryBase {

    FileConfiguration fileConfiguration = UpgradeMenuInventoryFile.get();

    public UpgradeMenuInventory(Player owner) {
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
        IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new ClanMenuInventory(getOwner()).open();
        if (itemCustomData.equals("upgradeMaxMember")) {
            // check rank
            Rank upgradeRequiredrank = Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.UPGRADE);
            if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
                upgradeRequiredrank = PluginDataManager.getClanDatabase(playerClanData.getName()).getSubjectPermission().get(Subject.UPGRADE);
            if (!ClanManager.isPlayerRankSatisfied(getOwner().getName(), upgradeRequiredrank)) {
                MessageUtil.sendMessage(getOwner(), Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(upgradeRequiredrank)));
                return true;
            }

            int newLevel = playerClanData.getLevel() + 1;
            if (!UpgradeManager.hasLevel(newLevel)) {
                MessageUtil.sendMessage(getOwner(), Messages.CLAN_MAX_LEVEL);
                return true;
            }

            int newMaxMembers = UpgradeManager.getMaxMembersForLevel(newLevel);
            long value = UpgradeManager.getVaultRequireForLevel(newLevel);
            if (playerClanData.getGuildFund() < value) {
                MessageUtil.sendMessage(getOwner(), Messages.GUILD_FUND_NOT_ENOUGH
                        .replace("%balance%", String.valueOf(playerClanData.getGuildFund())));
                return true;
            }
            playerClanData.setGuildFund(playerClanData.getGuildFund() - value);
            playerClanData.setLevel(newLevel);
            playerClanData.setMaxMembers(newMaxMembers);
            PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);
            PluginDataManager.addGuildFundTransaction(playerClanData.getName(), getOwner().getName(), "UPGRADE", value, playerClanData.getGuildFund());
            ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_UPGRADE_MAX_MEMBERS
                    .replace("%player%", getOwner().getName())
                    .replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(getOwner().getName()).getRank()))
                    .replace("%newLevel%", String.valueOf(playerClanData.getLevel()))
                    .replace("%newMaxMembers%", String.valueOf(playerClanData.getMaxMembers())));
            super.open();
        }
        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
            addBasicButton(fileConfiguration, true);

            IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName());
            List<String> upgradeMaxMembersItemLore = new ArrayList<>();
            int newLevel = playerClanData.getLevel() + 1;
            int newMaxMembers = UpgradeManager.getMaxMembersForLevel(newLevel);
            long price = UpgradeManager.getVaultRequireForLevel(newLevel);
            for (String lore : fileConfiguration.getStringList("items.upgradeMaxMember.lore")) {
                lore = lore.replace("%totalMembers%", String.valueOf(playerClanData.getMembers().size()));
                lore = lore.replace("%currentLevel%", String.valueOf(playerClanData.getLevel()));
                lore = lore.replace("%newLevel%", UpgradeManager.hasLevel(newLevel) ? String.valueOf(newLevel) : "MAX");
                lore = lore.replace("%maxMembers%", String.valueOf(playerClanData.getMaxMembers()));
                lore = lore.replace("%newMaxMembers%", String.valueOf(newMaxMembers));
                lore = lore.replace("%guildFund%", String.valueOf(playerClanData.getGuildFund()));
                lore = lore.replace("%currencySymbol%", StringUtil.getCurrencySymbolFormat(CurrencyType.VAULT));
                lore = lore.replace("%currencyName%", StringUtil.getCurrencyNameFormat(CurrencyType.VAULT));
                lore = lore.replace("%price%", UpgradeManager.hasLevel(newLevel) ? String.valueOf(price) : "MAX");
                upgradeMaxMembersItemLore.add(lore);
            }
            ItemStack upgradeMaxMembersItem = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.upgradeMaxMember.type").toUpperCase()),
                    fileConfiguration.getString("items.upgradeMaxMember.value"),
                    fileConfiguration.getInt("items.upgradeMaxMember.customModelData"),
                    fileConfiguration.getString("items.upgradeMaxMember.name"),
                    upgradeMaxMembersItemLore, false), "upgradeMaxMember");
            int upgradeMaxMembersItemSlot = fileConfiguration.getInt("items.upgradeMaxMember.slot");
            inventory.setItem(upgradeMaxMembersItemSlot, upgradeMaxMembersItem);

        });
    }

}
