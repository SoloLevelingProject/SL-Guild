package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import org.bukkit.Location;

import java.util.*;

public class ClanData implements IClanData {

    private String name;
    private String customName;
    private String owner;
    private String message;
    private int score;
    private int warning;
    private int maxMembers;
    private int level;
    private long guildFund;
    private long maintenanceDebt;
    private int maintenanceDebtDays;
    private long lastMaintenanceDay;
    private long createdDate;
    private ItemType itemType;
    private String iconValue;
    private List<String> members;
    private Location spawnPoint;
    private String spawnWorldName;
    private List<String> allies;
    private HashMap<Subject, Rank> subjectPermission;
    private List<String> allyInvitation;
    private long discordChannelID;
    private String discordJoinLink;

    public ClanData(String name, String customName, String owner, String message, int score, int warning,
            int maxMembers, int level, long guildFund, long maintenanceDebt, int maintenanceDebtDays,
            long lastMaintenanceDay, long createdDate, ItemType itemType, String iconValue, List<String> members,
            Location spawnPoint, List<String> allies, HashMap<Subject, Rank> subjectPermission,
            List<String> allyInvitation, long discordChannelID, String discordJoinLink) {
        this.name = name;
        this.customName = customName;
        this.owner = owner;
        this.message = message;
        this.score = score;
        this.warning = warning;
        this.maxMembers = maxMembers;
        this.level = level;
        this.guildFund = guildFund;
        this.maintenanceDebt = maintenanceDebt;
        this.maintenanceDebtDays = maintenanceDebtDays;
        this.lastMaintenanceDay = lastMaintenanceDay;
        this.createdDate = createdDate;
        this.itemType = itemType;
        this.iconValue = iconValue;
        this.members = members;
        this.spawnPoint = spawnPoint;
        this.allies = allies;
        this.subjectPermission = subjectPermission;
        this.allyInvitation = allyInvitation;
        this.discordChannelID = discordChannelID;
        this.discordJoinLink = discordJoinLink;
    }

    // --- Standard Getters/Setters ---

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCustomName() {
        return this.customName;
    }

    @Override
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int getScore() {
        return this.score;
    }

    @Override
    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int getWarning() {
        return warning;
    }

    @Override
    public void setWarning(int warning) {
        this.warning = warning;
    }

    @Override
    public List<String> getMembers() {
        return members;
    }

    @Override
    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
    public int getMaxMembers() {
        return maxMembers;
    }

    @Override
    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public long getGuildFund() {
        return guildFund;
    }

    @Override
    public void setGuildFund(long guildFund) {
        this.guildFund = guildFund;
    }

    @Override
    public long getMaintenanceDebt() {
        return maintenanceDebt;
    }

    @Override
    public void setMaintenanceDebt(long maintenanceDebt) {
        this.maintenanceDebt = maintenanceDebt;
    }

    @Override
    public int getMaintenanceDebtDays() {
        return maintenanceDebtDays;
    }

    @Override
    public void setMaintenanceDebtDays(int maintenanceDebtDays) {
        this.maintenanceDebtDays = maintenanceDebtDays;
    }

    @Override
    public long getLastMaintenanceDay() {
        return lastMaintenanceDay;
    }

    @Override
    public void setLastMaintenanceDay(long lastMaintenanceDay) {
        this.lastMaintenanceDay = lastMaintenanceDay;
    }

    @Override
    public long getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public ItemType getIconType() {
        return itemType;
    }

    @Override
    public void setIconType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public String getIconValue() {
        return iconValue;
    }

    @Override
    public void setIconValue(String iconValue) {
        this.iconValue = iconValue;
    }

    @Override
    public Location getSpawnPoint() {
        return spawnPoint;
    }

    @Override
    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
        if (spawnPoint != null && spawnPoint.getWorld() != null) {
            this.spawnWorldName = spawnPoint.getWorld().getName();
        }
    }

    public String getSpawnWorldName() {
        if (spawnPoint != null && spawnPoint.getWorld() != null) {
            return spawnPoint.getWorld().getName();
        }
        return spawnWorldName;
    }

    public void setSpawnWorldName(String spawnWorldName) {
        this.spawnWorldName = spawnWorldName;
    }

    @Override
    public List<String> getAllies() {
        return allies;
    }

    @Override
    public void setAllies(List<String> allies) {
        this.allies = allies;
    }

    @Override
    public HashMap<Subject, Rank> getSubjectPermission() {
        return subjectPermission;
    }

    @Override
    public void setSubjectPermission(HashMap<Subject, Rank> subjectPermission) {
        this.subjectPermission = subjectPermission;
    }

    @Override
    public List<String> getAllyInvitation() {
        return allyInvitation;
    }

    @Override
    public void setAllyInvitation(List<String> allyInvitation) {
        this.allyInvitation = allyInvitation;
    }

    @Override
    public long getDiscordChannelID() {
        return discordChannelID;
    }

    @Override
    public void setDiscordChannelID(long discordChannelID) {
        this.discordChannelID = discordChannelID;
    }

    @Override
    public String getDiscordJoinLink() {
        return discordJoinLink;
    }

    @Override
    public void setDiscordJoinLink(String discordJoinLink) {
        this.discordJoinLink = discordJoinLink;
    }

}
