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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PluginDataSQLiteStorage implements PluginStorage {
    private static Connection connection;
    private static String clanTable;
    private static String playerTable;
    private static String guildFundHistoryTable;

    public PluginDataSQLiteStorage(String fileName, String clanTableName, String playerTableName) {
        clanTable = clanTableName;
        playerTable = playerTableName;
        guildFundHistoryTable = clanTableName + "_fund_history";
        try {
            if (connection != null)
                disableStorage();

            // Ensure the bundled (relocated) SQLite driver is registered.
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {
            }

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + BangHoi.plugin.getDataFolder() + "/" + fileName + ".db");
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
                    " SCORE INTEGER, " +
                    " WARNING INTEGER, " +
                    " MAXMEMBERS INTEGER, " +
                     " GUILDLEVEL INTEGER, " +
                     " GUILDFUND INTEGER, " +
                     " MAINTENANCEDEBT INTEGER, " +
                     " MAINTENANCEDEBTDAYS INTEGER, " +
                     " LASTMAINTENANCEDAY INTEGER, " +
                     " CREATEDDATE INTEGER, " +
                    " ICONTYPE TEXT, " +
                    " ICONVALUE TEXT, " +
                    " MEMBERS TEXT, " +
                    " SPAWNPOINTWORLD TEXT, " +
                    " SPAWNPOINTX REAL, " +
                    " SPAWNPOINTY REAL, " +
                    " SPAWNPOINTZ REAL, " +
                    " SPAWNPOINTYAW REAL, " +
                    " SPAWNPOINTPITCH REAL, " +
                    " ALLIES TEXT, " +
                    " SUBJECTPERMISSION TEXT, " +
                    " ALLYINVITATION TEXT, " +
                    " DISCORDCHANNELID INTEGER, " +
                    " DISCORDJOINLINK TEXT, " +
                    " CONGHUAN INTEGER, " +
                    " PRIMARY KEY (NAME))";
            statement.executeUpdate(sql);
            MessageUtil.debug("LOADING DATABASE (SQLITE)", "Connected to clan table: " + clanTable);

            // Migration columns (ignored if already present). SQLite throws
            // "duplicate column name" when the column already exists.
            addColumnIfMissing(statement, clanTable, "CONGHUAN", "INTEGER");
            addColumnIfMissing(statement, clanTable, "GUILDLEVEL", "INTEGER");
            addColumnIfMissing(statement, clanTable, "GUILDFUND", "INTEGER");
            addColumnIfMissing(statement, clanTable, "MAINTENANCEDEBT", "INTEGER");
            addColumnIfMissing(statement, clanTable, "MAINTENANCEDEBTDAYS", "INTEGER");
            addColumnIfMissing(statement, clanTable, "LASTMAINTENANCEDAY", "INTEGER");
            addColumnIfMissing(statement, clanTable, "SPAWNPOINTYAW", "REAL");
            addColumnIfMissing(statement, clanTable, "SPAWNPOINTPITCH", "REAL");

            String sql2 = "CREATE TABLE IF NOT EXISTS " + playerTable + " " +
                    "(PLAYERNAME TEXT not NULL, " +
                    " UUID TEXT, " +
                    " CLAN TEXT, " +
                    " RANK TEXT, " +
                    " JOINDATE INTEGER, " +
                    " SCORECOLLECTED INTEGER, " +
                    " LASTACTIVATED INTEGER, " +
                    " CONGHUANCONTRIBUTED INTEGER, " +
                    " LASTCONTRIBUTETIME INTEGER, " +
                    " MONEYCONTRIBUTECOUNTTODAY INTEGER, " +
                    " PRIMARY KEY (PLAYERNAME))";
            statement.executeUpdate(sql2);
            MessageUtil.debug("LOADING DATABASE (SQLITE)", "Connected to player table: " + playerTable);

            addColumnIfMissing(statement, playerTable, "CONGHUANCONTRIBUTED", "INTEGER");
            addColumnIfMissing(statement, playerTable, "LASTCONTRIBUTETIME", "INTEGER");
            addColumnIfMissing(statement, playerTable, "MONEYCONTRIBUTECOUNTTODAY", "INTEGER");

            String sql3 = "CREATE TABLE IF NOT EXISTS " + guildFundHistoryTable + " " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " CLAN TEXT not NULL, " +
                    " PLAYERNAME TEXT not NULL, " +
                    " ACTION TEXT not NULL, " +
                    " AMOUNT INTEGER, " +
                    " BALANCEAFTER INTEGER, " +
                    " CREATEDAT INTEGER)";
            statement.executeUpdate(sql3);
            MessageUtil.debug("LOADING DATABASE (SQLITE)", "Connected to guild fund history table: " + guildFundHistoryTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addColumnIfMissing(Statement statement, String table, String column, String type) {
        try {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        } catch (SQLException e) {
            // Column already exists -> ignore. SQLite message contains "duplicate column name".
            MessageUtil.debug("sqlite create table",
                    "Skipping creating column " + column + " on " + table + " (likely already exists).");
        }
    }

    public static Connection getConnection() {
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
                 "MAXMEMBERS, GUILDLEVEL, GUILDFUND, MAINTENANCEDEBT, MAINTENANCEDEBTDAYS, LASTMAINTENANCEDAY, CREATEDDATE, ICONTYPE, ICONVALUE, MEMBERS, " +
                 "SPAWNPOINTWORLD, SPAWNPOINTX, SPAWNPOINTY, SPAWNPOINTZ, SPAWNPOINTYAW, SPAWNPOINTPITCH, " +
                 "ALLIES, SUBJECTPERMISSION, ALLYINVITATION, " +
                 "DISCORDCHANNELID, DISCORDJOINLINK, CONGHUAN) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clanName); // NAME
            preparedStatement.setString(2, ""); // CUSTOMNAME
            preparedStatement.setString(3, ""); // OWNER
            preparedStatement.setString(4, ""); // MESSAGE
            preparedStatement.setInt(5, 0); // SCORE
            preparedStatement.setInt(6, 0); // WARNING
            preparedStatement.setInt(7, 0); // MAXMEMBERS
            preparedStatement.setInt(8, 0); // GUILDLEVEL
            preparedStatement.setLong(9, 0); // GUILDFUND
            preparedStatement.setLong(10, 0); // MAINTENANCEDEBT
            preparedStatement.setInt(11, 0); // MAINTENANCEDEBTDAYS
            preparedStatement.setLong(12, 0); // LASTMAINTENANCEDAY
            preparedStatement.setInt(13, 0); // CREATEDDATE
            preparedStatement.setString(14, ""); // ICONTYPE
            preparedStatement.setString(15, ""); // ICONVALUE
            preparedStatement.setString(16, ""); // MEMBERS
            preparedStatement.setString(17, ""); // SPAWNPOINTWORLD
            preparedStatement.setInt(18, 0); // SPAWNPOINTX
            preparedStatement.setInt(19, 0); // SPAWNPOINTY
            preparedStatement.setInt(20, 0); // SPAWNPOINTZ
            preparedStatement.setInt(21, 0); // SPAWNPOINTYAW
            preparedStatement.setInt(22, 0); // SPAWNPOINTPITCH
            preparedStatement.setString(23, ""); // ALLIES
            preparedStatement.setString(24, ""); // SUBJECTPERMISSION
            preparedStatement.setString(25, ""); // ALLYINVITATION
            preparedStatement.setLong(26, 0L); // DISCORDCHANNELID
            preparedStatement.setString(27, ""); // DISCORDJOINLINK
            preparedStatement.setLong(28, 0); // CONGHUAN
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
                0,
                0,
                0,
                0,
                new Date().getTime(),
                ItemType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE.toUpperCase()),
                Settings.CLAN_SETTING_ICON_DEFAULT_VALUE,
                members,
                null,
                allies,
                permissionDefault,
                allyInvitation,
                0,
                null);

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
                clanData.setGuildFund(resultSet.getLong("GUILDFUND"));
                clanData.setMaintenanceDebt(resultSet.getLong("MAINTENANCEDEBT"));
                clanData.setMaintenanceDebtDays(resultSet.getInt("MAINTENANCEDEBTDAYS"));
                clanData.setLastMaintenanceDay(resultSet.getLong("LASTMAINTENANCEDAY"));
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
                + " GUILDFUND = ?,"
                + " MAINTENANCEDEBT = ?,"
                + " MAINTENANCEDEBTDAYS = ?,"
                + " LASTMAINTENANCEDAY = ?,"
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
            preparedStatement.setLong(9, clanData.getGuildFund());
            preparedStatement.setLong(10, clanData.getMaintenanceDebt());
            preparedStatement.setInt(11, clanData.getMaintenanceDebtDays());
            preparedStatement.setLong(12, clanData.getLastMaintenanceDay());
            preparedStatement.setLong(13, clanData.getCreatedDate());
            preparedStatement.setString(14, clanData.getIconType().toString().toUpperCase());
            preparedStatement.setString(15, clanData.getIconValue());
            preparedStatement.setString(16, gson.toJson(clanData.getMembers()));
            if (clanData.getSpawnPoint() != null) {
                String spawnWorldName = clanData.getSpawnPoint().getWorld() != null
                        ? clanData.getSpawnPoint().getWorld().getName()
                        : clanData instanceof ClanData cd ? cd.getSpawnWorldName() : null;
                preparedStatement.setString(17, spawnWorldName);
                preparedStatement.setDouble(18, clanData.getSpawnPoint().getX());
                preparedStatement.setDouble(19, clanData.getSpawnPoint().getY());
                preparedStatement.setDouble(20, clanData.getSpawnPoint().getZ());
                preparedStatement.setDouble(21, clanData.getSpawnPoint().getYaw());
                preparedStatement.setDouble(22, clanData.getSpawnPoint().getPitch());
            } else {
                preparedStatement.setString(17, null);
                preparedStatement.setDouble(18, 0);
                preparedStatement.setDouble(19, 0);
                preparedStatement.setDouble(20, 0);
                preparedStatement.setDouble(21, 0);
                preparedStatement.setDouble(22, 0);
            }
            preparedStatement.setString(23, gson.toJson(clanData.getAllies()));
            preparedStatement.setString(24, gson.toJson(clanData.getSubjectPermission()));
            preparedStatement.setString(25, gson.toJson(clanData.getAllyInvitation()));
            preparedStatement.setLong(26, clanData.getDiscordChannelID());
            preparedStatement.setString(27, clanData.getDiscordJoinLink());
            preparedStatement.setLong(28, 0); // CONGHUAN (legacy, no longer used)
            preparedStatement.setString(29, clanData.getName());
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public boolean deleteClanData(String clanName) {
        if (!isClanDataExisted(clanName))
            return true;

        String sql = "DELETE FROM " + clanTable + " WHERE NAME=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, clanName);
            ps.execute();
            try (PreparedStatement historyStatement = connection.prepareStatement("DELETE FROM " + guildFundHistoryTable + " WHERE CLAN=?")) {
                historyStatement.setString(1, clanName);
                historyStatement.execute();
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public void addGuildFundTransaction(String clanName, String playerName, String action, long amount, long balanceAfter, long createdAt) {
        String sql = "INSERT INTO " + guildFundHistoryTable + " (CLAN, PLAYERNAME, ACTION, AMOUNT, BALANCEAFTER, CREATEDAT) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clanName);
            preparedStatement.setString(2, playerName);
            preparedStatement.setString(3, action);
            preparedStatement.setLong(4, amount);
            preparedStatement.setLong(5, balanceAfter);
            preparedStatement.setLong(6, createdAt);
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<GuildFundTransaction> getGuildFundTransactions(String clanName, int limit) {
        String sql = "SELECT * FROM " + guildFundHistoryTable + " WHERE CLAN=? ORDER BY CREATEDAT DESC LIMIT ?";
        List<GuildFundTransaction> transactions = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, clanName);
            preparedStatement.setInt(2, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                transactions.add(new GuildFundTransaction(
                        resultSet.getString("CLAN"),
                        resultSet.getString("PLAYERNAME"),
                        resultSet.getString("ACTION"),
                        resultSet.getLong("AMOUNT"),
                        resultSet.getLong("BALANCEAFTER"),
                        resultSet.getLong("CREATEDAT")));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return transactions;
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
