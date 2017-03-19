package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;


public class MobListener implements Listener {
    public Location spawnLocation = null;
    public MobType mobType = MobType.NORMAL;
    public int mobLevel = 0;
    private AutoBloodmoon plugin;

    public MobListener(AutoBloodmoon pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (plugin.currentArena != null && spawnLocation != null) {
            Entity entity = e.getEntity();
            if (spawnLocation.equals(e.getLocation())) {
                if (mobType == MobType.INFERNAL) {
                    plugin.currentArena.infernalMobs.add(entity.getUniqueId());
                    plugin.currentArena.mobLevelMap.put(entity.getUniqueId(), mobLevel);
                } else {
                    entity.setMetadata("NPC", new FixedMetadataValue(plugin, 1));
                }
                plugin.currentArena.normalMobs.add(entity.getUniqueId());
                plugin.currentArena.entityList.add(entity.getUniqueId());
                spawnLocation = null;
                mobType = MobType.NORMAL;
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING) {
            Arena arena = plugin.currentArena;
            if (arena.entityList.contains(e.getEntity().getUniqueId())) {
                LivingEntity mob = e.getEntity();
                Player player = null;
                if (mob.getKiller() != null &&
                        arena.players.contains(mob.getKiller().getUniqueId())) {
                    player = mob.getKiller();
                    plugin.currentArena.scoreBoard.incNormalKill(player);
                }
                if (arena.infernalMobs.contains(mob.getUniqueId())) {
                    plugin.currentArena.scoreBoard.incInfernalKill(player, mob);
                    arena.infernalMobs.remove(mob.getUniqueId());
                    arena.broadcast(I18n._("user.game.mobs_remaining", arena.infernalMobs.size()));
                }
            }
        }
    }

    public enum MobType {INFERNAL, NORMAL}
}
