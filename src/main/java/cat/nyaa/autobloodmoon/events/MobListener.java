package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;


public class MobListener implements Listener {
    public Location spawnLocation = null;
    public MobType mobType = MobType.NORMAL;
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
        if (plugin.currentArena != null && !plugin.currentArena.infernalMobs.isEmpty()) {
            if (plugin.currentArena.infernalMobs.contains(e.getEntity().getUniqueId())) {
                plugin.currentArena.infernalMobs.remove(e.getEntity().getUniqueId());
                plugin.currentArena.broadcast(I18n._("user.game.mobs_remaining",
                        plugin.currentArena.infernalMobs.size()));
            }
        }
    }

    public enum MobType {INFERNAL, NORMAL}
}
