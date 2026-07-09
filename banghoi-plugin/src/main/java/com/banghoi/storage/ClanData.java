package com.banghoi.storage;

import com.banghoi.BangHoi;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Rank;
import com.banghoi.api.enums.Subject;
import com.banghoi.api.storage.IClanData;
import com.banghoi.inventory.ClanStorageInventory;
import com.banghoi.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    private int maxStorage;

    // Lightweight serialized storage data — always present, low memory footprint
    // Outer key = storage page number, inner map = slot number → Base64 item string
    private HashMap<Integer, Map<Integer, String>> serializedStorage = new HashMap<>();

    // Heavyweight live Bukkit Inventory objects — only loaded on demand when a player opens storage
    private HashMap<Integer, Inventory> liveStorage = new HashMap<>();

    // Tracks which live pages have been modified and need re-serialization before save/evict
    private final Set<Integer> dirtyPages = new HashSet<>();

    public ClanData(String name, String customName, String owner, String message, int score, int warning,
            int maxMembers, int level, long guildFund, long createdDate, ItemType itemType, String iconValue, List<String> members,
            Location spawnPoint, List<String> allies, HashMap<Subject, Rank> subjectPermission,
            List<String> allyInvitation, long discordChannelID, String discordJoinLink,
            HashMap<Integer, Inventory> storage, int maxStorage) {
        this.name = name;
        this.customName = customName;
        this.owner = owner;
        this.message = message;
        this.score = score;
        this.warning = warning;
        this.maxMembers = maxMembers;
        this.level = level;
        this.guildFund = guildFund;
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
        this.liveStorage = storage;
        this.maxStorage = maxStorage;
    }

    // --- Lazy Storage Methods ---

    /**
     * Set the serialized storage data (called during database loading instead of deserializing).
     * This is the key memory optimization: raw Base64 data stays as strings instead of being
     * inflated into heavyweight Bukkit Inventory objects.
     */
    public void setSerializedStorage(HashMap<Integer, Map<Integer, String>> serializedStorage) {
        this.serializedStorage = serializedStorage != null ? serializedStorage : new HashMap<>();
    }

    /**
     * Get or load a storage page on demand.
     * Only deserializes from Base64 when a player actually opens the storage.
     */
    @Override
    public Inventory getOrLoadStorage(int storageNumber) {
        // Already live in memory
        if (liveStorage.containsKey(storageNumber)) {
            return liveStorage.get(storageNumber);
        }
        // Has serialized data — deserialize it now
        if (serializedStorage.containsKey(storageNumber)) {
            Inventory inv = deserializeStoragePage(storageNumber);
            liveStorage.put(storageNumber, inv);
            return inv;
        }
        // Page doesn't exist
        return null;
    }

    /**
     * Create a new empty storage page and mark it as live + dirty.
     */
    public Inventory createStoragePage(int storageNumber) {
        ClanStorageInventory clanStorageInventory = new ClanStorageInventory(storageNumber);
        clanStorageInventory.setClanName(name);
        Inventory inv = clanStorageInventory.getInventory();
        liveStorage.put(storageNumber, inv);
        dirtyPages.add(storageNumber);
        return inv;
    }

    /**
     * Mark a storage page as dirty (modified by a player).
     */
    public void markStorageDirty(int storageNumber) {
        dirtyPages.add(storageNumber);
    }

    /**
     * Evict a specific storage page from live memory back to serialized form.
     * Should only be called when no players are viewing this page.
     */
    public void evictStorage(int storageNumber) {
        if (liveStorage.containsKey(storageNumber)) {
            if (dirtyPages.contains(storageNumber)) {
                serializedStorage.put(storageNumber, serializeInventory(liveStorage.get(storageNumber)));
                dirtyPages.remove(storageNumber);
            }
            liveStorage.remove(storageNumber);
        }
    }

    /**
     * Evict all live storage pages back to serialized form.
     */
    public void evictAllStorage() {
        for (int key : new ArrayList<>(liveStorage.keySet())) {
            evictStorage(key);
        }
    }

    /**
     * Sync dirty live pages to serialized form and return the full serialized storage map.
     * Used by the save layer to persist data efficiently without re-serializing clean pages.
     */
    public HashMap<Integer, Map<Integer, String>> syncAndGetSerializedStorage() {
        for (int page : new HashSet<>(dirtyPages)) {
            if (liveStorage.containsKey(page)) {
                serializedStorage.put(page, serializeInventory(liveStorage.get(page)));
            }
        }
        dirtyPages.clear();
        return serializedStorage;
    }

    @Override
    public boolean hasStoragePage(int storageNumber) {
        return liveStorage.containsKey(storageNumber) || serializedStorage.containsKey(storageNumber);
    }

    @Override
    public Set<Integer> getAllStorageNumbers() {
        Set<Integer> result = new HashSet<>();
        result.addAll(liveStorage.keySet());
        result.addAll(serializedStorage.keySet());
        return result;
    }

    @Override
    public int getStorageItemCount(int storageNumber) {
        // If live, count from live inventory (filtering navigation items)
        if (liveStorage.containsKey(storageNumber)) {
            int count = 0;
            for (ItemStack item : liveStorage.get(storageNumber).getContents()) {
                if (item == null) continue;
                String customData = BangHoi.nms.getCustomData(item);
                if (customData.equals("next") || customData.equals("previous") || customData.equals("noStorage"))
                    continue;
                count++;
            }
            return count;
        }
        // If only serialized, the map already excludes navigation items
        if (serializedStorage.containsKey(storageNumber)) {
            return serializedStorage.get(storageNumber).size();
        }
        return 0;
    }

    @Override
    public int getTotalStorageItemCount() {
        int total = 0;
        for (int page : getAllStorageNumbers()) {
            total += getStorageItemCount(page);
        }
        return total;
    }

    // --- Private Helpers ---

    private Inventory deserializeStoragePage(int storageNumber) {
        Map<Integer, String> inventoryMap = serializedStorage.get(storageNumber);
        ClanStorageInventory clanStorageInventory = new ClanStorageInventory(storageNumber);
        clanStorageInventory.setClanName(name);
        Inventory newInventory = clanStorageInventory.getInventory();
        for (int slotNumber : inventoryMap.keySet()) {
            try {
                ItemStack itemStack = StringUtil.stacksFromBase64(inventoryMap.get(slotNumber))[0];
                if (itemStack == null) continue;
                if (BangHoi.nms.getCustomData(itemStack).equals("next")
                        || BangHoi.nms.getCustomData(itemStack).equals("previous")
                        || BangHoi.nms.getCustomData(itemStack).equals("noStorage"))
                    continue;
                newInventory.setItem(slotNumber, itemStack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newInventory;
    }

    private Map<Integer, String> serializeInventory(Inventory inventory) {
        Map<Integer, String> items = new HashMap<>();
        int slotNumber = -1;
        for (ItemStack itemStack : inventory.getContents()) {
            slotNumber++;
            if (itemStack == null) continue;
            String customData = BangHoi.nms.getCustomData(itemStack);
            if (customData.equals("next") || customData.equals("previous") || customData.equals("noStorage"))
                continue;
            items.put(slotNumber, StringUtil.toBase64(itemStack));
        }
        return items;
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

    /**
     * Returns only the currently live (deserialized) inventories.
     * For most use cases, prefer getOrLoadStorage() or getStorageItemCount() instead.
     */
    @Override
    public HashMap<Integer, Inventory> getStorageHashMap() {
        return liveStorage;
    }

    @Override
    public void setStorageHashMap(HashMap<Integer, Inventory> inventory) {
        this.liveStorage = inventory;
    }

    @Override
    public int getMaxStorage() {
        return maxStorage;
    }

    @Override
    public void setMaxStorage(int maxStorage) {
        this.maxStorage = maxStorage;
    }

}
