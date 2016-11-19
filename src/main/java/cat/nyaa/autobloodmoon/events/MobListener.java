package cat.nyaa.autobloodmoon.events;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.RewardConfig;
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
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.PLAYING) {
            Arena arena = plugin.currentArena;
            if (arena.entityList.contains(e.getEntity().getUniqueId())) {
                LivingEntity mob = e.getEntity();
                Player player = null;
                if (mob.getKiller() != null &&
                        arena.players.contains(mob.getKiller().getUniqueId())) {
                    player = mob.getKiller();
                    arena.getPlayerStats(player).incrementStats(PlayerStats.StatsType.NORMAL_KILL);
                    int bonus = plugin.cfg.rewardConfig.getNormalBonus(RewardConfig.BonusType.KILL);
                    if (bonus > 0) {
                        plugin.vaultUtil.deposit(player, bonus);
                        player.sendMessage(I18n._("user.prefix") +
                                I18n._("user.reward.normal_kill", bonus));
                    }
                }
                if (arena.infernalMobs.contains(mob.getUniqueId())) {
                    if (player != null) {
                        arena.getPlayerStats(player).incrementStats(PlayerStats.StatsType.INFERNAL_KILL);
                        int bonus = plugin.cfg.rewardConfig.getInfernalBonus(RewardConfig.BonusType.KILL,
                                arena.currentLevel);
                        if (bonus > 0) {
                            plugin.vaultUtil.deposit(player, bonus);
                            player.sendMessage(I18n._("user.prefix") +
                                    I18n._("user.reward.infernal_kill", bonus));
                            if (plugin.nyaaUtils.dsListener.entityList.getUnchecked(mob.getUniqueId()) != null) {
                                Map<UUID, Double> assistList = plugin.nyaaUtils.dsListener.
                                        entityList.getUnchecked(mob.getUniqueId());
                                for (UUID k : assistList.keySet()) {
                                    if (k != player.getUniqueId() && arena.players.contains(k)
                                            && assistList.get(k) >= 5.0D) {
                                        Player assistPlayer = Bukkit.getPlayer(k);
                                        int assistBonus = plugin.cfg.rewardConfig.getInfernalBonus(
                                                RewardConfig.BonusType.ASSIST, arena.currentLevel);
                                        if (assistBonus > 0) {
                                            arena.getPlayerStats(assistPlayer).incrementStats(
                                                    PlayerStats.StatsType.ASSIST);
                                            plugin.vaultUtil.deposit(assistPlayer, assistBonus);
                                            assistPlayer.sendMessage(I18n._("user.prefix") +
                                                    I18n._("user.reward.infernal_assist", assistBonus));
                                        }
                                    }
                                }
                            }
                        }
                        arena.infernalMobs.remove(mob.getUniqueId());
                        arena.broadcast(I18n._("user.game.mobs_remaining", arena.infernalMobs.size()));
                    }
                }
            }
        }
    }

    public enum MobType {INFERNAL, NORMAL}
}
