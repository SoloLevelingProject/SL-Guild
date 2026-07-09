package com.banghoi;

import com.banghoi.api.enums.DatabaseType;
import com.banghoi.api.server.VersionSupport;
import com.banghoi.command.ClanAdminCommand;
import com.banghoi.command.ClanCommand;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.GuildMaintenanceManager;
import com.banghoi.file.GuildUpgradeFile;
import com.banghoi.file.inventory.*;
import com.banghoi.language.English;
import com.banghoi.language.Messages;
import com.banghoi.language.Vietnamese;
import com.banghoi.listener.*;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.storage.PluginDataStorage;
import com.banghoi.support.Support;
import com.banghoi.support.version.CrossVersionSupport;
import com.banghoi.util.MessageUtil;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import static com.banghoi.util.MessageUtil.log;

public class BangHoi extends JavaPlugin {

    public static BangHoi plugin;
    private static com.banghoi.api.BangHoiAPI api;
    public static VersionSupport nms;
    public static DatabaseType databaseType;
    public static Support support;

    public static com.banghoi.api.BangHoiAPI getAPI() {
        return api;
    }

    @Override
    public void onLoad() {
        plugin = this;
        nms = new CrossVersionSupport(plugin);
        api = new BangHoiAPIImpl();
        Bukkit.getServicesManager().register(com.banghoi.api.BangHoiAPI.class, api, this, ServicePriority.Highest);
    }

    @Override
    public void onEnable() {
        initFiles();
        Settings.setupValue();
        initLanguages();
        initDatabase();
        PluginDataManager.loadAllDatabase();
        initCommands();
        support = new Support();
        support.setupSupports();
        initListener();
        GuildMaintenanceManager.start();
        PluginDataManager.loadAllCustomHeadsFromJsonFiles();

        log("&f--------------------------------");
        log("&2Guild");
        log("&fVersion: &b" + getDescription().getVersion());
        log("&fAuthor: &bSoloLevelingProject");
        log("&eRunning version: " + Bukkit.getServer().getClass().getName().split("\\.")[3]);
        if (support.isFoliaLibSupported())
            log("      &2&lFOLIA SUPPORTED");
        log("");
        log("&fSupport:");
        log((support.isVaultSupported() ? "&2[SUPPORTED] &aVault" : "&4[UNSUPPORTED] &cVault"));
        log((support.isPlaceholderAPISupported() ? "&2[SUPPORTED] &aPlaceholderAPI"
                : "&4[UNSUPPORTED] &cPlaceholderAPI"));
        log((support.isPlayerPointsSupported() ? "&2[SUPPORTED] &aPlayerPoints" : "&4[UNSUPPORTED] &cPlayerPoints"));
        log("");
        log("&f--------------------------------");

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                PluginDataManager.loadPlayerDatabase(player.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void initFiles() {
        // create directories
        File configFolder = new File(getDataFolder() + "/configs");
        if (!configFolder.exists())
            configFolder.mkdirs();

        File languageFolder = new File(getDataFolder() + "/languages");
        if (!languageFolder.exists())
            languageFolder.mkdirs();

        File backupFolder = new File(getDataFolder() + "/backup");
        if (!backupFolder.exists())
            backupFolder.mkdirs();

        deleteObsoleteFile("configs/upgrade.yml");
        deleteObsoleteFile("gui/storage/storage-list-inventory.yml");
        deleteObsoleteFile("gui/storage/clan-storage-inventory.yml");
        File obsoleteStorageFolder = new File(getDataFolder(), "gui/storage");
        if (obsoleteStorageFolder.exists())
            obsoleteStorageFolder.delete();

        // config.yml
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        MessageUtil.debug("LOADING FILE", "Loaded config.yml.");

        // gui/main/clan-list-inventory.yml
        ClanListInventoryFile.setupFile();

        // gui/main/no-clan-inventory.yml
        NoClanInventoryFile.setupFile();

        // gui/main/clan-menu-inventory.yml
        ClanMenuInventoryFile.setupFile();

        // gui/members/members-menu-inventory.yml
        MembersMenuInventoryFile.setupFile();

        // gui/members/add-member-list-inventory.yml
        AddMemberListInventoryFile.setupFile();

        // gui/members/member-list-inventory.yml
        MemberListInventoryFile.setupFile();

        // gui/members/manage-member-inventory.yml
        ManageMemberInventoryFile.setupFile();

        // gui/members/manage-member-rank-inventory.yml
        ManageMemberRankInventoryFile.setupFile();

        // gui/allies/allies-menu-inventory.yml
        AlliesMenuInventoryFile.setupFile();

        // gui/allies/add-ally-list-inventory.yml
        AddAllyListInventoryFile.setupFile();

        // gui/allies/ally-invitation-list-inventory.yml
        AllyInvitationInventoryFile.setupFile();

        // gui/allies/ally-invitation-confirm-inventory.yml
        AllyInivtationConfirmInventoryFile.setupFile();

        // gui/allies/ally-list-inventory.yml
        AllyListInventoryFile.setupFile();

        // gui/allies/manage-ally-inventory.yml
        ManageAllyInventoryFile.setupFile();

        // gui/main/view-clan-inventory.yml
        ViewClanInventoryFile.setupFile();

        // gui/upgrade/upgrade-menu-inventory.yml
        UpgradeMenuInventoryFile.setupFile();

        // gui/settings/clan-settings-inventory.yml
        ClanSettingsInventoryFile.setupFile();

        // gui/settings/set-icon-custom-head-list-inventory.yml
        SetIconCustomHeadListInventoryFile.setupFile();

        // gui/settings/set-icon-material-list-inventory.yml
        SetIconMaterialListInventoryFile.setupFile();

        // gui/settings/set-icon-menu-inventory.yml
        SetIconMenuInventoryFile.setupFile();

        // gui/settings/set-permission-inventory.yml
        SetPermissionInventoryFile.setupFile();

        // gui/confirm/disband-confirmation-inventory.yml
        DisbandConfirmationInventoryFile.setupFile();

        // gui/confirm/leave-confirmation-inventory.yml
        LeaveConfirmationInventoryFile.setupFile();

        // gui/main/contribute-inventory.yml
        ContributeInventoryFile.setupFile();

        // configs/guild-upgrade.yml
        String guildUpgradeFileName = "configs/guild-upgrade.yml";
        File guildUpgradeFile = new File(getDataFolder() + "/configs/guild-upgrade.yml");
        if (!guildUpgradeFile.exists()) {
            try {
                GuildUpgradeFile.setup();
                GuildUpgradeFile.saveDefault();
                ConfigUpdater.update(this, guildUpgradeFileName, guildUpgradeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                GuildUpgradeFile.setup();
                GuildUpgradeFile.saveDefault();
                ConfigUpdater.update(this, guildUpgradeFileName, guildUpgradeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GuildUpgradeFile.reload();
        MessageUtil.debug("LOADING FILE", "Loaded guild-upgrade.yml.");
    }

    private void deleteObsoleteFile(String path) {
        File file = new File(getDataFolder(), path);
        if (file.exists())
            file.delete();
    }

    public void initLanguages() {
        // language_vi.yml
        String vietnameseFileName = "language_vi.yml";
        Vietnamese.setup();
        Vietnamese.saveDefault();
        File vietnameseFile = new File(getDataFolder(), "/languages/language_vi.yml");
        try {
            ConfigUpdater.update(this, vietnameseFileName, vietnameseFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Vietnamese.reload();

        // language_en.yml
        String englishFileName = "language_en.yml";
        English.setup();
        English.saveDefault();
        File englishFile = new File(getDataFolder(), "/languages/language_en.yml");
        try {
            ConfigUpdater.update(this, englishFileName, englishFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        English.reload();

        Messages.setupValue(Settings.LANGUAGE);
    }

    public void initCommands() {
        new ClanCommand();
        new ClanAdminCommand();
    }

    public void initListener() {
        new PlayerJoinListener();
        new InventoryClickListener();
        new EntityDamageListener();
        if ((support.getFoliaLib().isPaper() || support.getFoliaLib().isFolia())
                && Settings.CHAT_SETTING_USE_PAPER_ASYNC_CHAT) {
            new PaperAsyncChatListener();
            log("&e[PAPER OPTIMIZATION] USING PAPER ASYNC CHAT.");
        } else
            new AsyncPlayerChatListener();
        new SignChangeListener();
        new PlayerQuitListener();
        new PlayerMovementListener();
    }

    public void initDatabase() {
        try {
            databaseType = DatabaseType.valueOf(Settings.DATABASE_TYPE.toUpperCase());
            PluginDataStorage.init(databaseType);
        } catch (IllegalArgumentException exception) {
            log("&c--------------------------------------");
            log("    &4ERROR");
            log("&eDatabase type &c&l" + Settings.DATABASE_TYPE + "&e does not exist!");
            log("&ePlease check it again in config.yml.");
            log("&eDatabase will automatically use &b&lYAML &eto load.");
            log("&c--------------------------------------");
            PluginDataStorage.init(DatabaseType.YAML);
            databaseType = DatabaseType.YAML;
            Settings.DATABASE_TYPE = "YAML";
        }
    }

    @Override
    public void onDisable() {
        try {
            if (!Bukkit.getOnlinePlayers().isEmpty())
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.closeInventory();
                }
        } catch (IncompatibleClassChangeError exception) {
            // ignore it
        }

        PluginDataManager.saveAllDatabase();
        PluginDataStorage.disableStorage();

        // Clear all static caches to prevent stale references on reload
        PluginDataManager.getPlayerDatabase().clear();
        PluginDataManager.getClanDatabase().clear();
        ClanManager.beingInvitedPlayers.clear();
        ClanManager.managersFromOldData.clear();
        ClanManager.playerUsingClanChat.clear();
        ClanManager.playerTogglingPvP.clear();
        ClanManager.playerUsingChatSpy.clear();
        ClanManager.invalidateCache();
        ChatListenerHandler.createClan.clear();
        ChatListenerHandler.setCustomName.clear();
        ChatListenerHandler.setMessage.clear();
        PlayerMovementListener.spawnCountDownPlayers.clear();
        ClanCommand.commandConfirmation.clear();
        ClanAdminCommand.commandConfirmation.clear();
        ClanAdminCommand.transferDataCommandNotifying.clear();

        log("&f--------------------------------");
        log("&cGuild");
        log("&fVersion: &b" + getDescription().getVersion());
        log("&fAuthor: &bSoloLevelingProject");
        log("&f--------------------------------");
    }
}
