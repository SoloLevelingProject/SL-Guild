package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.DatabaseType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.event.ClanDisbandEvent;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.enums.CustomHeadCategory;
import com.banghoi.support.CustomHeadSupport;
import com.banghoi.util.MessageUtil;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PluginDataManager {

    public static boolean fixClansOldData;
    public static boolean fixMembersOldData;
    public static HashMap<String, IPlayerData> playerDatabase = new HashMap<>();
    public static TreeMap<String, IClanData> clanDatabase = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static HashMap<CustomHeadCategory, List<CustomHeadData>> customHeadDatabase = new HashMap<>();

    public static HashMap<String, IPlayerData> getPlayerDatabase() {
        return playerDatabase;
    }

    public static TreeMap<String, IClanData> getClanDatabase() {
        return clanDatabase;
    }

    public static IClanData getClanDatabase(String clanName) {
        return clanDatabase.get(clanName);
    }

    public static IClanData getClanDatabaseByPlayerName(String playerName) {
        if (!getPlayerDatabase().containsKey(playerName))
            return null;

        String playerClanName = getPlayerDatabase(playerName).getClan();
        if (playerClanName == null || !getClanDatabase().containsKey(playerClanName))
            return null;

        return getClanDatabase(playerClanName);
    }

    public static boolean isPlayerInCurrentClan(String playerName) {
        IPlayerData playerData = getPlayerDatabase(playerName);
        return playerData != null && isPlayerInCurrentClan(playerName, playerData.getClan());
    }

    public static boolean isPlayerInCurrentClan(String playerName, String clanName) {
        if (clanName == null) {
            return false;
        }
        IPlayerData playerData = getPlayerDatabase(playerName);
        IClanData clanData = getClanDatabase(clanName);
        if (playerData == null || clanData == null || playerData.getClan() == null
                || !playerData.getClan().equalsIgnoreCase(clanName)) {
            return false;
        }
        if (playerName.equalsIgnoreCase(clanData.getOwner())) {
            return true;
        }
        return clanData.getMembers() != null && clanData.getMembers().stream()
                .anyMatch(member -> member.equalsIgnoreCase(playerName));
    }

    public static void updatePlayerContribution(String playerName, long contribution) {
        IPlayerData playerData = getPlayerDatabase(playerName);
        if (playerData == null) {
            return;
        }
        long newContribution = isPlayerInCurrentClan(playerName) ? Math.max(0, contribution) : 0;
        if (playerData.getScoreCollected() == newContribution) {
            return;
        }
        playerData.setScoreCollected(newContribution);
        savePlayerDatabaseToStorage(playerName, playerData);
        ClanManager.invalidateCache();
    }

    public static void setPlayerContribution(String playerName, long contribution) {
        long newContribution = isPlayerInCurrentClan(playerName) ? Math.max(0, contribution) : 0;
        if (Bukkit.getPluginManager().isPluginEnabled("TurtleTop")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "tt set " + playerName + " " + Settings.SCORE_TURTLETOP_POINT + " " + newContribution);
        }
        updatePlayerContribution(playerName, newContribution);
    }

    public static IPlayerData getPlayerDatabase(String playerName) {
        IPlayerData playerData = playerDatabase.get(playerName);
        if (playerData == null) {
            return null;
        }
        if (playerData.getClan() != null) {
            if (!clanDatabase.containsKey(playerData.getClan())) {
                clearPlayerDatabase(playerName);
            }
        }
        return playerDatabase.get(playerName);
    }

    public static void loadClanDatabase(String clanName) {
        if (getClanDatabase().containsKey(clanName))
            return;
        getClanDatabase().put(clanName, PluginDataStorage.getClanData(clanName));
    }

    public static void loadPlayerDatabase(String playerName) {
        if (getPlayerDatabase().containsKey(playerName))
            return;
        getPlayerDatabase().put(playerName, PluginDataStorage.getPlayerData(playerName));
        IPlayerData loadedPlayerData = getPlayerDatabase().get(playerName);
        if (loadedPlayerData.getClan() == null && loadedPlayerData.getScoreCollected() != 0) {
            loadedPlayerData.setScoreCollected(0);
            savePlayerDatabaseToStorage(playerName, loadedPlayerData);
        }
        if (ClanManager.managersFromOldData.containsKey(playerName)) {
            IPlayerData playerData = getPlayerDatabase(playerName);
            if (ClanManager.isPlayerInClan(playerName))
                if (ClanManager.managersFromOldData.get(playerName).equals(playerData.getClan()))
                    playerData.setRank(Rank.MANAGER);
            ClanManager.managersFromOldData.remove(playerName);
        }
    }

    public static void saveClanDatabaseToHashMap(String clanName, IClanData clanData) {
        getClanDatabase().put(clanName, clanData);
    }

    public static void savePlayerDatabaseToHashMap(String playerName, IPlayerData IPlayerData) {
        getPlayerDatabase().put(playerName, IPlayerData);
    }

    public static void saveClanDatabaseToStorage(String clanName, IClanData clanData) {
        saveClanDatabaseToHashMap(clanName, clanData);
        PluginDataStorage.saveClanData(clanName, clanData);
    }

    public static void saveClanDatabaseToStorage(String clanName) {
        if (getClanDatabase().containsKey(clanName))
            PluginDataStorage.saveClanData(clanName, getClanDatabase().get(clanName));
    }

    public static void savePlayerDatabaseToStorage(String playerName, IPlayerData playerData) {
        savePlayerDatabaseToHashMap(playerName, playerData);
        PluginDataStorage.savePlayerData(playerName, playerData);
    }

    public static void savePlayerDatabaseToStorage(String playerName) {
        if (getPlayerDatabase().containsKey(playerName))
            PluginDataStorage.savePlayerData(playerName, getPlayerDatabase().get(playerName));
    }

    // Track which custom head categories have already been loaded
    private static final Set<CustomHeadCategory> loadedHeadCategories = new HashSet<>();

    public static HashMap<CustomHeadCategory, List<CustomHeadData>> getCustomHeadDatabase() {
        return customHeadDatabase;
    }

    /**
     * LAZY: Load a custom head category on demand instead of loading all at startup.
     * Returns null if the category has no data file.
     */
    public static List<CustomHeadData> getCustomHeadDatabase(CustomHeadCategory customHeadCategory) {
        if (!loadedHeadCategories.contains(customHeadCategory)) {
            loadCustomHeadCategory(customHeadCategory);
            loadedHeadCategories.add(customHeadCategory);
        }
        return getCustomHeadDatabase().get(customHeadCategory);
    }

    public static void clearPlayerDatabase(String playerName) {
        if (!getPlayerDatabase().containsKey(playerName))
            return;

        /*
         * String playerClanName = "";
         * if (ClanManager.isPlayerInClan(playerName))
         * playerClanName = getPlayerDatabase(playerName).getClan();
         */

        playerDatabase.get(playerName).setClan(null);
        playerDatabase.get(playerName).setRank(null);
        playerDatabase.get(playerName).setJoinDate(0);
        resetPlayerContribution(playerName);

        /*
         * if (playerClanName != null && !playerClanName.equalsIgnoreCase("")) {
         * getClanDatabase(playerClanName).getMembers().remove(playerName);
         * }
         */

        savePlayerDatabaseToStorage(playerName);
        ClanManager.invalidateCache();
    }

    public static void resetPlayerContribution(String playerName) {
        setPlayerContribution(playerName, 0);
    }

    public static boolean deleteClanData(String clanName) {
        if (!getClanDatabase().containsKey(clanName))
            return false;

        IClanData clanData = getClanDatabase(clanName);

        Set<String> members = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (clanData.getOwner() != null)
            members.add(clanData.getOwner());
        if (clanData.getMembers() != null)
            members.addAll(clanData.getMembers());
        for (String memberName : members)
            clearPlayerDatabase(memberName);

        PluginDataManager.getClanDatabase().remove(clanName);
        boolean deleted = PluginDataStorage.deleteClanData(clanName);
        if (deleted) {
            Bukkit.getPluginManager().callEvent(new ClanDisbandEvent(clanName));
        }
        ClanManager.invalidateCache();
        return deleted;
    }

    public static void addGuildFundTransaction(String clanName, String playerName, String action, long amount, long balanceAfter) {
        PluginDataStorage.addGuildFundTransaction(clanName, playerName, action, amount, balanceAfter, System.currentTimeMillis());
    }

    public static List<GuildFundTransaction> getGuildFundTransactions(String clanName, int limit) {
        return PluginDataStorage.getGuildFundTransactions(clanName, limit);
    }

    public static void transferDatabase(CommandSender commandSender, DatabaseType toDatabaseType) {
        if (BangHoi.databaseType != toDatabaseType) {
            DatabaseType oldDatabaseType = BangHoi.databaseType;
            if (commandSender != null) {
                commandSender
                        .sendMessage("Transferring database from " + oldDatabaseType + " to " + toDatabaseType + "...");
                commandSender.sendMessage("DO NOT SHUT DOWN THE SERVER");
            }
            BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {
                PluginDataStorage.disableStorage();
                PluginDataStorage.init(toDatabaseType);
                if (!PluginDataManager.getClanDatabase().isEmpty()) {
                    for (String clanName : PluginDataManager.getClanDatabase().keySet()) {
                        PluginDataManager.saveClanDatabaseToStorage(clanName);
                    }
                }
                if (!PluginDataManager.getPlayerDatabase().isEmpty()) {
                    for (String playerName : PluginDataManager.getPlayerDatabase().keySet()) {
                        PluginDataManager.savePlayerDatabaseToStorage(playerName);
                    }
                }
                BangHoi.databaseType = toDatabaseType;

                // config.yml
                BangHoi.plugin.saveDefaultConfig();
                File configFile = new File(BangHoi.plugin.getDataFolder(), "config.yml");
                BangHoi.plugin.getConfig().set("database.type", toDatabaseType.toString());
                try {
                    BangHoi.plugin.getConfig().save(configFile);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                try {
                    ConfigUpdater.update(BangHoi.plugin, "config.yml", configFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BangHoi.plugin.reloadConfig();

                if (commandSender != null)
                    commandSender.sendMessage("Transferred database type from " + oldDatabaseType + " to "
                            + toDatabaseType + " completely!");
            });
        }
    }

    public static void loadAllCustomHeadsFromJsonFiles() {
        // LAZY: only set up JSON files on startup; actual parsing happens on demand
        CustomHeadSupport.setupCustomHeadJsonFiles();
        // Clear loaded tracker so categories will reload from disk on next access
        loadedHeadCategories.clear();
        customHeadDatabase.clear();
    }

    /**
     * Load a single custom head category from its JSON file on demand.
     */
    private static void loadCustomHeadCategory(CustomHeadCategory customHeadCategory) {
        String customHeadCategoryString = customHeadCategory.toString().toLowerCase().replace("_", "-");
        List<CustomHeadData> customHeadDataList = new ArrayList<>();
        try {
            String customHeadsFolderName = "customheads" + (Settings.CUSTOM_HEADS_API_V2_ENABLED ? "V2" : "");
            String jsonString = new String(Files.readAllBytes(Paths.get(BangHoi.plugin.getDataFolder() + "/"
                    + customHeadsFolderName + "/custom-head-" + customHeadCategoryString + ".json")));
            if (Settings.CUSTOM_HEADS_API_V2_ENABLED) {
                JSONObject root = new JSONObject(jsonString);
                JSONArray dataArray = root.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);

                    String name = item.getString("n");
                    String uuid = item.getString("u");

                    // convert uuid to base 64
                    String json = "{ \"textures\": { \"SKIN\": { \"url\": \"http://textures.minecraft.net/texture/"
                            + uuid + "\" } } }";
                    String base64 = Base64.getEncoder()
                            .encodeToString(json.getBytes(StandardCharsets.UTF_8));

                    customHeadDataList.add(new CustomHeadData(name, base64));
                }
            } else {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    String value = jsonObject.getString("value");
                    customHeadDataList.add(new CustomHeadData(name, value));
                }
            }
            getCustomHeadDatabase().put(customHeadCategory, customHeadDataList);
        } catch (Exception exception) {
        }
    }

    public static void loadAllDatabase() {
        if (!PluginDataStorage.getAllClans().isEmpty())
            for (String clanName : PluginDataStorage.getAllClans()) {
                loadClanDatabase(clanName);
                if (Settings.DATABASE_SETTING_SMART_LOADING_ENABLED) {
                    IClanData clanData = getClanDatabase(clanName);
                    for (String playerName : clanData.getMembers()) {
                        loadPlayerDatabase(playerName);
                    }
                }
            }
        if (!Settings.DATABASE_SETTING_SMART_LOADING_ENABLED)
            if (!PluginDataStorage.getAllPlayers().isEmpty())
                for (String playerName : PluginDataStorage.getAllPlayers())
                    loadPlayerDatabase(playerName);
        if (Settings.DATABASAE_SETTING_FIX_BUG_DATABASE_ENABLED) {
            try {
                fixIllegalDatabase();
            } catch (Exception exception) {
                if (Settings.DEBUG_ENABLED)
                    exception.printStackTrace();
                MessageUtil.debug("FIX ILLEGAL DATABASE", exception.getMessage());
            }
        }

        if (fixClansOldData || fixMembersOldData)
            saveAllDatabase();
    }

    public static void saveAllDatabase() {
        if (!getClanDatabase().isEmpty())
            for (String clanName : getClanDatabase().keySet())
                saveClanDatabaseToStorage(clanName);
        if (!getPlayerDatabase().isEmpty())
            for (String playerName : getPlayerDatabase().keySet())
                savePlayerDatabaseToStorage(playerName);
    }

    public static void fixIllegalDatabase() {
        HashMap<Integer, Integer> error = new HashMap<>();
        List<String> deletedClans = new ArrayList<>();
        if (!getClanDatabase().isEmpty()) {
            a: for (String clanName : getClanDatabase().keySet()) {
                IClanData clanData = getClanDatabase(clanName);

                if (getPlayerDatabase().containsKey(clanData.getOwner())) {
                    IPlayerData playerData = getPlayerDatabase(clanData.getOwner());
                    // id 1: leader is not in this clan
                    if (playerData.getClan() == null || !playerData.getClan().equals(clanName)) {
                        boolean isCase2Fixed = false;
                        if (clanData.getMembers().size() <= 1) {
                            MessageUtil.debug("FIXING ILLEGAL DATABASE [ID 1 CASE 1: Owner is not in this clan]",
                                    "Delete clan " + clanName + " (clan owner: " + clanData.getOwner() + ")");
                            error.put(1, error.getOrDefault(1, 0) + 1);
                            deletedClans.add(clanName);
                            continue a;
                            // case 2: check whether clan's member is in this clan to determine to delete
                            // the clan or transfer clan's leader to member
                        } else if (clanData.getMembers().size() > 1) {
                            b: for (String memberName : clanData.getMembers()) {
                                if (memberName.equals(clanData.getOwner()))
                                    continue b;
                                if (!getPlayerDatabase().containsKey(memberName))
                                    loadPlayerDatabase(memberName);

                                IPlayerData memberData = getPlayerDatabase(memberName);

                                if (memberData.getClan().equals(clanName)) {
                                    MessageUtil.debug(
                                            "FIXING ILLEGAL DATABASE [ID 1 CASE 2: Owner is not in this clan, but clan's member is]",
                                            "Transfer clan's owner to " + memberName + " (previous owner: "
                                                    + clanData.getOwner() + ")");
                                    error.put(1, error.getOrDefault(1, 0) + 1);
                                    memberData.setRank(Rank.LEADER);
                                    clanData.getMembers().remove(clanData.getOwner());
                                    clanData.setOwner(memberName);
                                    savePlayerDatabaseToStorage(memberName, memberData);
                                    isCase2Fixed = true;
                                    break b;
                                }
                            }
                        }
                        if (!isCase2Fixed) {
                            MessageUtil.debug(
                                    "FIXING ILLEGAL DATABASE [ID 1 CASE 3: Clan's owner and member are not in this clan]",
                                    "Delete clan " + clanName + " (clan owner: " + clanData.getOwner() + ")");
                            error.put(1, error.getOrDefault(1, 0) + 1);
                            deletedClans.add(clanName);
                        }
                    }
                }
                if (clanData.getMembers().isEmpty() || clanData.getMembers() == null) {
                    MessageUtil.debug("FIXING ILLEGAL DATABASE [ID 2 CASE 1: There is no members in this clan]",
                            "Delete clan " + clanName + " (clan owner: " + clanData.getOwner() + ")");
                    error.put(2, error.getOrDefault(2, 0) + 1);
                    deletedClans.add(clanName);
                } else {
                    for (String memberName : clanData.getMembers()) {
                        if (getPlayerDatabase(memberName).getClan() == null)
                            clanData.getMembers().remove(memberName);
                        else if (!getPlayerDatabase(memberName).getClan().equalsIgnoreCase(clanName))
                            clanData.getMembers().remove(memberName);
                    }
                }
                saveClanDatabaseToStorage(clanName, clanData);
            }
        }
        if (!getPlayerDatabase().isEmpty()) {
            for (String playerName : getPlayerDatabase().keySet()) {
                IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
                if (playerData.getClan() != null) {
                    if (!PluginDataManager.getClanDatabase().containsKey(playerData.getClan())) {
                        MessageUtil.debug("FIXING ILLEGAL DATABASE [ID 3 CASE 1: Player's clan does not exist]",
                                "Clear player database " + playerName);
                        clearPlayerDatabase(playerName);
                        error.put(3, error.getOrDefault(3, 0) + 1);
                    }
                }
            }
        }
        if (!deletedClans.isEmpty())
            for (String clanName : deletedClans)
                try {
                    deleteClanData(clanName);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
        if (!error.isEmpty())
            for (int errorID : error.keySet())
                MessageUtil.debug("FIXING ILLEGAL DATABASE [ID " + errorID + "]", "Total: " + error.get(errorID));
    }

    public static void backupAll(String backupFileName) {
        String backupPath = BangHoi.plugin.getDataFolder() + "\\backup\\";
        DatabaseType databaseType = BangHoi.databaseType;
        MessageUtil.debug("BACKUP (DATABASE: " + databaseType + ")", "Starting backing up database...");

        // Save database before backing up
        saveAllDatabase();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Settings.DATABASE_BACK_UP_FILE_NAME_FORMAT);
        Date date = new Date();
        if (backupFileName == null)
            backupFileName = simpleDateFormat.format(date) + " (" + databaseType.toString().toLowerCase() + ")";
        String zipFileName = backupPath + backupFileName + ".zip";

        if (BangHoi.databaseType == DatabaseType.YAML) {
            Path clanDatabaseFolder = Paths.get(BangHoi.plugin.getDataFolder() + "\\banghoiData");
            Path playerDatabaseFolder = Paths.get(BangHoi.plugin.getDataFolder() + "\\playerData");
            Path zipPath = Paths.get(zipFileName);
            try {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
                Files.walkFileTree(clanDatabaseFolder, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryName = "banghoiData/" + clanDatabaseFolder.relativize(file).toString();
                        zos.putNextEntry(new ZipEntry(entryName.replace("\\", "/")));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
                Files.walkFileTree(playerDatabaseFolder, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryName = "playerData/" + playerDatabaseFolder.relativize(file).toString();
                        zos.putNextEntry(new ZipEntry(entryName.replace("\\", "/")));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
                zos.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else if (BangHoi.databaseType == DatabaseType.H2) {
            if (PluginDataH2Storage.getConnection() == null) {
                MessageUtil.debug("BACKUP (DATABASE: " + databaseType + ")",
                        "Cannot back up because H2 is not connected!");
                return;
            }
            try {
                Statement statement = PluginDataH2Storage.getConnection().createStatement();
                String sql = "BACKUP TO '" + zipFileName + "'";
                statement.executeUpdate(sql);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else if (BangHoi.databaseType == DatabaseType.SQLITE) {
            if (PluginDataSQLiteStorage.getConnection() == null) {
                MessageUtil.debug("BACKUP (DATABASE: " + databaseType + ")",
                        "Cannot back up because SQLite is not connected!");
                return;
            }
            // SQLite has no native "BACKUP TO" SQL command. The database is a single file,
            // so we zip the .db file directly (data was already flushed by saveAllDatabase()).
            Path dbFile = Paths.get(
                    BangHoi.plugin.getDataFolder() + "/" + Settings.DATABASE_SETTINGS_SQLITE_FILE_NAME + ".db");
            if (!Files.exists(dbFile)) {
                MessageUtil.debug("BACKUP (DATABASE: " + databaseType + ")",
                        "Cannot back up because SQLite database file does not exist!");
                return;
            }
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
                zos.putNextEntry(new ZipEntry(dbFile.getFileName().toString()));
                Files.copy(dbFile, zos);
                zos.closeEntry();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        MessageUtil.debug("BACKUP (DATABASE: " + databaseType + ")", "Backup completed.");
    }
}
