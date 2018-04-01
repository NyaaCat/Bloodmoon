package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobSpawnEvent;
import com.jacob_vejvoda.infernal_mobs.api.InfernalMobsAPI;
import com.jacob_vejvoda.infernal_mobs.persist.Mob;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.UUID;


public class MobListener implements Listener {
    private AutoBloodmoon plugin;

    public MobListener(AutoBloodmoon pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING) {
            Arena arena = plugin.currentArena;
            LivingEntity entity = event.getEntity();
            UUID mobUUID = entity.getUniqueId();
            boolean isInfernalMob = arena.currentLevelInfernalMobs.contains(mobUUID);
            boolean isNormalMob = arena.spawnedMobs.contains(mobUUID);
            if (!isInfernalMob && !isNormalMob && arena.inArena(entity.getLocation())) {
                Mob mob = InfernalMobsAPI.asInfernalMob(mobUUID);
                if (mob != null) {
                    isInfernalMob = true;
                    arena.currentLevelInfernalMobs.add(mobUUID);
                    arena.mobLevelMap.put(mobUUID, mob.abilityList.size());
                    arena.currentLevelSpawnedMobs.add(mobUUID);
                    arena.spawnedMobs.add(mobUUID);
                } else {
                    isNormalMob = true;
                }
            }
            if (isNormalMob || isInfernalMob) {
                Player killer = entity.getKiller();
                if (killer != null && arena.players.contains(killer.getUniqueId())) {
                    arena.scoreBoard.incNormalKill(killer);
                }
                if (isInfernalMob) {
                    if (killer != null && arena.players.contains(killer.getUniqueId())) {
                        arena.scoreBoard.incInfernalKill(killer, entity);
                    }
                    arena.currentLevelInfernalMobs.remove(mobUUID);
                    arena.broadcast(I18n.format("user.game.mobs_remaining", arena.currentLevelInfernalMobs.size()));
                }
                plugin.currentArena.spawnedMobs.remove(mobUUID);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (plugin.currentArena != null && plugin.currentArena.state != Arena.ArenaState.STOP && plugin.currentArena.inArena(event.getLocation())) {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                event.setCancelled(true);
            } else if (!(event.getEntity() instanceof ArmorStand)){
                plugin.currentArena.currentLevelSpawnedMobs.add(event.getEntity().getUniqueId());
                plugin.currentArena.spawnedMobs.add(event.getEntity().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInfernalMobSpawn(InfernalMobSpawnEvent event) {
        if (plugin.currentArena != null) {
            Arena arena = plugin.currentArena;
            UUID mobUUID = event.mobEntity.getUniqueId();
            if (arena.currentLevelInfernalMobs.contains(event.parentId) || arena.inArena(event.mobEntity.getLocation())) {
                arena.currentLevelInfernalMobs.add(mobUUID);
                arena.mobLevelMap.put(mobUUID, event.mob.abilityList.size());
                arena.currentLevelSpawnedMobs.add(mobUUID);
                arena.spawnedMobs.add(mobUUID);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onMobTeleport(EntityTeleportEvent event) {
        if (plugin.currentArena != null && plugin.currentArena.currentLevelInfernalMobs.contains(event.getEntity().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getWorld() != to.getWorld() || to.getBlock().getLightFromSky() < 1) {
                event.setCancelled(true);
            }
        }
    }
}
