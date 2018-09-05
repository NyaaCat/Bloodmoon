package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitConfig implements ISerializable {
    @Serializable(name = "MOSTKILL")
    public List<ItemStack> kitMostKill = new ArrayList<>();
    @Serializable(name = "MOSTASSIST")
    public List<ItemStack> kitMostAssist = new ArrayList<>();
    @Serializable(name = "MOSTNORMALKILL")
    public List<ItemStack> kitMostNormal = new ArrayList<>();
    @Serializable(name = "MVP")
    public List<ItemStack> kitMvp = new ArrayList<>();
    @Serializable(name = "TEAM")
    public List<ItemStack> kitTeam = new ArrayList<>();

    public static KitConfig fromConfig(ConfigurationSection sec) {
        KitConfig ret = new KitConfig();
        ret.deserialize(sec);
        return ret;
    }

    public void setKit(KitType type, ItemStack[] content) {
        List<ItemStack> clonedArray = new ArrayList<>();
        if (content != null) {
            for (ItemStack i : content) {
                if (i == null) continue;
                clonedArray.add(i.clone());
            }
        }
        switch (type) {
            case MOSTKILL:
                kitMostKill = clonedArray;
                break;
            case MOSTASSIST:
                kitMostAssist = clonedArray;
                break;
            case MOSTNORMALKILL:
                kitMostNormal = clonedArray;
                break;
            case MVP:
                kitMvp = clonedArray;
                break;
            case TEAM:
                kitTeam = clonedArray;
                break;
        }
    }

    public List<ItemStack> getKit(KitType type) {
        List<ItemStack> items;
        switch (type) {
            case MOSTKILL:
                items = kitMostKill;
                break;
            case MOSTASSIST:
                items = kitMostAssist;
                break;
            case MOSTNORMALKILL:
                items = kitMostNormal;
                break;
            case MVP:
                items = kitMvp;
                break;
            case TEAM:
                items = kitTeam;
                break;
            default:
                throw new IllegalArgumentException("Unknown KitType: " + type);
        }
        List<ItemStack> ret = new ArrayList<>();
        if (items == null) return null;
        for (ItemStack i : items) {
            ret.add(i.clone());
        }
        return ret;
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        if (Bukkit.getVersion().contains("MC: 1.12")) {
            config.set("nbt_kitMostKill", ItemStackUtils.itemsToBase64(kitMostKill));
            config.set("nbt_kitMostAssist", ItemStackUtils.itemsToBase64(kitMostAssist));
            config.set("nbt_kitMostNormal", ItemStackUtils.itemsToBase64(kitMostNormal));
            config.set("nbt_kitMvp", ItemStackUtils.itemsToBase64(kitMvp));
            config.set("nbt_kitTeam", ItemStackUtils.itemsToBase64(kitTeam));
        }
        config.set("nbt_version", Bukkit.getVersion());
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        if (Bukkit.getVersion().contains("MC: 1.13")) {
            if (config.getString("nbt_version", Bukkit.getVersion()).contains("MC: 1.12")) {
                kitMostKill = config.getString("nbt_kitMostKill", "").length() > 0 ? ItemStackUtils.itemsFromBase64(config.getString("nbt_kitMostKill")) : new ArrayList<>();
                kitMostAssist = config.getString("nbt_kitMostAssist", "").length() > 0 ? ItemStackUtils.itemsFromBase64(config.getString("nbt_kitMostAssist")) : new ArrayList<>();
                kitMostNormal = config.getString("nbt_kitMostNormal", "").length() > 0 ? ItemStackUtils.itemsFromBase64(config.getString("nbt_kitMostNormal")) : new ArrayList<>();
                kitMvp = config.getString("nbt_kitMvp", "").length() > 0 ? ItemStackUtils.itemsFromBase64(config.getString("nbt_kitMvp")) : new ArrayList<>();
                kitTeam = config.getString("nbt_kitTeam", "").length() > 0 ? ItemStackUtils.itemsFromBase64(config.getString("nbt_kitTeam")) : new ArrayList<>();
            }
        }
    }

    public enum KitType {
        MOSTKILL,
        MOSTASSIST,
        MOSTNORMALKILL,
        MVP,
        TEAM
    }
}
