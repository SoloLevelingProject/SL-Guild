package com.banghoi.inventory;

import com.banghoi.BangHoi;
import com.banghoi.Settings;
import com.banghoi.api.enums.ItemType;
import com.banghoi.api.enums.Subject;
import com.banghoi.clan.subject.SetIcon;
import com.banghoi.file.inventory.SetIconMaterialListInventoryFile;
import com.banghoi.language.Messages;
import com.banghoi.storage.PluginDataManager;
import com.banghoi.util.ItemUtil;
import com.banghoi.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SetIconMaterialListInventory extends PaginatedInventory {

    FileConfiguration fileConfiguration = SetIconMaterialListInventoryFile.get();
    private List<String> materials = new ArrayList<>();

    public SetIconMaterialListInventory(Player owner) {
        super(owner);
    }

    @Override
    public void open() {
        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return;
        }
        super.open();
    }

    @Override
    public String getMenuName() {
        String title = fileConfiguration.getString("title");
        title = title.replace("%search%", getSearch() != null ? fileConfiguration.getString("title-placeholders.search").replace("%search%", getSearch()) : "");
        return BangHoi.nms.addColor(title);
    }

    @Override
    public int getSlots() {
        int rows = fileConfiguration.getInt("rows") * 9;
        if (rows < 27 || rows > 54)
            return 54;
        return rows;
    }

    @Override
    public boolean handleMenu(InventoryClickEvent event) {
        if (!super.handleMenu(event))
            return false;

        if (PluginDataManager.getClanDatabaseByPlayerName(getOwner().getName()) == null) {
            MessageUtil.sendMessage(getOwner(), Messages.MUST_BE_IN_CLAN);
            getOwner().closeInventory();
            return false;
        }

        ItemStack itemStack = event.getCurrentItem();
        String itemCustomData = BangHoi.nms.getCustomData(itemStack);

        playClickSound(fileConfiguration, itemCustomData);

        if (itemCustomData.equals("prevPage")) {
            if (getPage() != 0) {
                setPage(getPage() - 1);
                open();
            }
        }
        if (itemCustomData.equals("nextPage")) {
            if (!((index + 1) >= materials.size())) {
                setPage(getPage() + 1);
                open();
            } else {
                MessageUtil.sendMessage(getOwner(), Messages.LAST_PAGE);
            }
        }
        if (itemCustomData.equals("close"))
            getOwner().closeInventory();
        if (itemCustomData.equals("back"))
            new SetIconMenuInventory(getOwner()).open();
        if (itemCustomData.contains("value=")) {
            playClickSound(fileConfiguration, "material");
            new SetIcon(Settings.CLAN_SETTING_PERMISSION_DEFAULT.get(Subject.SETICON), getOwner(), getOwner().getName(), ItemType.MATERIAL, itemCustomData.replace("value=", "")).execute();
        }

        return true;
    }

    @Override
    public void setMenuItems() {
        BangHoi.support.getFoliaLib().getScheduler().runAsync(task -> {

            addPaginatedMenuItems(fileConfiguration, true);

            if (PluginDataManager.getClanDatabase().isEmpty())
                return;

            materials.clear();

            for (Material material : Material.values()) {
                if (material == Material.AIR)
                    continue;
                materials.add(material.toString().toUpperCase());
            }

            if (getSearch() != null) {
                List<String> newMaterials = new ArrayList<>();
                for (String material : materials) {
                    if (material.toLowerCase().contains(getSearch().toLowerCase())) {
                        newMaterials.add(material);
                    }
                }
                materials.clear();
                materials.addAll(newMaterials);
            }

            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * getPage() + i;
                if (index >= materials.size())
                    break;
                if (materials.get(index) != null) {
                    try {
                        ItemStack materialItem = ItemUtil.getItem(
                                ItemType.MATERIAL,
                                materials.get(index),
                                0,
                                fileConfiguration.getString("items.material.name"),
                                fileConfiguration.getStringList("items.material.lore"), false);
                        ItemStack itemStack = BangHoi.nms.addCustomData(materialItem, "value=" + materials.get(index));
                        inventory.addItem(itemStack);
                    } catch (IllegalArgumentException exception) {
                        inventory.addItem(BangHoi.nms.addCustomData(ItemUtil.getItem(
                                ItemType.valueOf(fileConfiguration.getString("items.unavailableMaterial.type").toUpperCase()),
                                fileConfiguration.getString("items.unavailableMaterial.value"),
                                0,
                                fileConfiguration.getString("items.unavailableMaterial.name"),
                                fileConfiguration.getStringList("items.unavailableMaterial.lore"),
                                false),
                                String.valueOf(Math.random() // random custom data to avoid items being duplicated
                                )));
                        // ignore, some argument is not an item to be added to the inventory.
                    }
                }
            }
        });
    }
}
