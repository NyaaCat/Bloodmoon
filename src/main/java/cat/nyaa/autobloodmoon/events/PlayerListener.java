package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerListener implements Listener {
    private AutoBloodmoon plugin;
    private Map<UUID, List<ItemStack>> vanishingCurseItems = new HashMap<>();

    public PlayerListener(AutoBloodmoon pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        vanishingCurseItems.clear();
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING &&
                plugin.currentArena.players.contains(e.getPlayer().getUniqueId())) {
            plugin.currentArena.quit(e.getPlayer());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING &&
                plugin.currentArena.players.contains(e.getEntity().getUniqueId())) {
            Player player = e.getEntity();
            plugin.currentArena.scoreBoard.incDeath(player);
            if (!plugin.currentArena.getWorld().equals(player.getWorld().getName())) {
                return;
            }
            if (player.getKiller() != null && player.getKiller() instanceof Player) {
                plugin.currentArena.scoreBoard.incPlayerKill(player.getKiller(), player);
                if (plugin.cfg.pvp_penalty_percent > 0 && plugin.cfg.pvp_penalty_max > 0 &&
                        VaultUtils.enoughMoney(player, 100)) {
                    int money = 0;
                    if (VaultUtils.balance(player) / 100 * plugin.cfg.pvp_penalty_percent >
                            plugin.cfg.pvp_penalty_max) {
                        money = plugin.cfg.pvp_penalty_max;
                    } else {
                        money = (int) VaultUtils.balance(player) / 100 * plugin.cfg.pvp_penalty_percent;
                    }
                    if (money > 1 && VaultUtils.withdraw(player.getKiller(), money)) {
                        VaultUtils.deposit(player, money);
                        player.sendMessage(I18n.format("user.prefix") +
                                I18n.format("user.pvp_penalty.message", money, player.getKiller().getName()));
                        player.getKiller().sendMessage(I18n.format("user.prefix") +
                                I18n.format("user.pvp_penalty.killer", player.getName(), money, player.getName()));
                    }
                }
            }
            if (!e.getKeepInventory() && plugin.cfg.save_inventory && e.getDrops() != null && (!e.getDrops().isEmpty() || vanishingCurseItems.containsKey(player.getUniqueId())) &&
                    player.getLocation().getY() > 5) {
                if (vanishingCurseItems.containsKey(player.getUniqueId())) {
                    if (e.getDrops().stream().filter(itemStack -> itemStack.containsEnchantment(Enchantment.VANISHING_CURSE)).count() <= 0L) {
                        e.getDrops().addAll(vanishingCurseItems.get(player.getUniqueId()));
                    }
                    vanishingCurseItems.remove(player.getUniqueId());
                }
                for (int y = 0; y < 255; y++) {
                    Location loc = player.getLocation().clone();
                    if (loc.getY() + y >= loc.getWorld().getMaxHeight()) {
                        break;
                    }
                    loc.setY(loc.getY() + y);
                    if (loc.getBlock().getType() == Material.AIR) {
                        Block block = loc.getBlock();
                        block.setType(getChestType(loc.getBlock()));
                        Chest chest = (Chest) block.getState();
                        try {
                            SimpleDateFormat format = new SimpleDateFormat(I18n.format("user.chest.date_format"));
                            String date = format.format(System.currentTimeMillis());
                            chest.setCustomName(I18n.format("user.chest.name", player.getName(), date));
                            chest.update();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        Inventory inventory = chest.getInventory();
                        HashMap<Integer, ItemStack> items = inventory.addItem(e.getDrops().toArray(new ItemStack[0]));
                        e.getDrops().clear();
                        e.getDrops().addAll(items.values());
                        plugin.coreProtectAPI.logPlacement(player, block.getLocation(), block.getType(), block.getBlockData());
                        if (e.getDrops().isEmpty()) {
                            break;
                        }
                    }
                }
            }
            plugin.currentArena.quit(e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResurrect(EntityResurrectEvent e) {
        if (e.getEntity() instanceof Player && e.isCancelled() && plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING &&
                plugin.currentArena.players.contains(e.getEntity().getUniqueId()) && plugin.cfg.save_inventory) {
            Player player = (Player) e.getEntity();
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR && item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                    stacks.add(item.clone());
                }
            }
            if (!stacks.isEmpty()) {
                vanishingCurseItems.put(player.getUniqueId(), stacks);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Player damager = null;
            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile &&
                    ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) e.getDamager()).getShooter();
            }
            if (damager != null) {
                if (damager.getUniqueId().equals(player.getUniqueId())) {
                    return;
                }
                if ((plugin.tempPVPProtection.containsKey(player.getUniqueId()) &&
                        plugin.tempPVPProtection.get(player.getUniqueId()) >= System.currentTimeMillis()) ||
                        (plugin.tempPVPProtection.containsKey(damager.getUniqueId()) &&
                                plugin.tempPVPProtection.get(damager.getUniqueId()) >= System.currentTimeMillis())) {
                    e.setCancelled(true);
                    return;
                }
                if (!plugin.cfg.pvp && plugin.currentArena != null && (plugin.currentArena.state == Arena.ArenaState.PLAYING || plugin.currentArena.state == Arena.ArenaState.WAIT)) {
                    if (plugin.currentArena.players.contains(player.getUniqueId()) || plugin.currentArena.players.contains(damager.getUniqueId())) {
                        if (plugin.currentArena.getWorld().equals(player.getWorld().getName())) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (plugin.currentArena == null || plugin.currentArena.state == Arena.ArenaState.STOP) {
            return;
        }
        Player p = e.getPlayer();
        if (!plugin.currentArena.players.contains(p.getUniqueId()) && plugin.currentArena.inArena(e.getTo())) {
            plugin.currentArena.join(p, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.currentArena != null) {
            Arena arena = plugin.currentArena;
            if (arena.state != Arena.ArenaState.STOP && !arena.players.contains(event.getPlayer().getUniqueId()) && arena.inArena(event.getTo())) {
                arena.join(event.getPlayer(), false);
            }
        }
    }
}
