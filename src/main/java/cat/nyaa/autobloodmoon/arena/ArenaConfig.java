package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class ArenaConfig extends FileConfigure {
    public HashMap<String, Arena> arenaList = new HashMap<>();
    private AutoBloodmoon plugin;

    public ArenaConfig(AutoBloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "arena.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                ConfigurationSection data = config.getConfigurationSection(key);
                Arena arena = new Arena();
                arena.deserialize(data);
                arenaList.put(arena.getName(), arena);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        for (String key : arenaList.keySet()) {
            Arena arena = arenaList.get(key);
            ConfigurationSection data = config.createSection(arena.getName());
            arena.serialize(data);
        }
    }
}






