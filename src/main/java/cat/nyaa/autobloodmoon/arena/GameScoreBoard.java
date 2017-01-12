package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static cat.nyaa.autobloodmoon.arena.GameScoreBoard.StatType.*;

/**
 * Temporary score storage for each game
 */
public class GameScoreBoard {
    public enum StatType {
        NORMALKILL,
        INFERNALKILL,
        INFERNALASSIST,
        DEATH,    // total death
        ASSISSAN, // killing player
        VICTIM    // killed by player

    }

    private final AutoBloodmoon plugin;
    private final Arena arena;

    private final Set<UUID> fishermen = new HashSet<>();
    private final Map<UUID, Double> scoreMap = new HashMap<>();
    private final Map<Integer, Set<UUID>> activeMap = new HashMap<>();
    private final Map<StatType, Map<UUID, Integer>> statMap = new HashMap<>();

    public GameScoreBoard(AutoBloodmoon plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    public void incNormalKill(Player p) {
        incScore(p.getUniqueId(), plugin.cfg.rewardConfig.normal_kill);
        incStat(p.getUniqueId(), NORMALKILL);
        incActive(p.getUniqueId(), arena.currentLevel);
    }

    public void incInfernalKill(Player killer, LivingEntity infernalMob) {
        UUID killerId = killer == null ? null : killer.getUniqueId();
        Integer infernalLevel = arena.mobLevelMap.get(infernalMob.getUniqueId());
        if (infernalLevel == null) return;
        Double score;
        if (plugin.cfg.rewardConfig.infernal_kill.containsKey(infernalLevel)) {
            score = new Double(plugin.cfg.rewardConfig.infernal_kill.get(infernalLevel));
        } else {
            score = 0D;
        }
        if (score == null) score = 0D;
        if (killer != null) {
            incScore(killerId, score);
            incStat(killerId, INFERNALKILL);
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
                incScore(e.getKey(), score * e.getValue() / totalDamageExcludeKiller);
                incStat(e.getKey(), INFERNALASSIST);
            }
        }
    }

    public void incPlayerKill(Player killer, Player victim) {
        incStat(killer.getUniqueId(), ASSISSAN);
        incStat(victim.getUniqueId(), VICTIM);
        incScore(killer.getUniqueId(), -plugin.cfg.rewardConfig.killer_penalty);
        incScore(victim.getUniqueId(), -plugin.cfg.rewardConfig.victim_penalty);
    }

    private void incScore(UUID id, double score) {
        if (scoreMap.containsKey(id)) {
            scoreMap.put(id, scoreMap.get(id) + score);
        } else {
            scoreMap.put(id, score);
        }
    }

    private void incStat(UUID id, StatType type) {
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

    /**
     * A player is a fisherman if:
     * - active in less than 2 waves (unless there's only one wave), or
     * - have negative score
     */
    public void computeFishermen() {
        if (arena.level.getMaxInfernalLevel() >= 2) {
            for (UUID pid : scoreMap.keySet()) {
                int count = 0;
                for (int level = 1; level <= arena.level.getMaxInfernalLevel(); level++) {
                    if (activeMap.containsKey(level) && activeMap.get(level).contains(pid)) {
                        count++;
                    }
                }
                if (count < 2) fishermen.add(pid);
            }
        }

        for (UUID pid : scoreMap.keySet()) {
            if (scoreMap.get(pid) <= 0) {
                fishermen.add(pid);
            }
        }
    }

    public UUID getMVP() {
        return scoreMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .filter(e -> !fishermen.contains(e.getKey()))
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private int compareByScore(UUID u1, UUID u2) {
        Double s1 = scoreMap.containsKey(u1) ? scoreMap.get(u1) : 0D;
        Double s2 = scoreMap.containsKey(u2) ? scoreMap.get(u2) : 0D;
        return s1.compareTo(s2);
    }

    public UUID getMaxInfernalKill() {
        if (statMap.get(INFERNALKILL) == null) return null;
        return statMap.get(INFERNALKILL).entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .filter(e -> !fishermen.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue()) ?
                        compareByScore(e2.getKey(), e1.getKey()) :
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public UUID getMaxNormalKill() {
        if (statMap.get(NORMALKILL) == null) return null;
        return statMap.get(NORMALKILL).entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .filter(e -> !fishermen.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue()) ?
                        compareByScore(e2.getKey(), e1.getKey()) :
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public UUID getMaxAssist() {
        if (statMap.get(INFERNALASSIST) == null) return null;
        return statMap.get(INFERNALASSIST).entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .filter(e -> !fishermen.contains(e.getKey()))
                .sorted((e1, e2) -> e1.getValue().equals(e2.getValue()) ?
                        compareByScore(e2.getKey(), e1.getKey()) :
                        e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public Map<UUID, Double> getScores() {
        return scoreMap;
    }

    public Set<UUID> getFishermen() {
        return fishermen;
    }

    /**
     * @return the whole list of players appears in scoreMap
     *         Listed by their score from highest to lowest
     */
    public List<UUID> getSortedPlayers() {
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<StatType, Integer> getStatMap(UUID id) {
        Map<StatType, Integer> map = new HashMap<>();
        for (StatType type : statMap.keySet()) {
            if (statMap.get(type).containsKey(id)) {
                map.put(type, statMap.get(type).get(id));
            } else {
                map.put(type, 0);
            }
        }
        return map;
    }
}
