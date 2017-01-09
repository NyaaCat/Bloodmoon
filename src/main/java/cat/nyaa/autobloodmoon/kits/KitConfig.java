package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitConfig implements ISerializable{
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

    public enum KitType {
        MOSTKILL,
        MOSTASSIST,
        MOSTNORMALKILL,
        MVP,
        TEAM
    }

    public static KitConfig fromConfig(ConfigurationSection sec) {
        KitConfig ret = new KitConfig();
        ret.deserialize(sec);
        return ret;
    }

    public void setKit(KitType type, ItemStack[] content) {
        List<ItemStack> clonedArray = new ArrayList<>();
        if (content != null) {
            for (ItemStack i : content) {
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
}
