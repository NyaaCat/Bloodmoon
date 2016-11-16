package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.ArenaConfig;
import cat.nyaa.autobloodmoon.level.LevelConfig;
import cat.nyaa.autobloodmoon.mobs.MobConfig;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

public class Configuration implements ISerializable {
    public AutoBloodmoon plugin;
    @Serializable
    public String language = "en_US";
    @Serializable
    public int call_timeout = 600;
    @Serializable
    public int preparation_time = 600;
    @Serializable
    public boolean save_inventory = false;

    public ArenaConfig arenaConfig;
    public RewardConfig rewardConfig;
    public MobConfig mobConfig;
    public LevelConfig levelConfig;

    public Configuration(AutoBloodmoon pl) {
        plugin = pl;
        arenaConfig = new ArenaConfig(plugin);
        rewardConfig = new RewardConfig(plugin);
        mobConfig = new MobConfig(plugin);
        levelConfig = new LevelConfig(plugin);
    }

    public void save() {
        serialize(plugin.getConfig());
        plugin.saveConfig();
    }

    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        arenaConfig.load();
        rewardConfig.load();
        mobConfig.load();
        levelConfig.load();
    }

    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        arenaConfig.save();
        rewardConfig.save();
        mobConfig.save();
        levelConfig.save();
    }

}
