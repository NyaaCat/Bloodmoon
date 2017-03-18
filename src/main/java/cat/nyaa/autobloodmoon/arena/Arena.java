package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.api.InfernalMobsAPI;
import cat.nyaa.autobloodmoon.events.MobListener;
import cat.nyaa.autobloodmoon.kits.KitConfig;
import cat.nyaa.autobloodmoon.level.Level;
import cat.nyaa.autobloodmoon.mobs.Mob;
import cat.nyaa.autobloodmoon.stats.PlayerStats;
import cat.nyaa.autobloodmoon.utils.GetCircle;
import cat.nyaa.autobloodmoon.utils.RandomLocation;
import cat.nyaa.utils.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static cat.nyaa.autobloodmoon.kits.KitConfig.KitType.*;

public class Arena extends BukkitRunnable implements ISerializable {
    @Serializable
    private String name;
    @Serializable
    private String world;
    @Serializable
    private int radius;
    @Serializable
    private int spawnRadius;
    @Serializable
    private double x;
    @Serializable
    private double y;
    @Serializable
    private double z;

    // TODO separate game logic apart from configure.
    private AutoBloodmoon plugin;
    private int time = 0;
    private int infernal;
    private int ticks = 0;
    private long sendBorderParticle = 0;

    public ArrayList<UUID> players = new ArrayList<>();
    public Level level;
    public int currentLevel = 0;
    public String kitName;
    public int nextWave = 0;
    public int lastSpawn = 0;
    public ArrayList<UUID> infernalMobs = new ArrayList<>();
    public ArrayList<UUID> normalMobs = new ArrayList<>();
    public ArrayList<UUID> entityList = new ArrayList<>();
    public Map<UUID, Integer> mobLevelMap = new HashMap<>(); // Map<MobId, MobLevel>
    public ArenaState state;
    public HashMap<UUID, PlayerStats> playerStats = new HashMap<>(); // TODO merge this into scoreBoard
    public GameScoreBoard scoreBoard;


    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Location getCenterPoint() {
        return new Location(Bukkit.getWorld(getWorld()), x, y, z);
    }

    public void setCenterPoint(Location loc) {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        world = loc.getWorld().getName();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public void setSpawnRadius(int spawnRadius) {
        this.spawnRadius = spawnRadius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void init(AutoBloodmoon plugin, String difficulty, String kitName) {
        this.plugin = plugin;
        this.level = plugin.cfg.levelConfig.levels.get(difficulty);
        this.kitName = kitName;
        state = ArenaState.WAIT;
        nextWave = plugin.cfg.call_timeout;
        this.runTaskTimer(this.plugin, 20, 1);
        broadcast(I18n._("user.game.new_game_0"));
        broadcast(I18n._("user.game.new_game_1", level.getLevelType(), level.getMaxInfernalLevel(),
                level.getMinPlayerAmount()));
        broadcast(I18n._("user.game.new_game_2"));
    }

    public void join(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            PlayerStats stats = getPlayerStats(player);
            stats.incrementStats(PlayerStats.StatsType.JOINED);
            playerStats.put(player.getUniqueId(), stats);
            broadcast(I18n._("user.game.join", player.getName(), players.size(), level.getMinPlayerAmount()));
            plugin.teleportUtil.Teleport(player, getCenterPoint());
        }
    }

    public boolean quit(Player player) {
        if (players.contains(player.getUniqueId())) {
            players.remove(player.getUniqueId());
            broadcast(I18n._("user.game.quit", player.getName()));
            broadcast(I18n._("user.game.players_remaining", players.size()));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Change state from WAITING to PLAYING
     */
    public void start() {
        state = ArenaState.PLAYING;
        scoreBoard = new GameScoreBoard(plugin, this);
        lockTime();
        nextWave = 0;
        currentLevel = 0;
    }

    public void stop() {
        state = ArenaState.STOP;
        this.cancel();
        removeAllMobs();
        for (UUID k : playerStats.keySet()) {
            plugin.statsManager.getPlayerStats(k).add(playerStats.get(k));
        }
        plugin.cfg.statsConfig.save();
        plugin.currentArena = null;
    }

    @Override
    public void run() {
        time++;
        ticks++;
        sendBorderParticle();
        if (state == ArenaState.WAIT) {
            nextWave--;
            if (nextWave <= 0) {
                if (players.size() >= level.getMinPlayerAmount()) {
                    this.start();
                } else {
                    broadcast(I18n._("user.game.cancel"));
                    this.stop();
                }
            } else {
                if (nextWave <= 300) {
                    if (players.size() >= level.getMinPlayerAmount()) {
                        if (ticks >= 20) {
                            broadcast(I18n._("user.game.start", nextWave / 20));
                            ticks = 0;
                            return;
                        }
                    } else {
                        broadcast(I18n._("user.game.cancel"));
                        this.stop();
                    }
                } else {

                }
            }
        } else if (state == ArenaState.PLAYING) {
            if (nextWave <= 0) {
                if (currentLevel > 0 && time - lastSpawn >= level.getMobSpawnDelayTicks() &&
                        this.normalMobs.size() < players.size() * level.getMobAmount()) {
                    spawnMob();
                    lastSpawn = time;
                    return;
                }
                if (currentLevel == 0 || (infernalMobs.isEmpty() &&
                        normalMobs.size() >= players.size() * level.getMobAmount() &&
                        currentLevel < level.getMaxInfernalLevel())) {
                    nextWave = plugin.cfg.preparation_time;
                    normalMobs.clear();
                    currentLevel++;
                    broadcast(I18n._("user.game.next_wave", nextWave / 20));
                    broadcast(I18n._("user.game.level", currentLevel));
                    return;
                }
                if (infernalMobs.isEmpty() && currentLevel >= level.getMaxInfernalLevel() && !players.isEmpty() &&
                        normalMobs.size() >= players.size() * level.getMobAmount()) {
                    scoreBoard.computeFishermen();
                    UUID mvpId = scoreBoard.getMVP();
                    UUID mostKillId = scoreBoard.getMaxInfernalKill();
                    UUID mostNormalId = scoreBoard.getMaxNormalKill();
                    UUID mostAssistId = scoreBoard.getMaxAssist();

                    Map<UUID, Double> scoreMap = scoreBoard.getScores();
                    List<UUID> sortedPlayers = scoreBoard.getSortedPlayers();
                    Set<UUID> fishermen = scoreBoard.getFishermen();

                    // increase WINNING counter
                    List<PlayerStats> stats = players.stream().map(playerStats::get).filter(st -> st != null).collect(Collectors.toList());
                    stats.forEach(st -> st.incrementStats(PlayerStats.StatsType.WINING));

                    // winning announcement
                    broadcast(I18n._("user.game.win"));
                    if (mvpId != null) {
                        broadcast(I18n._("user.game.mvp", plugin.getServer().getOfflinePlayer(mvpId).getName()));
                    } else {
                        broadcast(I18n._("user.game.no_mvp"));
                    }
                    if (mostKillId != null) {
                        broadcast(I18n._("user.game.most_infernal_kill",
                                plugin.getServer().getOfflinePlayer(mostKillId).getName(),
                                getPlayerStats(plugin.getServer().getOfflinePlayer(mostKillId)).infernal_kill));
                    } else {
                        broadcast(I18n._("user.game.no_most_infernal_kill"));
                    }
                    if (mostNormalId != null) {
                        broadcast(I18n._("user.game.most_normal_kill",
                                plugin.getServer().getOfflinePlayer(mostNormalId).getName(),
                                getPlayerStats(plugin.getServer().getOfflinePlayer(mostNormalId)).normal_kill));
                    } else {
                        broadcast(I18n._("user.game.no_most_normal_kill"));
                    }
                    if (mostAssistId != null) {
                        broadcast(I18n._("user.game.most_assist",
                                plugin.getServer().getOfflinePlayer(mostAssistId).getName(),
                                getPlayerStats(plugin.getServer().getOfflinePlayer(mostAssistId)).assist));
                    } else {
                        broadcast(I18n._("user.game.no_most_assist"));
                    }
                    for (UUID id : fishermen) {
                        broadcast(I18n._("user.game.great_fisherman", plugin.getServer().getOfflinePlayer(id).getName()));
                    }

                    for (UUID id : sortedPlayers) {
                        Map<GameScoreBoard.StatType, Integer> stat = scoreBoard.getStatMap(id);

                        Object[] objs = new Object[6];
                        objs[0] = plugin.getServer().getOfflinePlayer(id).getName();
                        objs[1] = scoreMap.get(id);
                        objs[2] = stat.get(GameScoreBoard.StatType.INFERNALKILL);
                        objs[3] = stat.get(GameScoreBoard.StatType.INFERNALASSIST);
                        objs[4] = stat.get(GameScoreBoard.StatType.NORMALKILL);
                        objs[5] = playerStats.containsKey(id)? playerStats.get(id).death: 0;  // TODO use scoreBoard

                        if (fishermen.contains(id)) {
                            broadcast(I18n._("user.game.player_stats_fisherman", objs));
                        } else {
                            broadcast(I18n._("user.game.player_stats_active", objs));
                        }
                    }

                    // Distribute Rewards
                    Set<UUID> rewardedPlayers = new HashSet<>();
                    KitConfig kit = plugin.cfg.rewardConfig.kits.get(kitName);
                    if (mvpId != null) {
                        plugin.kitManager.addUnacquiredReward(mvpId, kit.getKit(MVP));
                        rewardedPlayers.add(mvpId);
                    }
                    if (mostKillId != null) {
                        plugin.kitManager.addUnacquiredReward(mostKillId, kit.getKit(MOSTKILL));
                        rewardedPlayers.add(mostKillId);
                    }
                    if (mostNormalId != null) {
                        plugin.kitManager.addUnacquiredReward(mostNormalId, kit.getKit(MOSTNORMALKILL));
                        rewardedPlayers.add(mostNormalId);
                    }
                    if (mostAssistId != null) {
                        plugin.kitManager.addUnacquiredReward(mostAssistId, kit.getKit(MOSTASSIST));
                        rewardedPlayers.add(mostAssistId);
                    }
                    List<ItemStack> teamReward = kit.getKit(TEAM);
                    Collections.shuffle(teamReward);
                    for (int i = 0; i < Math.min(teamReward.size(), sortedPlayers.size()); i++) {
                        plugin.kitManager.addUnacquiredReward(sortedPlayers.get(i),
                                Collections.singletonList(teamReward.get(i)));
                        rewardedPlayers.add(sortedPlayers.get(i));
                    }

                    // Actually give rewards
                    for (UUID id : rewardedPlayers) {
                        plugin.kitManager.applyUnacquiredReward(id);
                    }

                    // Give money
                    for (Map.Entry<UUID, Double> e : scoreMap.entrySet()) {
                        if (fishermen.contains(e.getKey())) continue;
                        if (e.getValue() <= 0 || e.getValue().isNaN()) continue;
                        plugin.vaultUtil.deposit(plugin.getServer().getOfflinePlayer(e.getKey()), e.getValue());
                        Player p = plugin.getServer().getPlayer(e.getKey());
                        if (p != null) {
                            p.sendMessage(I18n._("user.game.money_given", e.getValue()));
                        }
                    }

                    // Cancel listen & write statistics to db
                    stop();
                    return;
                }
            }
            if (ticks >= 20) {
                ticks = 0;
                lockTime();
                if (!infernalMobs.isEmpty()) {
                    ArrayList<UUID> tmp = new ArrayList<>();
                    for (LivingEntity entity : getCenterPoint().getWorld().getLivingEntities()) {
                        if (!entity.isDead() && infernalMobs.contains(entity.getUniqueId()) &&
                                InfernalMobsAPI.isInfernalMob(entity)) {
                            Location location = getCenterPoint().clone();
                            location.setY(entity.getLocation().getY());
                            if (location.distance(entity.getLocation()) > getRadius()) {
                                Location loc = getRandomLocation();
                                if (loc != null) {
                                    entity.teleport(loc);
                                }
                            }
                            tmp.add(entity.getUniqueId());
                        }
                    }
                    if (tmp.size() != infernalMobs.size()) {
                        infernalMobs = tmp;
                        broadcast(I18n._("user.game.mobs_remaining", infernalMobs.size()));
                    }
                }
            }
            nextWave--;
            if (players.isEmpty()) {
                broadcast(I18n._("user.game.fail"));
                stop();
            }
        } else {
            cancel();
        }
    }

    public Location getRandomLocation() {
        return RandomLocation.RandomLocation(getCenterPoint(), getSpawnRadius(), getRadius());
    }

    public void spawnMob() {
        infernal++;
        if (infernal == level.getInfernalAmount()) {
            Mob mob = plugin.mobManager.getRandomMob(currentLevel);
            Location loc = getRandomLocation();
            if (mob != null && loc != null) {
                plugin.mobListener.spawnLocation = loc;
                plugin.mobListener.mobType = MobListener.MobType.INFERNAL;
                plugin.mobListener.mobLevel = mob.getSkills().size();
                if (InfernalMobsAPI.spawnMob(mob.getMobType(), mob.getSkills(), loc)) {
                    plugin.mobListener.spawnLocation = null;
                }
            }
            infernal = 0;
            return;
        } else {
            Location loc = getRandomLocation();
            if (loc != null) {
                String mob = plugin.cfg.mobConfig.normalMob.get(new Random().nextInt(
                        plugin.cfg.mobConfig.normalMob.size()));
                plugin.mobListener.spawnLocation = loc;
                plugin.mobListener.mobType = MobListener.MobType.NORMAL;
                loc.getWorld().spawnEntity(loc, EntityType.valueOf(mob.toUpperCase()));
            }
        }
    }

    public void broadcast(String s) {
        plugin.getServer().broadcastMessage(I18n._("user.prefix") + s);
    }

    public Arena clone() {
        Arena arena = new Arena();
        arena.setName(getName());
        arena.setCenterPoint(getCenterPoint());
        arena.setRadius(getRadius());
        arena.setSpawnRadius(getSpawnRadius());
        return arena;
    }

    public void lockTime() {
        if (getCenterPoint().getWorld().getTime() != 18000) {
            getCenterPoint().getWorld().setTime(18000);
        }
    }

    public void removeAllMobs() {
        for (LivingEntity entity : getCenterPoint().getWorld().getLivingEntities()) {
            if (entityList.contains(entity.getUniqueId())) {
                entity.remove();
            }
        }
    }

    public void sendBorderParticle() {
        if (plugin.cfg.border_particle && System.currentTimeMillis() - sendBorderParticle >= 4000) {
            sendBorderParticle = System.currentTimeMillis();
            for (Block block : GetCircle.getCylinder(getCenterPoint(), getCenterPoint().getWorld(),
                    getRadius(), getRadius(), plugin.cfg.border_particle_height, false)) {
                block.getWorld().spawnParticle(Particle.BARRIER, block.getLocation().add(
                        new Vector(0.5D, 0.5D, 0.5D)), 1);
            }
        }
    }

    // NOTE: this is temporary statue, though it shares same class with persist status
    public PlayerStats getPlayerStats(OfflinePlayer player) {
        if (!playerStats.containsKey(player.getUniqueId())) {
            playerStats.put(player.getUniqueId(), new PlayerStats(player));
        }
        return playerStats.get(player.getUniqueId());
    }

    public enum ArenaState {
        WAIT,
        PLAYING,
        STOP
    }

}
