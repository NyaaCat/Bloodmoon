package cat.nyaa.autobloodmoon.level;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class LevelConfig extends FileConfigure {
    public HashMap<Level.LevelType, Level> levels = new HashMap<>();
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
            ConfigurationSection levels = config.getConfigurationSection("infernal");
            for (String k : levels.getKeys(false)) {
                Level level = new Level();
                level.deserialize(config.getConfigurationSection(k));
                this.levels.put(level.getLevelType(), level);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("levels", null);
        ConfigurationSection levels = config.createSection("levels");
        for (Level.LevelType k : this.levels.keySet()) {
            this.levels.get(k).serialize(levels.createSection(String.valueOf(k)));
        }

    }
}
