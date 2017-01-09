package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.kits.KitConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Temporary score storage for each game
 */
public class GameScoreBoard {
    private final AutoBloodmoon plugin;
    private final Arena arena;

    private final Set<UUID> fishermen = new HashSet<>();
    private final Map<UUID, Double> scoreMap = new HashMap<>();
    private final Map<Integer, Set<UUID>> activeMap = new HashMap<>();
    private final Map<KitConfig.KitType, Map<UUID, Integer>> statMap = new HashMap<>();

    public GameScoreBoard(AutoBloodmoon plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void incNormalKill(Player p) {
        incScore(p.getUniqueId(), plugin.cfg.rewardConfig.normal_kill);
        incStat(p.getUniqueId(), KitConfig.KitType.MOSTNORMALKILL);
        incActive(p.getUniqueId(), arena.currentLevel);
    }

    public void incInfernalKill(Player killer, LivingEntity infernalMob) {
        UUID killerId = killer == null? null: killer.getUniqueId();
        Integer infernalLevel = arena.mobLevelMap.get(infernalMob.getUniqueId());
        if (infernalLevel == null) return;
        Double score = new Double(plugin.cfg.rewardConfig.infernal_kill.get(infernalLevel));
        if (score == null) score = 0D;
        if (killer != null) {
            incScore(killerId, score);
            incStat(killerId, KitConfig.KitType.MOSTKILL);
            incActive(killerId, arena.currentLevel);
        }

        Map<UUID, Double> damageMap = plugin.damageStatistic.getDamagePlayerList(infernalMob.getUniqueId());
        Double totalDamageExcludeKiller = 0D;
        for (Map.Entry<UUID, Double> e : damageMap.entrySet()) {
            if (!e.getKey().equals(killerId)) {
                totalDamageExcludeKiller += e.getValue();
            }
        }

        for (Map.Entry<UUID, Double> e : damageMap.entrySet()) {
            if (!e.getKey().equals(killerId)) {
                incScore(e.getKey(), score*e.getValue()/totalDamageExcludeKiller);
                incStat(e.getKey(), KitConfig.KitType.MOSTASSIST);
            }
        }
    }

    public void incPlayerKill(Player killer, Player victim) {
        fishermen.add(killer.getUniqueId());
        fishermen.add(victim.getUniqueId());
    }

    private void incScore(UUID id, double score) {
        if (scoreMap.containsKey(id)) {
            scoreMap.put(id, scoreMap.get(id) + score);
        } else {
            scoreMap.put(id, score);
        }
    }

    private void incStat(UUID id, KitConfig.KitType type) {
        Map<UUID, Integer> map = statMap.get(type);
        if (map == null) {
            map = new HashMap<>();
            statMap.put(type, map);
        }
        if (map.containsKey(id)) {
            map.put(id, map.get(id) + 1);
        } else {
            map.put(id, 1);
        }
    }

    public void incActive(UUID id, int waveNo) {
        Set<UUID> activeSet = activeMap.get(waveNo);
        if (activeSet == null) {
            activeSet = new HashSet<>();
            activeMap.put(waveNo, activeSet);
        }
        activeSet.add(id);
    }

    public void computeInactivePlayers() {
        for (int level = 1; level <= arena.level.getMaxInfernalLevel(); level++) {
            if (!activeMap.containsKey(level)) {
                activeMap.put(level, new HashSet<>());
            }
        }
        for (UUID pid : arena.players) {
            boolean lastInactive = !activeMap.get(1).contains(pid);
            for (int level = 2; level <= arena.level.getMaxInfernalLevel(); level++) {
                if (activeMap.get(level).contains(pid)) {
                    lastInactive = false;
                } else {
                    if (lastInactive) { // fisherman
                        fishermen.add(pid);
                        break;
                    } else {
                        lastInactive = true;
                    }
                }
            }
        }
    }

    public UUID getMVP() {
        return scoreMap.entrySet().stream()
                .filter(e->!fishermen.contains(e.getKey()))
                .filter(e->arena.players.contains(e.getKey()))
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private int compairByScore(UUID u1, UUID u2) {
        Double s1 = scoreMap.containsKey(u1)? scoreMap.get(u1): 0D;
        Double s2 = scoreMap.containsKey(u2)? scoreMap.get(u2): 0D;
        return s1.compareTo(s2);
    }

    public UUID getMaxInfernalKill() {
        if (statMap.get(KitConfig.KitType.MOSTKILL) == null) return null;
        return statMap.get(KitConfig.KitType.MOSTKILL).entrySet().stream()
                .filter(e->!fishermen.contains(e.getKey()))
                .filter(e->arena.players.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue())?
                        compairByScore(e2.getKey(), e1.getKey()):
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public UUID getMaxNormalKill() {
        if (statMap.get(KitConfig.KitType.MOSTNORMALKILL) == null) return null;
        return statMap.get(KitConfig.KitType.MOSTNORMALKILL).entrySet().stream()
                .filter(e->!fishermen.contains(e.getKey()))
                .filter(e->arena.players.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue())?
                        compairByScore(e2.getKey(), e1.getKey()):
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public UUID getMaxAssist() {
        if (statMap.get(KitConfig.KitType.MOSTASSIST) == null) return null;
        return statMap.get(KitConfig.KitType.MOSTNORMALKILL).entrySet().stream()
                .filter(e->!fishermen.contains(e.getKey()))
                .filter(e->arena.players.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue())?
                        compairByScore(e2.getKey(), e1.getKey()):
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public Map<UUID, Double> getScores() {
        Map<UUID, Double> ret = new HashMap<>();
        for (UUID id : scoreMap.keySet()) {
            if (!fishermen.contains(id) && arena.players.contains(id)) {
                ret.put(id, scoreMap.get(id));
            }
        }
        return ret;
    }

    public Set<UUID> getFishermen() {
        return fishermen.stream()
                .filter(arena.players::contains)
                .collect(Collectors.toSet());
    }

    public Set<UUID> getActivePlayers() {
        return arena.players.stream()
                .filter(id -> !fishermen.contains(id))
                .collect(Collectors.toSet());
    }
}
