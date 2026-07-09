package com.banghoi.clan;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.CurrencyType;
import com.banghoi.file.GuildUpgradeFile;
import com.banghoi.language.Messages;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.entity.Player;

import java.util.Set;

public class UpgradeManager {

    public static int getDefaultLevel() {
        return GuildUpgradeFile.get().getInt("default-level", 1);
    }

    public static int getDefaultMaxMembers() {
        return GuildUpgradeFile.get().getInt("default-member", 10);
    }

    public static boolean hasLevel(int level) {
        return GuildUpgradeFile.get().contains("guild-upgrade." + level);
    }

    public static int getMaxMembersForLevel(int level) {
        if (level <= getDefaultLevel())
            return getDefaultMaxMembers();
        return GuildUpgradeFile.get().getInt("guild-upgrade." + level + ".max-member", getDefaultMaxMembers());
    }

    public static long getVaultRequireForLevel(int level) {
        return GuildUpgradeFile.get().getLong("guild-upgrade." + level + ".vault-require", -1);
    }

    public static int getLevelForMaxMembers(int maxMembers) {
        int level = getDefaultLevel();
        if (maxMembers <= getDefaultMaxMembers())
            return level;

        if (GuildUpgradeFile.get().getConfigurationSection("guild-upgrade") == null)
            return level;

        Set<String> levels = GuildUpgradeFile.get().getConfigurationSection("guild-upgrade").getKeys(false);
        for (String levelKey : levels) {
            try {
                int candidateLevel = Integer.parseInt(levelKey);
                int candidateMaxMembers = getMaxMembersForLevel(candidateLevel);
                if (candidateMaxMembers <= maxMembers && candidateLevel > level)
                    level = candidateLevel;
            } catch (NumberFormatException ignored) {
            }
        }
        return level;
    }

    public static boolean checkPlayerCurrency(Player player, CurrencyType currencyType, long value, boolean take) {
        if (currencyType == CurrencyType.VAULT) {
            if (BangHoi.support.getVault() == null) {
                MessageUtil.throwErrorMessage("THE SERVER DOES NOT HAVE THE VAULT PLUGIN TO PERFORM THE ACTION, PLEASE CHECK AGAIN");
                player.sendMessage("Error: Vault plugin is missing, please contact the server admin immediately");
                return false;
            }
            if (BangHoi.support.getVault().getBalance(player) >= value) {
                if (take) BangHoi.support.getVault().withdrawPlayer(player, value);
                return true;
            }
        }
        if (currencyType == CurrencyType.PLAYERPOINTS) {
            if (BangHoi.support.getPlayerPointsAPI() == null) {
                MessageUtil.throwErrorMessage("THE SERVER DOES NOT HAVE THE PLAYERPOINTS PLUGIN TO PERFORM THE ACTION, PLEASE CHECK AGAIN");
                player.sendMessage("Error: PlayerPoints plugin is missing, please contact the server admin immediately");
                return false;
            }
            if (BangHoi.support.getPlayerPointsAPI().look(player.getUniqueId()) >= value) {
                if (take) BangHoi.support.getPlayerPointsAPI().take(player.getUniqueId(), (int) value);
                return true;
            }
        }
        MessageUtil.sendMessage(player, Messages.NOT_ENOUGH_CURRENCY.replace("%currencySymbol%", StringUtil.getCurrencySymbolFormat(currencyType)).replace("%price%", String.valueOf(value)).replace("%currencyName%", StringUtil.getCurrencyNameFormat(currencyType)));
        return false;
    }

}
