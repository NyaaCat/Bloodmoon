package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.kits.KitItems;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class RewardConfig extends FileConfigure {
    @Serializable(name = "normal.kill")
    public int normal_kill = 1;
    public HashMap<Integer, Integer> infernal_kill = new HashMap<>();   // Kill Infernal Mob money reward Map<Level,Money>
    public HashMap<Integer, Integer> infernal_assist = new HashMap<>();
    public HashMap<String, HashMap<KitItems.KitType, KitItems>> kits = new HashMap<>();
    private AutoBloodmoon plugin;

    public RewardConfig(AutoBloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "reward.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("infernal")) {
            ConfigurationSection infernal = config.getConfigurationSection("infernal");
            for (String k : infernal.getKeys(false)) {
                ConfigurationSection level = infernal.getConfigurationSection(k);
                infernal_kill.put(Integer.valueOf(k), level.getInt("kill"));
                infernal_assist.put(Integer.valueOf(k), level.getInt("assist"));
            }
        }
        if (config.isConfigurationSection("kits")) {
            ConfigurationSection kitsData = config.getConfigurationSection("kits");
            for (String kitName : kitsData.getKeys(false)) {
                for (String type : kitsData.getConfigurationSection(kitName).getKeys(false)) {
                    KitItems kit = new KitItems();
                    kit.deserialize(kitsData.getConfigurationSection(kitName).getConfigurationSection(type));
                    if (!this.kits.containsKey(kitName)) {
                        this.kits.put(kitName, new HashMap<>());
                    }
                    this.kits.get(kitName).put(kit.getType(), kit);
                }
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("infernal", null);
        ConfigurationSection infernal = config.createSection("infernal");
        for (int k : infernal_kill.keySet()) {
            ConfigurationSection level = infernal.createSection(String.valueOf(k));
            level.set("kill", infernal_kill.get(k));
            if (infernal_kill.containsKey(k)) {
                level.set("assist", infernal_assist.get(k));
            }
        }
        ConfigurationSection kitsData = config.createSection("kits");
        for (String kitName : kits.keySet()) {
            kitsData.set(kitName, null);
            ConfigurationSection data = kitsData.createSection(kitName);
            HashMap<KitItems.KitType, KitItems> k = kits.get(kitName);
            for (KitItems.KitType kitType : k.keySet()) {
                KitItems kitItems = k.get(kitType);
                kitItems.serialize(data.createSection(kitType.name()));
            }
        }
    }

    public int getNormalBonus(BonusType type) {
        if (type == BonusType.KILL) {
            return normal_kill;
        } else {
            return 0;
        }
    }

    public int getInfernalBonus(BonusType type, int level) {
        if (type == BonusType.KILL) {
            if (infernal_kill.containsKey(level)) {
                return infernal_kill.get(level);
            }
        } else if (type == BonusType.ASSIST) {
            if (infernal_assist.containsKey(level)) {
                return infernal_assist.get(level);
            }
        }
        return 0;
    }

    public enum BonusType {KILL, ASSIST}
}







