package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.ArenaConfig;
import cat.nyaa.autobloodmoon.level.LevelConfig;
import cat.nyaa.autobloodmoon.mobs.MobConfig;
import cat.nyaa.autobloodmoon.stats.StatsConfig;
import cat.nyaa.utils.ISerializable;
import cat.nyaa.utils.PluginConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends PluginConfigure {
    public AutoBloodmoon plugin;
    @Serializable
    public String language = "en_US";
    @Serializable
    public int call_timeout = 600;
    @Serializable
    public int preparation_time = 600;
    @Serializable
    public boolean save_inventory = false;
    @Serializable
    public boolean border_particle = true;
    @Serializable
    public int border_particle_height = 3;
    @Serializable
    public boolean pvp = false;
    @Serializable
    public int pvp_penalty_percent = 0;
    @Serializable
    public int pvp_penalty_max = 0;

    @StandaloneConfig
    public ArenaConfig arenaConfig;
    @StandaloneConfig
    public RewardConfig rewardConfig;
    @StandaloneConfig
    public MobConfig mobConfig;
    @StandaloneConfig
    public LevelConfig levelConfig;
    @StandaloneConfig
    public StatsConfig statsConfig;

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    public Configuration(AutoBloodmoon pl) {
        plugin = pl;
        arenaConfig = new ArenaConfig(plugin);
        rewardConfig = new RewardConfig(plugin);
        mobConfig = new MobConfig(plugin);
        levelConfig = new LevelConfig(plugin);
        statsConfig = new StatsConfig(plugin);
    }
}
