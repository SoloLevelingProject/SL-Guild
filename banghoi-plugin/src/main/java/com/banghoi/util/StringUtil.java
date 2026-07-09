package com.banghoi.util;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.CurrencyType;
import com.banghoi.clan.ClanManager;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

    public static String dateTimeToDateFormat(long time) {
        if (time == 0) return BangHoi.nms.addColor(Messages.UNKNOWN);
        return new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date(time));
    }

    public static String getCurrencySymbolFormat(CurrencyType currencyType) {
        if (currencyType == CurrencyType.VAULT) return Messages.CURRENCY_DISPLAY_VAULT_SYMBOL;
        if (currencyType == CurrencyType.PLAYERPOINTS) return Messages.CURRENCY_DISPLAY_PLAYERPOINTS_SYMBOL;
        return null;
    }

    public static String getCurrencyNameFormat(CurrencyType currencyType) {
        if (currencyType == CurrencyType.VAULT) return Messages.CURRENCY_DISPLAY_VAULT_NAME;
        if (currencyType == CurrencyType.PLAYERPOINTS) return Messages.CURRENCY_DISPLAY_PLAYERPOINTS_NAME;
        return null;
    }

    public static String getProgressBar(int current, int max) {
        float percent = (float) current / max;
        int progressBars = (int) (Settings.PROGRESS_BAR_TOTAL_BARS * percent);

        return BangHoi.nms.addColor(
                (Settings.PROGRESS_BAR_SYMBOL_COMPLETED.repeat(progressBars)
                        + Settings.PROGRESS_BAR_SYMBOL_NOTCOMPLETED.repeat(
                        Settings.PROGRESS_BAR_TOTAL_BARS - progressBars))
                        + "&r"
        );    }

    public static String getStatus(boolean status) {
        if (status) return Messages.STATUS_ENABLE;
        else return Messages.STATUS_DISABLE;
    }

    public static String setClanNamePlaceholder(String string, String clanName) {
        if (!ClanManager.isClanExisted(clanName)) return string;

        return string.replace("%formatClanName%", ClanManager.getFormatClanName(PluginDataManager.getClanDatabase(clanName))).replace("%clanName%", clanName);
    }

    public static String getTimeFormat(long seconds, String hhmmss, String mmss, String ss) {
        if (seconds > 3600) {
            hhmmss = hhmmss.replace("%hours%", String.valueOf(seconds / 3600));
            hhmmss = hhmmss.replace("%minutes%", String.valueOf((seconds % 3600) / 60));
            hhmmss = hhmmss.replace("%seconds%", String.valueOf(seconds % 60));
            return BangHoi.nms.addColor(hhmmss);
        } else if (seconds > 60) {
            mmss = mmss.replace("%minutes%", String.valueOf((seconds / 60)));
            mmss = mmss.replace("%seconds%", String.valueOf(seconds % 60));
            return BangHoi.nms.addColor(mmss);
        } else return BangHoi.nms.addColor(ss.replace("%seconds%", String.valueOf(seconds)));
    }

    public static String toBase64(ItemStack itemstack) {
        ItemStack[] arr = new ItemStack[1];
        arr[0] = itemstack;
        return toBase64(arr);
    }

    public static String toBase64(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);

            for (ItemStack itemStack : contents) {
                dataOutput.writeObject(BangHoi.nms.getItemStack(itemStack));
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to save item stacks.", exception);
        }
    }

    public static Inventory inventoryFromBase64(String data) throws IOException {
        ItemStack[] stacks = stacksFromBase64(data);
        Inventory inventory = Bukkit.createInventory(null, (int) Math.ceil(stacks.length / 9D) * 9);

        for (int i = 0; i < stacks.length; i++) {
            inventory.setItem(i, stacks[i]);
        }

        return inventory;
    }

    public static ItemStack[] stacksFromBase64(String data) throws IOException {
        try {
            if (data == null || Base64Coder.decodeLines(data) == null) return new ItemStack[]{};
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] stacks = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = BangHoi.nms.getItemStack((ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return stacks;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

}
