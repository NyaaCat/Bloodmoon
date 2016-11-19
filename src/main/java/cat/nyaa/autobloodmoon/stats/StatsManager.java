package cat.nyaa.autobloodmoon.stats;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class StatsManager {
    private final AutoBloodmoon plugin;

    public StatsManager(AutoBloodmoon pl) {
        plugin = pl;
    }

    public PlayerStats getPlayerStats(OfflinePlayer player) {
        if (!plugin.cfg.statsConfig.statsHashMap.containsKey(player.getUniqueId())) {
            plugin.cfg.statsConfig.statsHashMap.put(player.getUniqueId(), new PlayerStats(player));
        }
        return plugin.cfg.statsConfig.statsHashMap.get(player.getUniqueId());
    }

    public PlayerStats getPlayerStats(UUID player) {
        if (!plugin.cfg.statsConfig.statsHashMap.containsKey(player)) {
            plugin.cfg.statsConfig.statsHashMap.put(player, new PlayerStats(player));
        }
        return plugin.cfg.statsConfig.statsHashMap.get(player);
    }
}
