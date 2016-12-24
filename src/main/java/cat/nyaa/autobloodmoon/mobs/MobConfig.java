package cat.nyaa.autobloodmoon.mobs;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MobConfig extends FileConfigure {
    public HashMap<Integer, ArrayList<Mob>> mobList = new HashMap<>();
    @Serializable
    public ArrayList<String> normalMob = new ArrayList<>(Arrays.asList(new String[]{"zombie", "creeper"}));
    private AutoBloodmoon plugin;

    public MobConfig(AutoBloodmoon plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "mob.yml";
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
                ArrayList<Mob> mobs = new ArrayList<>();
                ConfigurationSection data = levels.getConfigurationSection(k);
                for (String id : data.getKeys(false)) {
                    Mob mob = new Mob();
                    mob.deserialize(data.getConfigurationSection(id));
                    mobs.add(mob);
                }
                this.mobList.put(Integer.valueOf(k), mobs);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("levels", null);
        ConfigurationSection levels = config.createSection("levels");
        for (int k : mobList.keySet()) {
            ConfigurationSection data = levels.createSection(String.valueOf(k));
            List<Mob> mobs = mobList.get(k);
            for (int i = 0; i < mobs.size(); i++) {
                mobs.get(i).serialize(data.createSection(String.valueOf(i)));
            }
        }
    }
}
