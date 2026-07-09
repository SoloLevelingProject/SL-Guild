package com.banghoi.clan;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.CurrencyType;
import com.banghoi.language.Messages;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import org.bukkit.entity.Player;

public class UpgradeManager {

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
