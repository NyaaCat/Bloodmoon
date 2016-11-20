package cat.nyaa.autobloodmoon.kits;


import cat.nyaa.utils.ISerializable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitItems implements ISerializable {
    @Serializable
    private List<ItemStack> items = new ArrayList<>();
    @Serializable
    private String kitName;
    @Serializable
    private KitType type;

    public KitItems() {

    }

    public KitItems(String kitName, KitType type, List<ItemStack> items) {
        this.kitName = kitName;
        this.type = type;
        this.items = items;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public KitType getType() {
        return type;
    }

    public void setType(KitType type) {
        this.type = type;
    }

    public List<ItemStack> getItems() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(ItemStack[] items) {
        if (this.items == null || !this.items.isEmpty()) {
            this.items = new ArrayList<>();
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                ItemStack item = items[i];
                if (!item.getType().equals(Material.AIR) && item.getAmount() > 0) {
                    this.items.add(item);
                }
            }
        }
    }

    public void setItems(List<ItemStack> items) {
        if (this.items == null || !this.items.isEmpty()) {
            this.items = new ArrayList<>();
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                ItemStack item = items.get(i);
                if (!item.getType().equals(Material.AIR) && item.getAmount() > 0) {
                    this.items.add(item);
                }
            }
        }
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }

    public static enum KitType {
        MOSTKILL,
        MOSTASSIST,
        MOSTNORMALKILL,
        MVP
    }
}


