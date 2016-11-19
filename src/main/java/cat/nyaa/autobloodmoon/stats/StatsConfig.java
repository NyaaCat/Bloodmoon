package cat.nyaa.autobloodmoon.stats;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class StatsConfig extends FileConfigure {
    public HashMap<UUID, PlayerStats> statsHashMap = new HashMap<>();
    private AutoBloodmoon plugin;

    public StatsConfig(AutoBloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "stats.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("stats")) {
            ConfigurationSection stats = config.getConfigurationSection("stats");
            for (String k : stats.getKeys(false)) {
                ConfigurationSection data = stats.getConfigurationSection(k);
                PlayerStats playerStats = new PlayerStats();
                playerStats.deserialize(data);
                statsHashMap.put(playerStats.getUUID(), playerStats);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("stats", null);
        ConfigurationSection stats = config.createSection("stats");
        for (UUID k : statsHashMap.keySet()) {
            ConfigurationSection data = stats.createSection(k.toString());
            PlayerStats playerStats = statsHashMap.get(k);
            playerStats.serialize(data);
        }
    }
}
