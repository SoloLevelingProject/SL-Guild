package com.banghoi.clan;

import com.banghoi.api.storage.IClanData;
import com.banghoi.file.GuildGiftFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.CommandUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GuildGiftManager {

    public static void claim(Player player) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }

        long today = LocalDate.now().toEpochDay();
        String claimPath = "claims." + player.getUniqueId();
        if (GuildGiftFile.getClaims().getLong(claimPath + ".day") == today) {
            MessageUtil.sendMessage(player, Messages.GUILD_GIFT_ALREADY_CLAIMED);
            return;
        }

        List<String> commands = getRewardCommands(clanData.getLevel());
        if (commands.isEmpty()) {
            MessageUtil.sendMessage(player, Messages.GUILD_GIFT_NO_GIFT.replace("%level%", String.valueOf(clanData.getLevel())));
            return;
        }

        GuildGiftFile.getClaims().set(claimPath + ".player", player.getName());
        GuildGiftFile.getClaims().set(claimPath + ".day", today);
        GuildGiftFile.saveClaims();

        for (String command : commands) {
            CommandUtil.dispatchCommand(player, command);
        }
        MessageUtil.sendMessage(player, Messages.GUILD_GIFT_CLAIM_SUCCESS.replace("%level%", String.valueOf(clanData.getLevel())));
    }

    private static List<String> getRewardCommands(int guildLevel) {
        ConfigurationSection section = GuildGiftFile.get().getConfigurationSection("guild-gift");
        if (section == null)
            return new ArrayList<>();

        int rewardLevel = 0;
        for (String levelText : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelText);
                if (level <= guildLevel && level > rewardLevel)
                    rewardLevel = level;
            } catch (NumberFormatException ignored) {
            }
        }
        if (rewardLevel == 0)
            return new ArrayList<>();
        return GuildGiftFile.get().getStringList("guild-gift." + rewardLevel + ".commands");
    }
}
