package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            plugin.currentArena.getPlayerStats(player).incrementStats(PlayerStats.StatsType.DEATH);
            if (player.getKiller() != null &&
                    plugin.currentArena.players.contains(player.getKiller().getUniqueId())) {
                if (plugin.cfg.pvp_penalty_percent > 0 && plugin.cfg.pvp_penalty_max > 0 &&
                        plugin.vaultUtil.enoughMoney(player, 100)) {
                    int money = 0;
                    if (plugin.vaultUtil.balance(player) / 100 * plugin.cfg.pvp_penalty_percent >
                            plugin.cfg.pvp_penalty_max) {
                        money = plugin.cfg.pvp_penalty_max;
                    } else {
                        money = (int) plugin.vaultUtil.balance(player) / 100 * plugin.cfg.pvp_penalty_percent;
                    }
                    if (money > 1 && plugin.vaultUtil.withdraw(player.getKiller(), money)) {
                        plugin.vaultUtil.deposit(player, money);
                        player.sendMessage(I18n._("user.prefix") +
                                I18n._("user.pvp_penalty.message", money, player.getKiller().getName()));
                        player.getKiller().sendMessage(I18n._("user.prefix") +
                                I18n._("user.pvp_penalty.killer", player.getName(), money, player.getName()));
                    }
                }
            }
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!plugin.cfg.pvp && plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING) {
            if (e.getEntity() instanceof Player) {
                Player damager = null;
                if (e.getDamager() instanceof Player) {
                    damager = (Player) e.getDamager();
                } else if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) e.getDamager()).getShooter();
                }
                if (plugin.currentArena.players.contains(e.getEntity().getUniqueId()) ||
                        (damager != null && plugin.currentArena.players.contains(damager.getUniqueId()))) {
                    e.setCancelled(true);
                }
            }
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
