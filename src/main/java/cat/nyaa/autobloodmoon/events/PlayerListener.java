package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

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
            Player player = e.getEntity();
            if (plugin.cfg.save_inventory && e.getDrops() != null && !e.getDrops().isEmpty() &&
                    player.getLocation().getY() > 5) {
                for (int y = 0; y < 32; y++) {
                    Location loc = player.getLocation().clone();
                    loc.setY(loc.getY() + y);
                    if (loc.getBlock().getType() == Material.AIR) {
                        Block block = loc.getBlock();
                        block.setType(getChestType(loc.getBlock()));
                        Chest chest = (Chest) block.getState();
                        Inventory inventory = chest.getInventory();
                        HashMap items = inventory.addItem((ItemStack[]) e.getDrops().toArray(new ItemStack[0]));
                        e.getDrops().clear();
                        e.getDrops().addAll(items.values());
                        if (e.getDrops().isEmpty()) {
                            e.setKeepInventory(false);
                            break;
                        }
                    }
                }
            }
            plugin.currentArena.quit(e.getEntity());
        }
    }
    
        public static Material getChestType(Block block) {
        Material material;
        if (block.getX() % 2 == 0) {
            material = Material.TRAPPED_CHEST;
        } else {
            material = Material.CHEST;
        }
        if (block.getY() % 2 == 0) {
            material = material.equals(Material.CHEST) ? Material.TRAPPED_CHEST : Material.CHEST;
        }
        if (block.getZ() % 2 == 0) {
            material = material.equals(Material.TRAPPED_CHEST) ? Material.CHEST : Material.TRAPPED_CHEST;
        }
        return material;
    }
}
