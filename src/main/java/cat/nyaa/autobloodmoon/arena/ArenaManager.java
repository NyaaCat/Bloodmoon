package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import org.bukkit.Location;

public class ArenaManager {
    private final AutoBloodmoon plugin;

    public ArenaManager(AutoBloodmoon pl) {
        plugin = pl;
    }

    public boolean createArena(String name, Location loc, int radius, int spawnRadius) {
        Arena arena = new Arena();
        arena.setName(name);
        arena.setCenterPoint(loc);
        arena.setRadius(radius);
        arena.setSpawnRadius(spawnRadius);
        plugin.cfg.arenaConfig.arenaList.put(name, arena);
        plugin.cfg.arenaConfig.save();
        return true;
    }

    public boolean removeArena(String name) {
        if (plugin.cfg.arenaConfig.arenaList.containsKey(name)) {
            plugin.cfg.arenaConfig.arenaList.remove(name);
            plugin.cfg.arenaConfig.save();
            return true;
        }
        return false;
    }

    public Arena getArena(Location loc) {
        for (String key : plugin.cfg.arenaConfig.arenaList.keySet()) {
            Arena arena = plugin.cfg.arenaConfig.arenaList.get(key);
            if (loc.getWorld().getName().equals(arena.getWorld())) {
                if (loc.distance(arena.getCenterPoint()) <= arena.getRadius()) {
                    return arena;
                }
            }
        }
        return null;
    }

    public Arena getArena(String name) {
        if (plugin.cfg.arenaConfig.arenaList.containsKey(name)) {
            return plugin.cfg.arenaConfig.arenaList.get(name);
        }
        return null;
    }
}


