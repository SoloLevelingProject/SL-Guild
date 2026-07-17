package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.storage.IClanData;
import com.banghoi.api.storage.IPlayerData;
import com.banghoi.file.SkillBookContributeFile;
import com.banghoi.file.inventory.SkillBookListInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkillBookListInventory extends BangHoiInventoryBase {
    private static final NamespacedKey SKILL_KEY = NamespacedKey.fromString("shadowhunter:shadow_skill_id");
    private static final NamespacedKey SLOT_KEY = NamespacedKey.fromString("banghoi:skillbook_slot");
    private final FileConfiguration fileConfiguration = SkillBookListInventoryFile.get();

    public SkillBookListInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        if (!isEnabled()) {
            MessageUtil.sendMessage(getOwner(), Messages.CONTRIBUTION_DISABLED);
            return;
        }
        if (!PluginDataManager.isPlayerInCurrentClan(getOwner().getName())) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            return;
        }
        super.open();
    }

    private boolean isEnabled() {
        return Settings.CONTRIBUTION_ENABLED && SkillBookContributeFile.get().getBoolean("enabled", true);
    }

    @Override
    public String getMenuName() {
        return BangHoi.nms.addColor(fileConfiguration.getString("title"));
    }

    @Override
    public int getSlots() {
        int slots = fileConfiguration.getInt("rows") * 9;
        return slots < 27 || slots > 54 ? 54 : slots;
    }

    @Override
    public boolean handleMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null)
            return false;

        String itemCustomData = BangHoi.nms.getCustomData(item);
        if ("back".equals(itemCustomData)) {
            new ContributeInventory(getOwner()).open();
            return true;
        }

        if (!item.hasItemMeta() || SLOT_KEY == null)
            return false;
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        Integer originalSlot = data.get(SLOT_KEY, PersistentDataType.INTEGER);
        if (originalSlot == null)
            return false;
        contribute(originalSlot, event.isRightClick());
        return true;
    }

    private void contribute(int originalSlot, boolean contributeAll) {
        Player player = getOwner();
        String playerName = player.getName();
        if (!isEnabled()) {
            MessageUtil.sendMessage(player, Messages.CONTRIBUTION_DISABLED);
            return;
        }

        IPlayerData playerData = PluginDataManager.getPlayerDatabase(playerName);
        IClanData clanData = PluginDataManager.getClanDatabaseByPlayerName(playerName);
        if (playerData == null || clanData == null || !PluginDataManager.isPlayerInCurrentClan(playerName)) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return;
        }

        ItemStack selectedBook = player.getInventory().getItem(originalSlot);
        String skillId = getSkillId(selectedBook);
        if (skillId == null) {
            player.sendMessage(BangHoi.nms.addColor("&cSách kỹ năng không còn hợp lệ hoặc không tồn tại trong túi đồ!"));
            new SkillBookListInventory(player).open();
            return;
        }

        String normalizedId = skillId.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        long pointsPerBook = SkillBookContributeFile.get().getLong("skillbooks." + normalizedId + ".points", 0);
        if (pointsPerBook <= 0) {
            player.sendMessage(BangHoi.nms.addColor("&cSách này không có điểm cống hiến được cấu hình."));
            return;
        }

        List<Integer> slots = new ArrayList<>();
        int consumed = collectBooks(skillId, originalSlot, contributeAll, slots);
        if (consumed == 0) {
            player.sendMessage(BangHoi.nms.addColor("&cKhông tìm thấy sách hợp lệ để cống hiến!"));
            return;
        }

        long currentContribution = Math.max(0, playerData.getScoreCollected());
        if (pointsPerBook > Long.MAX_VALUE / consumed
                || currentContribution > Long.MAX_VALUE - pointsPerBook * consumed) {
            player.sendMessage(BangHoi.nms.addColor("&cĐiểm cống hiến của bạn đã đạt giới hạn."));
            return;
        }
        long reward = pointsPerBook * consumed;

        if (!PluginDataManager.trySetPlayerContribution(playerName, currentContribution + reward)) {
            player.sendMessage(BangHoi.nms.addColor("&cKhông thể lưu điểm cống hiến. Sách chưa bị trừ."));
            return;
        }
        removeBooks(slots, contributeAll);

        player.sendMessage(BangHoi.nms.addColor("&aThành công! &fBạn đã cống hiến &d" + consumed
                + " Sách Kỹ Năng &fvà nhận được &b" + reward + " &fđiểm cống hiến."));
        new SkillBookListInventory(player).open();
    }

    private int collectBooks(String skillId, int originalSlot, boolean contributeAll, List<Integer> slots) {
        if (!contributeAll) {
            ItemStack item = getOwner().getInventory().getItem(originalSlot);
            if (item == null || item.getAmount() <= 0 || !skillId.equals(getSkillId(item)))
                return 0;
            slots.add(originalSlot);
            return 1;
        }

        int total = 0;
        ItemStack[] contents = getOwner().getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item != null && skillId.equals(getSkillId(item))) {
                slots.add(slot);
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeBooks(List<Integer> slots, boolean all) {
        for (int slot : slots) {
            ItemStack item = getOwner().getInventory().getItem(slot);
            if (item == null)
                continue;
            if (all || item.getAmount() == 1)
                getOwner().getInventory().setItem(slot, null);
            else
                item.setAmount(item.getAmount() - 1);
        }
    }

    private String getSkillId(ItemStack item) {
        if (item == null || !item.hasItemMeta() || SKILL_KEY == null)
            return null;
        return item.getItemMeta().getPersistentDataContainer().get(SKILL_KEY, PersistentDataType.STRING);
    }

    @Override
    public void setMenuItems() {
        if (fileConfiguration.getBoolean("items.border.enabled")) {
            ItemStack border = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.border.type").toUpperCase()),
                    fileConfiguration.getString("items.border.value"),
                    fileConfiguration.getInt("items.border.customModelData"),
                    fileConfiguration.getString("items.border.name"),
                    fileConfiguration.getStringList("items.border.lore"), false), "border");
            List<Integer> bookSlots = fileConfiguration.getIntegerList("items.border.leave-blank");
            for (int slot = 0; slot < getSlots(); slot++)
                if (!bookSlots.contains(slot))
                    inventory.setItem(slot, border);
        }

        ItemStack back = BangHoi.nms.addCustomData(ItemUtil.getItem(
                ItemType.valueOf(fileConfiguration.getString("items.back.type").toUpperCase()),
                fileConfiguration.getString("items.back.value"),
                fileConfiguration.getInt("items.back.customModelData"),
                fileConfiguration.getString("items.back.name"),
                fileConfiguration.getStringList("items.back.lore"), false), "back");
        inventory.setItem(fileConfiguration.getInt("items.back.slot"), back);
        populateSkillBooks();
    }

    private void populateSkillBooks() {
        if (SKILL_KEY == null || SLOT_KEY == null)
            return;
        List<Integer> guiSlots = fileConfiguration.getIntegerList("items.border.leave-blank");
        int guiIndex = 0;
        ItemStack[] contents = getOwner().getInventory().getStorageContents();
        for (int originalSlot = 0; originalSlot < contents.length && guiIndex < guiSlots.size(); originalSlot++) {
            ItemStack item = contents[originalSlot];
            String skillId = getSkillId(item);
            if (skillId == null)
                continue;
            String normalizedId = skillId.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
            long reward = SkillBookContributeFile.get().getLong("skillbooks." + normalizedId + ".points", 0);
            if (reward <= 0)
                continue;

            ItemStack displayItem = item.clone();
            displayItem.setAmount(1);
            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            for (String line : fileConfiguration.getStringList("items.book-append-lore"))
                lore.add(BangHoi.nms.addColor(line.replace("%points%", String.valueOf(reward))));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(SLOT_KEY, PersistentDataType.INTEGER, originalSlot);
            displayItem.setItemMeta(meta);
            inventory.setItem(guiSlots.get(guiIndex++), displayItem);
        }

        if (guiIndex == 0) {
            ItemStack noBooks = BangHoi.nms.addCustomData(ItemUtil.getItem(
                    ItemType.valueOf(fileConfiguration.getString("items.no-books.type").toUpperCase()),
                    fileConfiguration.getString("items.no-books.value"),
                    fileConfiguration.getInt("items.no-books.customModelData"),
                    fileConfiguration.getString("items.no-books.name"),
                    fileConfiguration.getStringList("items.no-books.lore"), false), "no-books");
            inventory.setItem(fileConfiguration.getInt("items.no-books.slot"), noBooks);
        }
    }
}
