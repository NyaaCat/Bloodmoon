package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.ArenaConfig;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

public class Configuration implements ISerializable {
    public AutoBloodmoon plugin;
    @Serializable
    public String language = "en_US";
    @Serializable
    public long call_timeout = 600;
    @Serializable
    public int preparation_time = 600;
    @Serializable
    public boolean save_inventory = false;
    
    public ArenaConfig arenaConfig;

    public Configuration(AutoBloodmoon pl) {
        plugin = pl;
        arenaConfig = new ArenaConfig(plugin); 
    }

    public void save() {
        serialize(plugin.getConfig());
        plugin.saveConfig();
    }

    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        arenaConfig.load();
    }

    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        arenaConfig.save();
    }

}
