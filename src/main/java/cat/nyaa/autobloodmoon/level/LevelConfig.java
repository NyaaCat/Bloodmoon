package cat.nyaa.autobloodmoon.level;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class LevelConfig extends FileConfigure {
    public HashMap<String, Level> levels = new HashMap<>();
    private AutoBloodmoon plugin;

    public LevelConfig(AutoBloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "level.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("levels")) {
            ConfigurationSection levels = config.getConfigurationSection("levels");
            for (String k : levels.getKeys(false)) {
                Level level = new Level();
                level.deserialize(levels.getConfigurationSection(k));
                this.levels.put(k, level);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("levels", null);
        ConfigurationSection levels = config.createSection("levels");
        for (String k : this.levels.keySet()) {
            this.levels.get(k).serialize(levels.createSection(k));
        }

    }
}
