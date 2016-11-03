package cat.nyaa.autobloodmoon.mobs;


import cat.nyaa.autobloodmoon.AutoBloodmoon;

import java.util.ArrayList;
import java.util.Random;

public class MobManager {
    private AutoBloodmoon plugin;

    public MobManager(AutoBloodmoon pl) {
        plugin = pl;
    }

    public Mob getMob(int level, int id) {
        if (plugin.cfg.mobConfig.mobList.containsKey(level) && id < plugin.cfg.mobConfig.mobList.get(level).size()) {
            return plugin.cfg.mobConfig.mobList.get(level).get(id);
        }
        return null;
    }

    public ArrayList<Mob> getMobs(int level) {
        if (plugin.cfg.mobConfig.mobList.containsKey(level)) {
            return plugin.cfg.mobConfig.mobList.get(level);
        }
        return null;
    }

    public Mob getRandomMob(int level) {
        if (plugin.cfg.mobConfig.mobList.containsKey(level)) {
            ArrayList<Mob> mobs = plugin.cfg.mobConfig.mobList.get(level);
            return mobs.get(new Random().nextInt(mobs.size()));
        }
        return null;
    }
}
