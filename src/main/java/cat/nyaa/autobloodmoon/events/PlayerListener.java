package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private AutoBloodmoon plugin;

    public PlayerListener(AutoBloodmoon pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING &&
                plugin.currentArena.players.contains(e.getPlayer().getUniqueId())) {
            plugin.currentArena.quit(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING &&
                plugin.currentArena.players.contains(e.getEntity().getUniqueId())) {
            plugin.currentArena.quit(e.getEntity());
        }
    }
}
