package com.banghoi.api.storage;

import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;

public interface IClanData {

    String getName();

    void setName(String name);

    String getCustomName();

    void setCustomName(String customName);

    String getOwner();

    void setOwner(String owner);

    String getMessage();

    void setMessage(String message);

    int getScore();

    void setScore(int score);

    int getWarning();

    void setWarning(int warning);

    List<String> getMembers();

    void setMembers(List<String> members);

    int getMaxMembers();

    void setMaxMembers(int maxMembers);

    int getLevel();

    void setLevel(int level);

    long getGuildFund();

    void setGuildFund(long guildFund);

    long getMaintenanceDebt();

    void setMaintenanceDebt(long maintenanceDebt);

    int getMaintenanceDebtDays();

    void setMaintenanceDebtDays(int maintenanceDebtDays);

    long getLastMaintenanceDay();

    void setLastMaintenanceDay(long lastMaintenanceDay);

    long getCreatedDate();

    void setCreatedDate(long createdDate);

    ItemType getIconType();

    void setIconType(ItemType itemType);

    String getIconValue();

    void setIconValue(String iconValue);

    Location getSpawnPoint();

    void setSpawnPoint(Location spawnPoint);

    List<String> getAllies();

    void setAllies(List<String> allies);

    HashMap<Subject, Rank> getSubjectPermission();

    void setSubjectPermission(HashMap<Subject, Rank> subjectPermission);

    List<String> getAllyInvitation();

    void setAllyInvitation(List<String> allyInvitation);

    long getDiscordChannelID();

    void setDiscordChannelID(long discordChannelID);

    String getDiscordJoinLink();

    void setDiscordJoinLink(String discordJoinLink);

}
