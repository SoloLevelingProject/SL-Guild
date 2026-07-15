package com.banghoi.command;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.GuildGiftManager;
import com.banghoi.clan.GuildMaintenanceManager;
import com.banghoi.clan.subject.*;
import com.banghoi.inventory.*;
import com.banghoi.language.Messages;
import com.banghoi.storage.GuildFundTransaction;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

public class ClanCommand implements CommandExecutor, TabExecutor {

    public static List<CommandSender> commandConfirmation = new ArrayList<>();

    public ClanCommand() {
        BangHoi.plugin.getCommand("guild").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, Messages.NON_CONSOLE_COMMAND);
            return false;
        }

        if (args.length == 0) {
            if (PluginDataManager.getPlayerDatabase(player.getName()).getClan() == null) {
                new NoClanInventory(player).open();
                return false;
            } else {
                new ClanMenuInventory(player).open();
                return false;
            }
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("accept")) {
                new Accept(player, player.getName()).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("reject") || args[0].equalsIgnoreCase("deny")) {
                new Reject(player, player.getName()).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("leave")) {
                new Leave(player, player.getName()).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("spawn")) {
                new Spawn(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SPAWN), player, player.getName()).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("disband")) {
                if (!commandConfirmation.contains(sender)) {
                    commandConfirmation.add(sender);
                    MessageUtil.sendMessage(player, Messages.COMMAND_CONFIRMATION);

                    BangHoi.support.getFoliaLib().getScheduler().runLaterAsync(() -> {
                        if (commandConfirmation.contains(sender)) commandConfirmation.remove(sender);
                    }, 20 * 10);
                    return false;
                } else {
                    new Disband(Rank.LEADER, player, player.getName()).execute();
                    commandConfirmation.remove(sender);
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("setspawn")) {
                new SetSpawn(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETSPAWN), player, player.getName()).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("list")) {
                new ClanListInventory(player).open();
                return false;
            }
            if (args[0].equalsIgnoreCase("seticon")) {
                new SetIconMenuInventory(player).open();
                return false;
            }
            if (args[0].equalsIgnoreCase("setpermission")) {
                new SetPermissionInventory(player).open();
                return false;
            }
            if (args[0].equalsIgnoreCase("setting")) {
                new ClanSettingsInventory(player).open();
                return false;
            }
            if (args[0].equalsIgnoreCase("upgrade")) {
                new UpgradeMenuInventory(player).open();
                return false;
            }
            if (args[0].equalsIgnoreCase("menu")) {
                if (PluginDataManager.getPlayerDatabase(player.getName()).getClan() == null) {
                    new NoClanInventory(player).open();
                    return false;
                } else {
                    new ClanMenuInventory(player).open();
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("chat")) {
                if (PluginDataManager.getPlayerDatabase(player.getName()).getClan() == null) {
                    MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
                    return false;
                } else {
                    if (!ClanManager.getPlayerUsingClanChat().contains(player)) {
                        MessageUtil.sendMessage(player, Messages.TOGGLE_CLAN_CHAT_ON);
                        ClanManager.getPlayerUsingClanChat().add(player);
                    } else {
                        MessageUtil.sendMessage(player, Messages.TOGGLE_CLAN_CHAT_OFF);
                        ClanManager.getPlayerUsingClanChat().remove(player);
                    }
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("pvp")) {
                if (PluginDataManager.getPlayerDatabase(player.getName()).getClan() == null) {
                    MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
                    return false;
                } else {
                    if (!ClanManager.getPlayerTogglingPvP().contains(player)) {
                        MessageUtil.sendMessage(player, Messages.TOGGLE_CLAN_PVP_ON);
                        ClanManager.getPlayerTogglingPvP().add(player);
                    } else {
                        MessageUtil.sendMessage(player, Messages.TOGGLE_CLAN_PVP_OFF);
                        ClanManager.getPlayerTogglingPvP().remove(player);
                    }
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("info")) {
                if (ClanManager.isPlayerInClan(player)) {
                    new ViewClanInformationInventory(player, PluginDataManager.getPlayerDatabase(player.getName()).getClan()).open();
                    return false;
                } else {
                    MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("quy")) {
                IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
                if (clanData == null) {
                    MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
                    return false;
                }
                MessageUtil.sendMessage(player, Messages.GUILD_FUND_BALANCE
                        .replace("%amount%", String.valueOf(clanData.getGuildFund())));
                return false;
            }
            if (args[0].equalsIgnoreCase("lichsuquy")) {
                sendGuildFundHistory(player);
                return false;
            }
            if (args[0].equalsIgnoreCase("trano")) {
                handlePayMaintenanceDebt(player);
                return false;
            }
            if (args[0].equalsIgnoreCase("qua") || args[0].equalsIgnoreCase("gift") || args[0].equalsIgnoreCase("nhanqua")) {
                GuildGiftManager.claim(player);
                return false;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("donggop")) {
                handleGuildFundDeposit(player, args[1]);
                return false;
            }
            if (args[0].equalsIgnoreCase("rutien")) {
                handleGuildFundWithdraw(player, args[1]);
                return false;
            }
            if (args[0].equalsIgnoreCase("info")) {
                if (PluginDataManager.getClanDatabase().containsKey(args[1])) {
                    new ViewClanInformationInventory(player, args[1]).open();
                    return false;
                } else {
                    MessageUtil.sendMessage(player, Messages.CLAN_DOES_NOT_EXIST.replace("%clan%", args[1]));
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("create")) {
                new Create(player, player.getName(), args[1]).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("invite")) {
                new Invite(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.INVITE), player, player.getName(), Bukkit.getPlayer(args[1]), args[1], Settings.CLAN_SETTING_TIME_TO_ACCEPT).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("kick")) {
                new Kick(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.KICK), player, player.getName(), Bukkit.getPlayer(args[1]), args[1]).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("setowner")) {
                new SetOwner(Rank.LEADER, player, player.getName(), Bukkit.getPlayer(args[1]), args[1]).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("setmanager")) {
                new SetManager(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETMANAGER), player, player.getName(), Bukkit.getPlayer(args[1]), args[1]).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("removemanager")) {
                new RemoveManager(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.REMOVEMANAGER), player, player.getName(), Bukkit.getPlayer(args[1]), args[1]).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("requestally")) {
                new RequestAlly(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.MANAGEALLY), player, player.getName(), args[1]).execute();
                return false;
            }
        }

        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("setcustomname")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++)
                    builder.append(args[i]).append(" ");
                builder.deleteCharAt(builder.length() - 1);

                String customName = builder.toString();
                new SetCustomName(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETCUSTOMNAME), player, player.getName(), customName).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("setmessage")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++)
                    builder.append(args[i]).append(" ");
                builder.deleteCharAt(builder.length() - 1);

                String clanMessage = builder.toString();
                new SetMessage(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETMESSAGE), player, player.getName(), clanMessage).execute();
                return false;
            }
            if (args[0].equalsIgnoreCase("chat")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++)
                    builder.append(args[i]).append(" ");
                builder.deleteCharAt(builder.length() - 1);

                String message = builder.toString();
                new Chat(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.CHAT), player, player.getName(), message).execute();
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("seticon")) {
            if (args.length < 3) {
                MessageUtil.sendMessage(player, Messages.INVALID_ICON_TYPE);
                return false;
            }

            ItemType itemType;
            try {
                itemType = ItemType.valueOf(args[1].toUpperCase());
            } catch (Exception exception) {
                MessageUtil.sendMessage(player, Messages.INVALID_ICON_TYPE);
                return false;
            }

            new SetIcon(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETICON), player, player.getName(), itemType, args[2]).execute();
            return false;
        }

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(player.getName());
        if (!ClanManager.isPlayerInClan(player)) {
            for (String nonClanMessage : Messages.COMMAND_BANGHOI_MESSAGES_NON_CLAN) {
                nonClanMessage = nonClanMessage.replace("%version%", BangHoi.plugin.getDescription().getVersion());
                player.sendMessage(BangHoi.nms.addColor(nonClanMessage));
            }
            return false;
        }

        String inClanMessage = Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN;
        StringBuilder memberCommands = new StringBuilder();
        StringBuilder managerCommands = new StringBuilder();
        StringBuilder leaderCommands = new StringBuilder();

        IClanData playerClanData = PluginDataManager.getClanDatabase(playerData.getClan());
        for (Subject subject : getPlayerClanSubjectPer(playerClanData).keySet()) {
            Rank subjectRequiredRank = getPlayerClanSubjectPer(playerClanData).get(subject);
            if (subjectRequiredRank == Rank.MEMBER) {
                String commandPlaceholder = Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_MEMBERCOMMANDS_PLACEHOLDER_COMMAND;
                commandPlaceholder = commandPlaceholder.replace("%command%", subject.toString().toLowerCase());
                commandPlaceholder = commandPlaceholder.replace("%description%", getSubjectDescription(subject));
                memberCommands.append(commandPlaceholder).append("\n");
            }
            if (subjectRequiredRank == Rank.MANAGER) {
                String commandMessage = Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_MANAGERCOMMANDS_PLACEHOLDER_COMMAND;
                commandMessage = commandMessage.replace("%command%", subject.toString().toLowerCase());
                commandMessage = commandMessage.replace("%description%", getSubjectDescription(subject));
                managerCommands.append(commandMessage).append("\na");

                // also add this to leader commands list because the player is leader
                if (playerData.getRank() == Rank.LEADER) subjectRequiredRank = Rank.LEADER;
            }
            if (subjectRequiredRank == Rank.LEADER) {
                String commandMessage = Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_LEADERCOMMANDS_PLACEHOLDER_COMMAND;
                commandMessage = commandMessage.replace("%command%", subject.toString().toLowerCase());
                commandMessage = commandMessage.replace("%description%", getSubjectDescription(subject));
                leaderCommands.append(commandMessage).append("\n");
            }
        }

        inClanMessage = inClanMessage.replace("%memberCommands%", Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_MEMBERCOMMANDS.replace("%command%", memberCommands.toString()));
        inClanMessage = inClanMessage.replace("%managerCommands%", (playerData.getRank() == Rank.MANAGER ? Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_MANAGERCOMMANDS.replace("%command%", managerCommands.toString()) : ""));
        inClanMessage = inClanMessage.replace("%leaderCommands%", (playerData.getRank() == Rank.LEADER ? Messages.COMMAND_BANGHOI_MESSAGES_IN_CLAN_PLACEHOLDER_LEADERCOMMANDS.replace("%command%", leaderCommands.toString()) : ""));
        inClanMessage = inClanMessage.replace("%version%", BangHoi.plugin.getDescription().getVersion());
        player.sendMessage(BangHoi.nms.addColor(inClanMessage));

        return false;
    }

    private void handleGuildFundDeposit(Player player, String amountText) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }

        Long amount = parsePositiveAmount(player, amountText);
        if (amount == null)
            return;

        Economy economy = BangHoi.support.getVault();
        if (economy == null) {
            player.sendMessage("Error: Vault plugin is missing, please contact the server admin immediately");
            return;
        }
        if (economy.getBalance(player) < amount) {
            MessageUtil.sendMessage(player, Messages.NOT_ENOUGH_CURRENCY
                    .replace("%currencySymbol%", Messages.CURRENCY_DISPLAY_VAULT_SYMBOL)
                    .replace("%price%", String.valueOf(amount))
                    .replace("%currencyName%", Messages.CURRENCY_DISPLAY_VAULT_NAME));
            return;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            MessageUtil.sendMessage(player, Messages.NOT_ENOUGH_CURRENCY
                    .replace("%currencySymbol%", Messages.CURRENCY_DISPLAY_VAULT_SYMBOL)
                    .replace("%price%", String.valueOf(amount))
                    .replace("%currencyName%", Messages.CURRENCY_DISPLAY_VAULT_NAME));
            return;
        }
        clanData.setGuildFund(clanData.getGuildFund() + amount);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
        PluginDataManager.addGuildFundTransaction(clanData.getName(), player.getName(), "DEPOSIT", amount, clanData.getGuildFund());

        MessageUtil.sendMessage(player, Messages.GUILD_FUND_DEPOSIT_SUCCESS
                .replace("%amount%", String.valueOf(amount))
                .replace("%balance%", String.valueOf(clanData.getGuildFund())));
    }

    private void handleGuildFundWithdraw(Player player, String amountText) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }
        if (!ClanManager.isPlayerRankSatisfied(player.getName(), Rank.MANAGER)) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(Rank.MANAGER)));
            return;
        }

        Long amount = parsePositiveAmount(player, amountText);
        if (amount == null)
            return;

        Economy economy = BangHoi.support.getVault();
        if (economy == null) {
            player.sendMessage("Error: Vault plugin is missing, please contact the server admin immediately");
            return;
        }
        if (clanData.getGuildFund() < amount) {
            MessageUtil.sendMessage(player, Messages.GUILD_FUND_NOT_ENOUGH
                    .replace("%balance%", String.valueOf(clanData.getGuildFund())));
            return;
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            player.sendMessage("Error: Could not deposit money to your account, please contact the server admin immediately");
            return;
        }
        clanData.setGuildFund(clanData.getGuildFund() - amount);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);
        PluginDataManager.addGuildFundTransaction(clanData.getName(), player.getName(), "WITHDRAW", amount, clanData.getGuildFund());

        MessageUtil.sendMessage(player, Messages.GUILD_FUND_WITHDRAW_SUCCESS
                .replace("%amount%", String.valueOf(amount))
                .replace("%balance%", String.valueOf(clanData.getGuildFund())));
    }

    private Long parsePositiveAmount(Player player, String amountText) {
        try {
            long amount = Long.parseLong(amountText);
            if (amount <= 0)
                throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException exception) {
            MessageUtil.sendMessage(player, Messages.INVALID_NUMBER);
            return null;
        }
    }

    private void handlePayMaintenanceDebt(Player player) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }
        if (clanData.getMaintenanceDebt() <= 0) {
            MessageUtil.sendMessage(player, Messages.GUILD_MAINTENANCE_NO_DEBT);
            return;
        }
        long debt = clanData.getMaintenanceDebt();
        if (!GuildMaintenanceManager.payDebtFromFund(clanData, player.getName())) {
            MessageUtil.sendMessage(player, Messages.GUILD_MAINTENANCE_PAY_DEBT_NOT_ENOUGH
                    .replace("%debt%", String.valueOf(debt))
                    .replace("%balance%", String.valueOf(clanData.getGuildFund())));
            return;
        }
        MessageUtil.sendMessage(player, Messages.GUILD_MAINTENANCE_PAY_DEBT_SUCCESS
                .replace("%amount%", String.valueOf(debt))
                .replace("%balance%", String.valueOf(clanData.getGuildFund())));
    }

    private void sendGuildFundHistory(Player player) {
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        if (clanData == null) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }

        List<GuildFundTransaction> transactions = PluginDataManager.getGuildFundTransactions(clanData.getName(), 10);
        if (transactions.isEmpty()) {
            MessageUtil.sendMessage(player, Messages.GUILD_FUND_HISTORY_EMPTY);
            return;
        }

        MessageUtil.sendMessage(player, Messages.GUILD_FUND_HISTORY_HEADER);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (GuildFundTransaction transaction : transactions) {
            MessageUtil.sendMessage(player, Messages.GUILD_FUND_HISTORY_LINE
                    .replace("%time%", dateFormat.format(new Date(transaction.getCreatedAt())))
                    .replace("%player%", transaction.getPlayerName())
                    .replace("%action%", formatGuildFundAction(transaction.getAction()))
                    .replace("%amount%", String.valueOf(transaction.getAmount()))
                    .replace("%balance%", String.valueOf(transaction.getBalanceAfter())));
        }
    }

    private String formatGuildFundAction(String action) {
        if (action.equalsIgnoreCase("DEPOSIT"))
            return "Đóng góp";
        if (action.equalsIgnoreCase("WITHDRAW"))
            return "Rút tiền";
        if (action.equalsIgnoreCase("UPGRADE"))
            return "Nâng cấp";
        if (action.equalsIgnoreCase("MAINTENANCE"))
            return "Phí duy trì";
        if (action.equalsIgnoreCase("MAINTENANCE_DEBT"))
            return "Nợ duy trì";
        if (action.equalsIgnoreCase("PAY_DEBT"))
            return "Trả nợ";
        return action;
    }

    private String getSubjectDescription(Subject subject) {
        return subject.getDescription() == null ? subject.toString().toLowerCase() : subject.getDescription();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        Player player = (Player) sender;
        String playerName = player.getName();

        IClanData playerClanData = PluginDataManager.getClanDatabaseByPlayerName(player.getName());
        IPlayerData playerData = PluginDataManager.getPlayerDatabase(player.getName());

        if (args.length == 1) {
            // general sub command
            commands.add("info");
            commands.add("list");
            // player is in a clan -> list all commands available
            if (playerClanData != null) {
                for (Subject subject : getPlayerClanSubjectPer(playerClanData).keySet()) {
                    if (ClanManager.isPlayerRankSatisfied(playerName, getPlayerClanSubjectPer(playerClanData).get(subject)))
                        commands.add(subject.toString().toLowerCase());
                }
                if (playerData.getRank() == Rank.LEADER) {
                    commands.add("disband");
                    commands.add("setowner");
                    commands.add("setpermission");
                } else {
                    commands.add("leave");
                }
                commands.add("setting");
                commands.add("upgrade");
                commands.add("donggop");
                commands.add("quy");
                commands.add("lichsuquy");
                commands.add("trano");
                commands.add("qua");
                if (playerData.getRank() == Rank.LEADER || playerData.getRank() == Rank.MANAGER)
                    commands.add("rutien");
                commands.add("menu");
                // player is not in a clan -> list all commands for non clan player
            } else {
                commands.add("create");
                commands.add("accept");
                commands.add("deny");
            }

            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            // check clan info -> list all clan name
            if (args[0].equalsIgnoreCase("info")) {
                if (!PluginDataManager.getClanDatabase().isEmpty()) {
                    commands.addAll(PluginDataManager.getClanDatabase().keySet());
                }
            }
            // all the commands below should be for the player who is in a clan
            if (playerClanData != null) {
                HashMap<Subject, Rank> clanSubjectPer = getPlayerClanSubjectPer(playerClanData);

                if (args[0].equalsIgnoreCase("invite") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.INVITE))) {
                    // list all players not in a clan
                    for (Player serverPlayer : Bukkit.getOnlinePlayers()) {
                        String serverPlayerName = serverPlayer.getName();

                        if (serverPlayerName.equalsIgnoreCase(playerName)) continue;

                        // server player is already in a clan -> skip
                        if (PluginDataManager.getClanDatabaseByPlayerName(serverPlayerName) != null) continue;

                        commands.add(serverPlayerName);
                    }
                }

                if (args[0].equalsIgnoreCase("kick") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.KICK))) {
                    // list all members in the player's clan
                    for (String memberName : playerClanData.getMembers()) {
                        if (memberName.equalsIgnoreCase(playerName)) continue;

                        if (memberName.equalsIgnoreCase(playerClanData.getOwner())) continue;

                        commands.add(memberName);
                    }
                }

                if (args[0].equalsIgnoreCase("removeally") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.MANAGEALLY))) {
                    commands.addAll(playerClanData.getAllies());
                }

                if (args[0].equalsIgnoreCase("requestally") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.MANAGEALLY))) {
                    if (!PluginDataManager.getClanDatabase().isEmpty()) {
                        for (String serverClan : PluginDataManager.getClanDatabase().keySet()) {
                            if (serverClan.equalsIgnoreCase(playerClanData.getName())) continue;

                            if (playerClanData.getAllies().contains(serverClan)) continue;

                            commands.add(serverClan);
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("setmanager") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.SETMANAGER))) {
                    // list all members in the player's clan
                    for (String memberName : playerClanData.getMembers()) {
                        if (memberName.equalsIgnoreCase(playerName)) continue;

                        if (memberName.equalsIgnoreCase(playerClanData.getOwner())) continue;

                        if (PluginDataManager.getPlayerDatabase(memberName).getRank().equals(Rank.MANAGER)) continue;

                        commands.add(memberName);
                    }
                }

                if (args[0].equalsIgnoreCase("removemanager") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.REMOVEMANAGER))) {
                    // list all members in the player's clan
                    for (String memberName : playerClanData.getMembers()) {
                        if (PluginDataManager.getPlayerDatabase(memberName).getRank().equals(Rank.MANAGER))
                            commands.add(memberName);
                    }
                }

                if (args[0].equalsIgnoreCase("seticon") && ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.SETICON))) {
                    for (ItemType itemType : ItemType.values())
                        commands.add(itemType.toString().toUpperCase());
                }

                if (args[0].equalsIgnoreCase("setowner") && ClanManager.isPlayerRankSatisfied(playerName, Rank.LEADER)) {
                    // list all members in the player's clan
                    for (String memberName : playerClanData.getMembers()) {
                        if (memberName.equalsIgnoreCase(playerClanData.getOwner())) continue;
                        commands.add(memberName);
                    }
                }
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length == 3) {
            if (playerClanData != null) {
                HashMap<Subject, Rank> clanSubjectPer = getPlayerClanSubjectPer(playerClanData);

                if (ClanManager.isPlayerRankSatisfied(playerName, clanSubjectPer.get(Subject.SETICON))) {
                    if (args[0].equalsIgnoreCase("seticon") && args[1].equalsIgnoreCase("MATERIAL")) {
                        for (Material material : Material.values()) {
                            if (material == Material.AIR) continue;
                            commands.add(material.toString().toUpperCase());
                        }
                    }
                }
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private HashMap<Subject, Rank> getPlayerClanSubjectPer(IClanData clanData) {
        if (Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED) return Settings.CLAN_SETTING_PERMISSION_DEFAULT;
        else return clanData.getSubjectPermission();
    }
}
