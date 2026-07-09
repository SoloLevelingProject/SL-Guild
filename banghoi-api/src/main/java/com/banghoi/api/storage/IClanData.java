package com.banghoi.api.storage;

import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    HashMap<Integer, Inventory> getStorageHashMap();

    void setStorageHashMap(HashMap<Integer, Inventory> inventory);

    int getMaxStorage();

    void setMaxStorage(int maxStorage);

    // --- Lazy storage accessors (default implementations for backward compatibility) ---

    /**
     * Get or load a specific storage page. Implementations may lazy-load from serialized data.
     */
    default Inventory getOrLoadStorage(int storageNumber) {
        return getStorageHashMap().get(storageNumber);
    }

    /**
     * Check if a storage page exists (either live or serialized).
     */
    default boolean hasStoragePage(int storageNumber) {
        return getStorageHashMap().containsKey(storageNumber);
    }

    /**
     * Get all storage page numbers (union of live and serialized).
     */
    default Set<Integer> getAllStorageNumbers() {
        return new HashSet<>(getStorageHashMap().keySet());
    }

    /**
     * Get item count for a storage page without necessarily deserializing.
     */
    default int getStorageItemCount(int storageNumber) {
        Inventory inv = getStorageHashMap().get(storageNumber);
        if (inv == null) return 0;
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null) count++;
        }
        return count;
    }

    /**
     * Get total stored items across all storage pages.
     */
    default int getTotalStorageItemCount() {
        int total = 0;
        for (int page : getAllStorageNumbers()) {
            total += getStorageItemCount(page);
        }
        return total;
    }

}
