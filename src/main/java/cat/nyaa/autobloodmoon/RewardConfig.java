package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.kits.KitConfig;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class RewardConfig extends FileConfigure {
    @Serializable(name = "normal_kill_point")
    public int normal_kill = 1;
    public HashMap<Integer, Integer> infernal_kill = new HashMap<>();   // Kill Infernal Mob money reward Map<Level,Money>
    public HashMap<Integer, Integer> infernal_assist = new HashMap<>();
    public Map<String, KitConfig> kits = new HashMap<>();
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
        ConfigurationSection killSec = config.getConfigurationSection("infernal_kill_point");
        if (killSec != null) {
            for (String key : killSec.getKeys(false)) {
                infernal_kill.put(Integer.parseInt(key), killSec.getInt(key));
            }
        }
        ConfigurationSection assistSec = config.getConfigurationSection("infernal_assist_point");
        if (assistSec != null) {
            for (String key : assistSec.getKeys(false)) {
                infernal_assist.put(Integer.parseInt(key), assistSec.getInt(key));
            }
        }
        ConfigurationSection kitsSec = config.getConfigurationSection("kits");
        if (kitsSec != null) {
            for (String kitName : kitsSec.getKeys(false)) {
                kits.put(kitName, KitConfig.fromConfig(kitsSec.getConfigurationSection(kitName)));
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        ConfigurationSection infernal = config.createSection("infernal_kill_point");
        for (Integer level : infernal_kill.keySet()) {
            infernal.set(Integer.toString(level), infernal_kill.get(level));
        }
        infernal = config.createSection("infernal_assist_point");
        for (Integer level : infernal_assist.keySet()) {
            infernal.set(Integer.toString(level), infernal_kill.get(level));
        }

        ConfigurationSection kitsData = config.createSection("kits");
        for (String kitName : kits.keySet()) {
            ConfigurationSection sec = kitsData.createSection(kitName);
            kits.get(kitName).serialize(sec);
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







