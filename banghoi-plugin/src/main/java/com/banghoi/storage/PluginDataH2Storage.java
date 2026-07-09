package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.UpgradeManager;
import com.banghoi.util.MessageUtil;
import com.banghoi.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.h2.jdbc.JdbcConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PluginDataH2Storage implements PluginStorage {
    private static JdbcConnection connection;
    private static String clanTable;
    private static String playerTable;

    public PluginDataH2Storage(String fileName, String clanTableName, String playerTableName) {
        clanTable = clanTableName;
        playerTable = playerTableName;
        try {
            if (connection != null)
                disableStorage();

            connection = new JdbcConnection(
                    "jdbc:h2:./" + BangHoi.plugin.getDataFolder() + "/" + fileName + ";mode=MySQL", new Properties(),
                    null, null, false);
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS " + clanTable + " " +
                    "(NAME TEXT not NULL, " +
                    " CUSTOMNAME TEXT, " +
                    " OWNER TEXT, " +
                    " MESSAGE TEXT, " +
                    " SCORE INT, " +
                    " WARNING INT, " +
                    " MAXMEMBERS INT, " +
                    " GUILDLEVEL INT, " +
                    " CREATEDDATE LONG, " +
                    " ICONTYPE TEXT, " +
                    " ICONVALUE TEXT, " +
                    " MEMBERS TEXT, " +
                    " SPAWNPOINTWORLD TEXT, " +
                    " SPAWNPOINTX DOUBLE, " +
                    " SPAWNPOINTY DOUBLE, " +
                    " SPAWNPOINTZ DOUBLE, " +
                    " SPAWNPOINTYAW DOUBLE, " +
                    " SPAWNPOINTPITCH DOUBLE, " +
                    " ALLIES TEXT, " +
                    " SUBJECTPERMISSION TEXT, " +
                    " ALLYINVITATION TEXT, " +
                    " DISCORDCHANNELID LONG, " +
                    " DISCORDJOINLINK TEXT, " +
                    " STORAGE TEXT, " +
                    " MAXSTORAGE INT, " +
                    " CONGHUAN LONG, " +
                    " PRIMARY KEY (NAME))";

            // 2.4 update
            // add STORAGE into the table
            try {
                String alterTableUpdate = "ALTER TABLE " + clanTable + " ADD STORAGE TEXT";
                statement.execute(alterTableUpdate);
            } catch (SQLException e) {
                if (e.getErrorCode() != 42121) { // 42121 = column already exists in H2
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column STORAGE because column already existed.");
                }
            }

            // 2.4 update
            // add MAX INVENTORY into the table
            try {
                String alterTableUpdate = "ALTER TABLE " + clanTable + " ADD MAXSTORAGE INT";
                statement.execute(alterTableUpdate);
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column MAXSTORAGE because column already existed.");
                }
            }

            // remove existing column INVENTORY from the previous versions
            try {
                statement.execute("ALTER TABLE " + clanTable + " DROP COLUMN INVENTORY");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42122) {
                    MessageUtil.debug("h2 create table",
                            "Skipping dropping column INVENTORY because column already deleted.");
                }
            }

            // contribution update
            try {
                statement.execute("ALTER TABLE " + clanTable + " ADD CONGHUAN LONG");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column CONGHUAN because column already existed.");
                }
            }

            try {
                statement.execute("ALTER TABLE " + clanTable + " ADD GUILDLEVEL INT");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column GUILDLEVEL because column already existed.");
                }
            }

            try {
                statement.execute("ALTER TABLE " + clanTable + " ADD SPAWNPOINTYAW DOUBLE");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column SPAWNPOINTYAW because column already existed.");
                }
            }
            try {
                statement.execute("ALTER TABLE " + clanTable + " ADD SPAWNPOINTPITCH DOUBLE");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column SPAWNPOINTPITCH because column already existed.");
                }
            }

            String sql2 = "CREATE TABLE IF NOT EXISTS " + playerTable + " " +
                    "(PLAYERNAME TEXT not NULL, " +
                    " UUID VARCHAR(50), " +
                    " CLAN TEXT, " +
                    " RANK VARCHAR(10), " +
                    " JOINDATE LONG, " +
                    " SCORECOLLECTED LONG, " +
                    " LASTACTIVATED LONG, " +
                    " CONGHUANCONTRIBUTED LONG, " +
                    " LASTCONTRIBUTETIME LONG, " +
                    " MONEYCONTRIBUTECOUNTTODAY INT, " +
                    " PRIMARY KEY (PLAYERNAME))";

            // contribution update - player table
            try {
                statement.execute("ALTER TABLE " + playerTable + " ADD CONGHUANCONTRIBUTED LONG");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column CONGHUANCONTRIBUTED because column already existed.");
                }
            }
            try {
                statement.execute("ALTER TABLE " + playerTable + " ADD LASTCONTRIBUTETIME LONG");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column LASTCONTRIBUTETIME because column already existed.");
                }
            }
            // daily contribution count update
            try {
                statement.execute("ALTER TABLE " + playerTable + " ADD MONEYCONTRIBUTECOUNTTODAY INT");
            } catch (SQLException e) {
                if (e.getErrorCode() == 42121) {
                    MessageUtil.debug("h2 create table",
                            "Skipping creating a new column MONEYCONTRIBUTECOUNTTODAY because column already existed.");
                }
            }
            statement.executeUpdate(sql);
            MessageUtil.debug("LOADING DATABASE (H2)", "Connected to clan table: " + clanTable);
            statement.executeUpdate(sql2);
            MessageUtil.debug("LOADING DATABASE (H2)", "Connected to player table: " + playerTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JdbcConnection getConnection() {
        return connection;
    }

    private static boolean isClanDataExisted(String clanName) {
        String sql = "select * from " + clanTable + " where NAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, clanName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            rs.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private static boolean isPlayerDataExisted(String playerName) {
        String sql = "SELECT * FROM " + playerTable + " WHERE PLAYERNAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            rs.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private static void initClanData(String clanName) {
        String sql = "INSERT INTO " + clanTable + " (" +
                "NAME, CUSTOMNAME, OWNER, MESSAGE, SCORE, WARNING, " +
                "MAXMEMBERS, GUILDLEVEL, CREATEDDATE, ICONTYPE, ICONVALUE, MEMBERS, " +
                "SPAWNPOINTWORLD, SPAWNPOINTX, SPAWNPOINTY, SPAWNPOINTZ, SPAWNPOINTYAW, SPAWNPOINTPITCH, " +
                "ALLIES, SUBJECTPERMISSION, ALLYINVITATION, " +
                "DISCORDCHANNELID, DISCORDJOINLINK, STORAGE, MAXSTORAGE, CONGHUAN) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clanName); // NAME
            preparedStatement.setString(2, ""); // CUSTOMNAME
            preparedStatement.setString(3, ""); // OWNER
            preparedStatement.setString(4, ""); // MESSAGE
            preparedStatement.setInt(5, 0); // SCORE
            preparedStatement.setInt(6, 0); // WARNING
            preparedStatement.setInt(7, 0); // MAXMEMBERS
            preparedStatement.setInt(8, 0); // GUILDLEVEL
            preparedStatement.setInt(9, 0); // CREATEDDATE
            preparedStatement.setString(10, ""); // ICONTYPE
            preparedStatement.setString(11, ""); // ICONVALUE
            preparedStatement.setString(12, ""); // MEMBERS
            preparedStatement.setString(13, ""); // SPAWNPOINTWORLD
            preparedStatement.setInt(14, 0); // SPAWNPOINTX
            preparedStatement.setInt(15, 0); // SPAWNPOINTY
            preparedStatement.setInt(16, 0); // SPAWNPOINTZ
            preparedStatement.setInt(17, 0); // SPAWNPOINTYAW
            preparedStatement.setInt(18, 0); // SPAWNPOINTPITCH
            preparedStatement.setString(19, ""); // ALLIES
            preparedStatement.setString(20, ""); // SUBJECTPERMISSION
            preparedStatement.setString(21, ""); // ALLYINVITATION
            preparedStatement.setLong(22, 0L); // DISCORDCHANNELID
            preparedStatement.setString(23, ""); // DISCORDJOINLINK
            preparedStatement.setString(24, ""); // STORAGE
            preparedStatement.setInt(25, 0); // MAXSTORAGE
            preparedStatement.setLong(26, 0); // CONGHUAN
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initPlayerData(String playerName) {
        String sql = "INSERT INTO " + playerTable + " (" +
                "PLAYERNAME, UUID, CLAN, RANK, JOINDATE, SCORECOLLECTED, LASTACTIVATED, CONGHUANCONTRIBUTED, LASTCONTRIBUTETIME, MONEYCONTRIBUTECOUNTTODAY) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, playerName); // PLAYERNAME
            preparedStatement.setString(2, ""); // UUID
            preparedStatement.setString(3, ""); // CLAN
            preparedStatement.setString(4, ""); // RANK
            preparedStatement.setLong(5, 0); // JOINDATE
            preparedStatement.setLong(6, 0); // SCORECOLLECTED
            preparedStatement.setLong(7, 0); // LASTACTIVATED
            preparedStatement.setLong(8, 0); // CONGHUANCONTRIBUTED
            preparedStatement.setLong(9, 0); // LASTCONTRIBUTETIME
            preparedStatement.setInt(10, 0); // MONEYCONTRIBUTECOUNTTODAY
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClanData getClanData(String clanName) {
        List<String> members = new ArrayList<>();
        List<String> allies = new ArrayList<>();
        List<String> allyInvitation = new ArrayList<>();
        HashMap<Subject, Rank> permissionDefault = new HashMap<>();
        HashMap<Integer, Inventory> storage = new HashMap<>();
        for (Subject subject : Subject.values())
            permissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));

        ClanData clanData = new ClanData(
                clanName,
                null,
                null,
                null,
                0,
                0,
                Settings.CLAN_SETTING_MAXIMUM_MEMBER_DEFAULT,
                UpgradeManager.getDefaultLevel(),
                new Date().getTime(),
                ItemType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE.toUpperCase()),
                Settings.CLAN_SETTING_ICON_DEFAULT_VALUE,
                members,
                null,
                allies,
                permissionDefault,
                allyInvitation,
                0,
                null,
                storage,
                Settings.CLAN_SETTINGS_MAX_STORAGE_DEFAULT);

        if (!isClanDataExisted(clanName))
            return clanData;

        String sql = "SELECT * FROM " + clanTable + " WHERE NAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, clanName);
            ResultSet resultSet = ps.executeQuery();
            Gson gson = new Gson();

            while (resultSet.next()) {
                clanData.setCustomName(resultSet.getString("CUSTOMNAME"));
                clanData.setOwner(resultSet.getString("OWNER"));
                clanData.setMessage(resultSet.getString("MESSAGE"));
                clanData.setScore(resultSet.getInt("SCORE"));
                clanData.setWarning(resultSet.getInt("WARNING"));
                int level = resultSet.getInt("GUILDLEVEL");
                if (level == 0)
                    level = UpgradeManager.getLevelForMaxMembers(resultSet.getInt("MAXMEMBERS"));
                clanData.setLevel(level);
                clanData.setMaxMembers(UpgradeManager.getMaxMembersForLevel(level));
                clanData.setCreatedDate(resultSet.getLong("CREATEDDATE"));
                clanData.setIconType(ItemType.valueOf(resultSet.getString("ICONTYPE").toUpperCase()));
                clanData.setIconValue(resultSet.getString("ICONVALUE"));
                clanData.setMembers(gson.fromJson(resultSet.getString("MEMBERS"), new TypeToken<List<String>>() {
                }.getType()));
                if (resultSet.getString("SPAWNPOINTWORLD") != null) {
                    String spawnWorldName = resultSet.getString("SPAWNPOINTWORLD");
                    clanData.setSpawnWorldName(spawnWorldName);
                    clanData.setSpawnPoint(new Location(Bukkit.getWorld(spawnWorldName),
                            resultSet.getDouble("SPAWNPOINTX"), resultSet.getDouble("SPAWNPOINTY"),
                            resultSet.getDouble("SPAWNPOINTZ"), (float) resultSet.getDouble("SPAWNPOINTYAW"),
                            (float) resultSet.getDouble("SPAWNPOINTPITCH")));
                }
                clanData.setAllies(gson.fromJson(resultSet.getString("ALLIES"), new TypeToken<List<String>>() {
                }.getType()));
                clanData.setSubjectPermission(gson.fromJson(resultSet.getString("SUBJECTPERMISSION"),
                        new TypeToken<HashMap<Subject, Rank>>() {
                        }.getType()));
                clanData.setAllyInvitation(
                        gson.fromJson(resultSet.getString("ALLYINVITATION"), new TypeToken<List<String>>() {
                        }.getType()));
                clanData.setDiscordChannelID(resultSet.getLong("DISCORDCHANNELID"));
                clanData.setDiscordJoinLink(resultSet.getString("DISCORDJOINLINK"));

                // LAZY LOADING: store raw Base64 data instead of deserializing into Inventory objects.
                // Inventories will only be inflated on demand when a player opens a storage page.
                HashMap<Integer, Map<Integer, String>> inventoryBase64 = gson.fromJson(resultSet.getString("STORAGE"),
                        new TypeToken<HashMap<Integer, Map<Integer, String>>>() {
                        }.getType());
                clanData.setSerializedStorage(inventoryBase64 != null ? inventoryBase64 : new HashMap<>());

                // Check if default max storage does not equal to 0
                if (resultSet.getInt("MAXSTORAGE") == 0)
                    clanData.setMaxStorage(Settings.CLAN_SETTINGS_MAX_STORAGE_DEFAULT);
                else
                    clanData.setMaxStorage(resultSet.getInt("MAXSTORAGE"));

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return clanData;
    }

    @Override
    public List<String> getAllClans() {
        String sql = "SELECT NAME FROM " + clanTable;
        List<String> clans = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                clans.add(resultSet.getString("NAME"));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return clans;
    }

    @Override
    public List<String> getAllPlayers() {
        String sql = "SELECT PLAYERNAME FROM " + playerTable;
        List<String> players = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("PLAYERNAME"));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return players;
    }

    @Override
    public void saveClanData(String clanName, IClanData clanData) {
        if (!isClanDataExisted(clanName))
            initClanData(clanName);

        String sql = "UPDATE " + clanTable + " "
                + "SET NAME = ?,"
                + " CUSTOMNAME = ?,"
                + " OWNER = ?,"
                + " MESSAGE = ?,"
                + " SCORE = ?,"
                + " WARNING = ?,"
                + " MAXMEMBERS = ?,"
                + " GUILDLEVEL = ?,"
                + " CREATEDDATE = ?,"
                + " ICONTYPE = ?,"
                + " ICONVALUE = ?,"
                + " MEMBERS = ?,"
                + " SPAWNPOINTWORLD = ?,"
                + " SPAWNPOINTX = ?,"
                + " SPAWNPOINTY = ?,"
                + " SPAWNPOINTZ = ?,"
                + " SPAWNPOINTYAW = ?,"
                + " SPAWNPOINTPITCH = ?,"
                + " ALLIES = ?,"
                + " SUBJECTPERMISSION = ?,"
                + " ALLYINVITATION = ?,"
                + " DISCORDCHANNELID = ?,"
                + " DISCORDJOINLINK = ?,"
                + " STORAGE = ?,"
                + " MAXSTORAGE = ?,"
                + " CONGHUAN = ?"
                + " WHERE NAME = ?";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            Gson gson = new Gson();
            preparedStatement.setString(1, clanData.getName());
            preparedStatement.setString(2, clanData.getCustomName());
            preparedStatement.setString(3, clanData.getOwner());
            preparedStatement.setString(4, clanData.getMessage());
            preparedStatement.setInt(5, clanData.getScore());
            preparedStatement.setInt(6, clanData.getWarning());
            preparedStatement.setInt(7, clanData.getMaxMembers());
            preparedStatement.setInt(8, clanData.getLevel());
            preparedStatement.setLong(9, clanData.getCreatedDate());
            preparedStatement.setString(10, clanData.getIconType().toString().toUpperCase());
            preparedStatement.setString(11, clanData.getIconValue());
            preparedStatement.setString(12, gson.toJson(clanData.getMembers()));
            if (clanData.getSpawnPoint() != null) {
                String spawnWorldName = clanData.getSpawnPoint().getWorld() != null
                        ? clanData.getSpawnPoint().getWorld().getName()
                        : clanData instanceof ClanData cd ? cd.getSpawnWorldName() : null;
                preparedStatement.setString(13, spawnWorldName);
                preparedStatement.setDouble(14, clanData.getSpawnPoint().getX());
                preparedStatement.setDouble(15, clanData.getSpawnPoint().getY());
                preparedStatement.setDouble(16, clanData.getSpawnPoint().getZ());
                preparedStatement.setDouble(17, clanData.getSpawnPoint().getYaw());
                preparedStatement.setDouble(18, clanData.getSpawnPoint().getPitch());
            } else {
                preparedStatement.setString(13, null);
                preparedStatement.setDouble(14, 0);
                preparedStatement.setDouble(15, 0);
                preparedStatement.setDouble(16, 0);
                preparedStatement.setDouble(17, 0);
                preparedStatement.setDouble(18, 0);
            }
            preparedStatement.setString(19, gson.toJson(clanData.getAllies()));
            preparedStatement.setString(20, gson.toJson(clanData.getSubjectPermission()));
            preparedStatement.setString(21, gson.toJson(clanData.getAllyInvitation()));
            preparedStatement.setLong(22, clanData.getDiscordChannelID());
            preparedStatement.setString(23, clanData.getDiscordJoinLink());

            // LAZY SAVING: sync dirty live pages, then write serialized storage directly.
            // Avoids re-serializing pages that haven't been modified.
            HashMap<Integer, Map<Integer, String>> clanInventoryBase64Converted;
            if (clanData instanceof ClanData cd) {
                clanInventoryBase64Converted = cd.syncAndGetSerializedStorage();
            } else {
                // Fallback for non-ClanData implementations
                clanInventoryBase64Converted = new HashMap<>();
                if (!clanData.getStorageHashMap().isEmpty()) {
                    for (int storageNumber : clanData.getStorageHashMap().keySet()) {
                        Inventory inventory = clanData.getStorageHashMap().get(storageNumber);
                        Map<Integer, String> items = new HashMap<>();
                        int slotNumber = -1;
                        for (ItemStack itemStack : inventory.getContents()) {
                            slotNumber++;
                            if (itemStack == null) continue;
                            if (BangHoi.nms.getCustomData(itemStack).equals("next")
                                    || BangHoi.nms.getCustomData(itemStack).equals("previous")
                                    || BangHoi.nms.getCustomData(itemStack).equals("noStorage"))
                                continue;
                            items.put(slotNumber, StringUtil.toBase64(itemStack));
                        }
                        clanInventoryBase64Converted.put(storageNumber, items);
                    }
                }
            }

            preparedStatement.setString(24, gson.toJson(clanInventoryBase64Converted));
            preparedStatement.setInt(25, clanData.getMaxStorage());
            preparedStatement.setLong(26, 0); // CONGHUAN (legacy, no longer used)
            preparedStatement.setString(27, clanData.getName());
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public boolean deleteClanData(String clanName) {
        if (!isClanDataExisted(clanName))
            return true;

        // String sql = "DELETE FROM " + clanTable + " WHERE NAME=" + clanName;
        String sql = "DELETE FROM " + clanTable + " WHERE NAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, clanName);
            ps.execute();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public PlayerData getPlayerData(String playerName) {
        PlayerData playerData = new PlayerData(
                playerName,
                (Bukkit.getPlayer(playerName) != null ? Bukkit.getPlayer(playerName).getUniqueId().toString() : null),
                null,
                null,
                0,
                0,
                new Date().getTime(),
                0);

        if (!isPlayerDataExisted(playerName))
            return playerData;

        String sql = "SELECT * FROM " + playerTable + " WHERE PLAYERNAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                playerData.setPlayerName(resultSet.getString("PLAYERNAME"));
                if (resultSet.getString("UUID") == null) {
                    if (Bukkit.getPlayer(playerName) != null)
                        playerData.setUUID(Bukkit.getPlayer(playerName).getUniqueId().toString());
                } else
                    playerData.setUUID(resultSet.getString("UUID"));
                playerData.setClan(resultSet.getString("CLAN"));
                if (resultSet.getString("RANK") != null)
                    playerData.setRank(Rank.valueOf(resultSet.getString("RANK")));
                playerData.setJoinDate(resultSet.getLong("JOINDATE"));
                playerData.setScoreCollected(resultSet.getLong("SCORECOLLECTED"));
                playerData.setLastActivated(resultSet.getLong("LASTACTIVATED"));

                try {
                    playerData.setLastContributeTime(resultSet.getLong("LASTCONTRIBUTETIME"));
                } catch (Exception ignored) {
                }
                try {
                    playerData.setMoneyContributeCountToday(resultSet.getInt("MONEYCONTRIBUTECOUNTTODAY"));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return playerData;
    }

    @Override
    public void savePlayerData(String playerName, IPlayerData playerData) {
        if (!isPlayerDataExisted(playerName))
            initPlayerData(playerName);

        String sql = "UPDATE " + playerTable + " "
                + "SET PLAYERNAME = ?,"
                + " UUID = ?,"
                + " CLAN = ?,"
                + " RANK = ?,"
                + " JOINDATE = ?,"
                + " SCORECOLLECTED = ?,"
                + " LASTACTIVATED = ?,"
                + " CONGHUANCONTRIBUTED = ?,"
                + " LASTCONTRIBUTETIME = ?,"
                + " MONEYCONTRIBUTECOUNTTODAY = ?"
                + " WHERE PLAYERNAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, playerData.getUUID());
            preparedStatement.setString(3, playerData.getClan());
            if (playerData.getRank() != null)
                preparedStatement.setString(4, playerData.getRank().toString().toUpperCase());
            else
                preparedStatement.setString(4, null);
            preparedStatement.setLong(5, playerData.getJoinDate());
            preparedStatement.setLong(6, playerData.getScoreCollected());
            preparedStatement.setLong(7, playerData.getLastActivated());
            preparedStatement.setLong(8, 0); // CONGHUANCONTRIBUTED (legacy, no longer used)
            preparedStatement.setLong(9, playerData.getLastContributeTime());
            preparedStatement.setInt(10, playerData.getMoneyContributeCountToday());
            preparedStatement.setString(11, playerName);
            preparedStatement.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void disableStorage() {
        try {
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
