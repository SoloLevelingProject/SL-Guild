package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.clan.ClanManager;
import com.banghoi.clan.UpgradeManager;
import com.banghoi.util.FileNameUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PluginDataYAMLStorage implements PluginStorage {

    private static File getClanFile(String clanName) {
        File file = new File(BangHoi.plugin.getDataFolder() + "/banghoiData/" + clanName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static File getPlayerFile(String playerName) {
        File file = new File(BangHoi.plugin.getDataFolder() + "/playerData/" + playerName + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public ClanData getClanData(String clanName) {
        File clanFile = getClanFile(clanName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(clanFile);

        List<String> members = new ArrayList<>();
        List<String> allies = new ArrayList<>();
        List<String> allyInvitation = new ArrayList<>();
        HashMap<Subject, Rank> permissionDefault = new HashMap<>();
        for (Subject subject : Subject.values())
            permissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));
        ClanData clanData = new ClanData(clanName, null, null, null, 0, 0, Settings.CLAN_SETTING_MAXIMUM_MEMBER_DEFAULT,
                UpgradeManager.getDefaultLevel(), 0, 0, 0, 0, new Date().getTime(), ItemType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE.toUpperCase()),
                Settings.CLAN_SETTING_ICON_DEFAULT_VALUE, members, null, allies, permissionDefault, allyInvitation, 0,
                null);

        if (!storage.contains("data"))
            return clanData;

        // old data before version 3.4
        if (storage.getString("data.ten") != null) {
            PluginDataManager.fixClansOldData = true;
            clanData.setName(storage.getString("data.ten"));
            clanData.setCustomName(storage.getString("data.ten_custom"));
            clanData.setOwner(storage.getString("data.leader"));
            clanData.setScore(storage.getInt("data.diem"));
            clanData.setWarning(storage.getInt("data.warn"));
            clanData.setCreatedDate(storage.getLong("data.ngay_thanh_lap"));
            clanData.setMaxMembers(storage.getInt("data.thanh_vien_toi_da"));
            clanData.setLevel(UpgradeManager.getLevelForMaxMembers(clanData.getMaxMembers()));
            clanData.setGuildFund(storage.getLong("data.guild-fund", 0));
            clanData.setMaintenanceDebt(storage.getLong("data.maintenance-debt", 0));
            clanData.setMaintenanceDebtDays(storage.getInt("data.maintenance-debt-days", 0));
            clanData.setLastMaintenanceDay(storage.getLong("data.last-maintenance-day", 0));
            for (String player : storage.getStringList("data.thanh_vien"))
                clanData.getMembers().add(player);

            storage.set("data.ten", null);
            storage.set("data.ten_custom", null);
            storage.set("data.leader", null);
            storage.set("data.diem", null);
            storage.set("data.warn", null);
            storage.set("data.ngay_thanh_lap", null);
            storage.set("data.thanh_vien_toi_da", null);
            storage.set("data.thanh_vien", null);

            if (storage.getString("data.banghoiicon") != null) {
                String oldIconValue = storage.getString("data.banghoiicon").toUpperCase();
                try {
                    XMaterial xMaterial = XMaterial.valueOf(oldIconValue);
                    if (xMaterial.get() != null) {
                        clanData.setIconType(ItemType.MATERIAL);
                        clanData.setIconValue(oldIconValue);
                    } else {
                        clanData.setIconType(ItemType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE.toUpperCase()));
                        clanData.setIconValue(Settings.CLAN_SETTING_ICON_DEFAULT_VALUE);
                    }
                } catch (Exception exception) {
                    clanData.setIconType(ItemType.valueOf(Settings.CLAN_SETTING_ICON_DEFAULT_TYPE.toUpperCase()));
                    clanData.setIconValue(Settings.CLAN_SETTING_ICON_DEFAULT_VALUE);
                }
                storage.set("data.banghoiicon", null);
            }

            try {
                storage.save(clanFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // do not add any new column here!
            clanData.setName(storage.getString("data.name"));
            clanData.setCustomName(storage.getString("data.custom-name"));
            clanData.setOwner(storage.getString("data.owner"));
            clanData.setScore(storage.getInt("data.score"));
            clanData.setWarning(storage.getInt("data.warning"));
            clanData.setCreatedDate(storage.getLong("data.created-date"));
            int level = storage.getInt("data.level", UpgradeManager.getLevelForMaxMembers(storage.getInt("data.max-members")));
            clanData.setLevel(level);
            clanData.setMaxMembers(UpgradeManager.getMaxMembersForLevel(level));
            clanData.setGuildFund(storage.getLong("data.guild-fund", 0));
            clanData.setMaintenanceDebt(storage.getLong("data.maintenance-debt", 0));
            clanData.setMaintenanceDebtDays(storage.getInt("data.maintenance-debt-days", 0));
            clanData.setLastMaintenanceDay(storage.getLong("data.last-maintenance-day", 0));
            clanData.setMembers(storage.getStringList("data.members"));
        }

        clanData.setAllies(storage.getStringList("data.allies"));

        if (storage.getString("data.managers") != null) {
            for (String manager : storage.getStringList("data.managers"))
                ClanManager.managersFromOldData.put(manager, clanName);
            storage.set("data.managers", null);
        }

        clanData.setMessage(storage.getString("data.message"));

        String iconType = storage.getString("data.icon.type");
        if (iconType != null) {
            clanData.setIconType(ItemType.valueOf(storage.getString("data.icon.type").toUpperCase()));
            clanData.setIconValue(storage.getString("data.icon.value"));
        }

        String spawnWorld = storage.getString("data.spawn.world");
        if (spawnWorld != null) {
            Location location = new Location(Bukkit.getWorld(spawnWorld), storage.getDouble("data.spawn.x"),
                    storage.getDouble("data.spawn.y"), storage.getDouble("data.spawn.z"),
                    (float) storage.getDouble("data.spawn.yaw"), (float) storage.getDouble("data.spawn.pitch"));
            clanData.setSpawnWorldName(spawnWorld);
            clanData.setSpawnPoint(location);
        }

        if (storage.getConfigurationSection("data.permission") == null) {
            HashMap<Subject, Rank> newPermissionDefault = new HashMap<>();
            for (Subject subject : Subject.values())
                newPermissionDefault.put(subject, Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(subject));
            clanData.setSubjectPermission(newPermissionDefault);
        } else {
            if (storage.getConfigurationSection("data.permission") != null) {
                for (String subjectName : storage.getConfigurationSection("data.permission").getKeys(false)) {
                    Subject subject = Subject.valueOf(subjectName);
                    Rank rank = Rank.valueOf(storage.getString("data.permission." + subjectName));
                    clanData.getSubjectPermission().put(subject, rank);
                }
            }
        }

        clanData.setAllyInvitation(storage.getStringList("data.ally-invitation"));
        clanData.setDiscordChannelID(storage.getLong("data.discord.channel-id"));
        clanData.setDiscordJoinLink(storage.getString("data.discord.join-link"));

        return clanData;
    }

    @Override
    public void saveClanData(String clanName, IClanData clanData) {
        File file = getClanFile(clanName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        storage.set("data.name", clanData.getName());
        storage.set("data.custom-name", clanData.getCustomName());
        storage.set("data.owner", clanData.getOwner());
        storage.set("data.message", clanData.getMessage());
        storage.set("data.score", clanData.getScore());
        storage.set("data.created-date", clanData.getCreatedDate());
        storage.set("data.level", clanData.getLevel());
        storage.set("data.guild-fund", clanData.getGuildFund());
        storage.set("data.maintenance-debt", clanData.getMaintenanceDebt());
        storage.set("data.maintenance-debt-days", clanData.getMaintenanceDebtDays());
        storage.set("data.last-maintenance-day", clanData.getLastMaintenanceDay());
        storage.set("data.max-members", clanData.getMaxMembers());
        storage.set("data.members", clanData.getMembers());
        storage.set("data.allies", clanData.getAllies());
        storage.set("data.warn", clanData.getWarning());
        storage.set("data.icon.type", clanData.getIconType().toString().toUpperCase());
        storage.set("data.icon.value", clanData.getIconValue());
        if (clanData.getSpawnPoint() != null) {
            String spawnWorldName = clanData.getSpawnPoint().getWorld() != null
                    ? clanData.getSpawnPoint().getWorld().getName()
                    : clanData instanceof ClanData cd ? cd.getSpawnWorldName() : null;
            storage.set("data.spawn.world", spawnWorldName);
            storage.set("data.spawn.x", clanData.getSpawnPoint().getX());
            storage.set("data.spawn.y", clanData.getSpawnPoint().getY());
            storage.set("data.spawn.z", clanData.getSpawnPoint().getZ());
            storage.set("data.spawn.yaw", clanData.getSpawnPoint().getYaw());
            storage.set("data.spawn.pitch", clanData.getSpawnPoint().getPitch());
        }
        for (Subject subject : clanData.getSubjectPermission().keySet()) {
            storage.set("data.permission." + subject.toString(),
                    clanData.getSubjectPermission().get(subject).toString().toUpperCase());
        }
        storage.set("data.ally-invitation", clanData.getAllyInvitation());
        storage.set("data.discord.channel-id", clanData.getDiscordChannelID());
        storage.set("data.discord.join-link", clanData.getDiscordJoinLink());
        storage.set("data.max-storage", null);
        storage.set("data.storage", null);

        if (storage.get("data.inventory") != null)
            storage.set("data.inventory", null);

        try {
            storage.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData getPlayerData(String playerName) {
        File playerFile = getPlayerFile(playerName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(playerFile);

        PlayerData playerData = new PlayerData(playerName,
                (Bukkit.getPlayer(playerName) != null ? Bukkit.getPlayer(playerName).getUniqueId().toString() : null),
                null, null, 0, 0, new Date().getTime(), 0);

        if (!storage.contains("data"))
            return playerData;

        if (storage.getString("data.chuc_vu") != null || storage.getString("data.bang_hoi") != null) {
            PluginDataManager.fixMembersOldData = true;
            playerData.setClan(storage.getString("data.bang_hoi"));
            playerData.setJoinDate(storage.getLong("data.ngay_tham_gia"));
            playerData.setScoreCollected(storage.getLong("data.diem_kiem_duoc"));
            try {
                playerData.setRank(Rank.valueOf(storage.getString("data.chuc_vu").toUpperCase()));
            } catch (NullPointerException | IllegalArgumentException exception) {
                playerData.setRank(null);
            }

            storage.set("data.bang_hoi", null);
            storage.set("data.ngay_tham_gia", null);
            storage.set("data.diem_kiem_duoc", null);
            storage.set("data.chuc_vu", null);

            storage.set("data.clan", playerData.getClan());
            storage.set("data.rank", String.valueOf(playerData.getRank()));
            storage.set("data.join-date", playerData.getJoinDate());
            storage.set("data.score-collected", playerData.getScoreCollected());
            try {
                storage.save(playerFile);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            playerData.setPlayerName(storage.getString("data.playerName"));
            if (storage.getString("data.UUID") == null) {
                if (Bukkit.getPlayer(playerName) != null)
                    playerData.setUUID(storage.getString(Bukkit.getPlayer(playerName).getUniqueId().toString()));
            } else
                playerData.setUUID(storage.getString("data.UUID"));
            playerData.setClan(storage.getString("data.clan"));
            playerData.setJoinDate(storage.getLong("data.join-date"));
            playerData.setScoreCollected(storage.getLong("data.score-collected"));
            playerData.setLastActivated(storage.getLong("data.last-activated"));

            playerData.setLastContributeTime(storage.getLong("data.last-contribute-time"));
            playerData.setMoneyContributeCountToday(storage.getInt("data.money-contribute-count-today"));
            try {
                playerData.setRank(Rank.valueOf(storage.getString("data.rank").toUpperCase()));
            } catch (Exception exception) {
                playerData.setRank(null);
            }
        }

        return playerData;
    }

    @Override
    public boolean savePlayerData(String playerName, IPlayerData playerData) {
        File file = getPlayerFile(playerName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        storage.set("data.playerName", playerName);
        storage.set("data.UUID", playerData.getUUID());
        storage.set("data.clan", playerData.getClan());
        storage.set("data.rank", String.valueOf(playerData.getRank()));
        storage.set("data.join-date", playerData.getJoinDate());
        storage.set("data.score-collected", playerData.getScoreCollected());
        storage.set("data.last-activated", playerData.getLastActivated());

        storage.set("data.last-contribute-time", playerData.getLastContributeTime());
        storage.set("data.money-contribute-count-today", playerData.getMoneyContributeCountToday());

        try {
            storage.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteClanData(String clanName) {
        File clanFile = new File(BangHoi.plugin.getDataFolder() + "/banghoiData/" + clanName + ".yml");
        if (!clanFile.exists())
            return true;

        try {
            return clanFile.delete();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized void addGuildFundTransaction(String clanName, String playerName, String action, long amount,
            long balanceAfter, long createdAt) {
        File file = getClanFile(clanName);
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> transactions = new ArrayList<>(storage.getMapList("data.guild-fund-history"));
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("player", playerName);
        transaction.put("action", action);
        transaction.put("amount", amount);
        transaction.put("balance-after", balanceAfter);
        transaction.put("created-at", createdAt);
        transactions.add(transaction);
        storage.set("data.guild-fund-history", transactions);
        try {
            storage.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public synchronized List<GuildFundTransaction> getGuildFundTransactions(String clanName, int limit) {
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(getClanFile(clanName));
        List<GuildFundTransaction> transactions = new ArrayList<>();
        for (Map<?, ?> entry : storage.getMapList("data.guild-fund-history")) {
            Object amount = entry.get("amount");
            Object balanceAfter = entry.get("balance-after");
            Object createdAt = entry.get("created-at");
            transactions.add(new GuildFundTransaction(
                    clanName,
                    String.valueOf(entry.get("player")),
                    String.valueOf(entry.get("action")),
                    amount instanceof Number number ? number.longValue() : 0,
                    balanceAfter instanceof Number number ? number.longValue() : 0,
                    createdAt instanceof Number number ? number.longValue() : 0));
        }
        transactions.sort(Comparator.comparingLong(GuildFundTransaction::getCreatedAt).reversed());
        return transactions.subList(0, Math.min(Math.max(0, limit), transactions.size()));
    }

    @Override
    public List<String> getAllClans() {
        File clanFolder = new File(BangHoi.plugin.getDataFolder() + "/banghoiData");
        File[] listOfFilesClan = clanFolder.listFiles();
        List<String> clans = new ArrayList<>();

        if (listOfFilesClan == null)
            return clans;

        for (File file : listOfFilesClan) {
            try {
                if (file.isFile()) {
                    String clanName = FileNameUtil.removeExtension(file.getName());
                    clans.add(clanName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return clans;
    }

    @Override
    public List<String> getAllPlayers() {
        File playerFolder = new File(BangHoi.plugin.getDataFolder() + "/playerData");
        File[] listOfFilesPlayer = playerFolder.listFiles();
        List<String> players = new ArrayList<>();

        if (listOfFilesPlayer == null)
            return players;

        for (File file : listOfFilesPlayer) {
            try {
                if (file.isFile()) {
                    String playerName = FileNameUtil.removeExtension(file.getName());
                    players.add(playerName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return players;
    }

    @Override
    public void disableStorage() {
    }
}
