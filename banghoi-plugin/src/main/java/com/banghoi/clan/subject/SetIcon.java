package com.banghoi.clan.subject;

import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.SubjectManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicReference;

public class SetIcon extends SubjectManager {

    private ItemType itemType;
    private String value;

    public SetIcon(Rank rank, Player player, String playerName, ItemType type, String value) {
        super(rank, player, playerName, null, null);
        this.itemType = type;
        this.value = value;
    }

    @Override
    public boolean execute() {
        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.SETICON));

        String commandPermission = "banghoi.seticon";
        if (!player.hasPermission(commandPermission)) {
            MessageUtil.sendMessage(player, Messages.PERMISSION_REQUIRED.replace("%permission%", commandPermission));
            return false;
        }

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        IClanData playerClanData = getPlayerClanData();

        if (itemType == ItemType.MATERIAL) {
            try {
                new AtomicReference<>(new ItemStack(Material.valueOf(value)));
            } catch (IllegalArgumentException exception) {
                MessageUtil.sendMessage(player, Messages.INVALID_ICON_VALUE);
                return false;
            }
            try {
                XMaterial xMaterial = XMaterial.valueOf(value);
                Material material = xMaterial.get();
                if (material == null || material.equals(Material.AIR)) {
                    MessageUtil.sendMessage(player, Messages.INVALID_ICON_VALUE);
                    return false;
                }
            } catch (Exception exception) {
                MessageUtil.sendMessage(player, Messages.INVALID_ICON_VALUE);
                return false;
            }
        }

        playerClanData.setIconType(itemType);
        playerClanData.setIconValue(value);

        PluginDataManager.saveClanDatabaseToStorage(playerClanData.getName(), playerClanData);
        MessageUtil.sendMessage(player, Messages.SET_ICON_SUCCESS.replace("%value%", value).replace("%type%", itemType.toString()));
        ClanManager.alertClan(playerClanData.getName(), Messages.CLAN_BROADCAST_SET_ICON.replace("%player%", playerName).replace("%value%", value).replace("%type%", itemType.toString()).replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank())));
        return true;
    }
}
