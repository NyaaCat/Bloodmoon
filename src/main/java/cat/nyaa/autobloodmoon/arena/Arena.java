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
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.VaultUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.librazy.nyaautils_lang_checker.LangKey;
import org.librazy.nyaautils_lang_checker.LangKeyType;

import java.util.*;

import static cat.nyaa.autobloodmoon.kits.KitConfig.KitType.*;

public class Arena extends BukkitRunnable implements ISerializable {
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
    public GameScoreBoard scoreBoard;
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
        broadcast(I18n.format("user.game.new_game_0"));
        broadcast(I18n.format("user.game.new_game_1", level.getLevelType(), level.getMaxInfernalLevel(),
                level.getMinPlayerAmount()));
        broadcast(I18n.format("user.game.new_game_2"));
    }

    public void join(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            plugin.statsManager.getPlayerStats(player).incrementStats(PlayerStats.StatsType.JOINED);
            broadcast(I18n.format("user.game.join", player.getName(), players.size(), level.getMinPlayerAmount()));
            plugin.teleportUtil.Teleport(player, getCenterPoint());
        }
    }

    public boolean quit(Player player) {
        if (players.contains(player.getUniqueId())) {
            players.remove(player.getUniqueId());
            broadcast(I18n.format("user.game.quit", player.getName()));
            broadcast(I18n.format("user.game.players_remaining", players.size()));
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
        plugin.cfg.statsConfig.save();
        if (!plugin.cfg.pvp && plugin.cfg.temp_pvp_protection_time > 0) {
            for (UUID uuid : players) {
                plugin.tempPVPProtection.put(uuid, System.currentTimeMillis() + (plugin.cfg.temp_pvp_protection_time * 1000));
            }
        }
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
                    broadcast(I18n.format("user.game.cancel"));
                    this.stop();
                }
            } else {
                if (nextWave <= 300) {
                    if (players.size() >= level.getMinPlayerAmount()) {
                        if (ticks >= 20) {
                            broadcastTitle("user.game.start", nextWave / 20);
                            ticks = 0;
                        }
                    } else {
                        broadcast(I18n.format("user.game.cancel"));
                        this.stop();
                    }
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
                    broadcastTitle("user.game.next_wave", currentLevel, nextWave / 20);
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
                    for (UUID uuid : players) {
                        plugin.statsManager.getPlayerStats(uuid).incrementStats(PlayerStats.StatsType.WINING);
                    }

                    // winning announcement
                    broadcast(I18n.format("user.game.win"));
                    if (mvpId != null) {
                        broadcast(I18n.format("user.game.mvp", plugin.getServer().getOfflinePlayer(mvpId).getName()));
                    } else {
                        broadcast(I18n.format("user.game.no_mvp"));
                    }
                    if (mostKillId != null) {
                        Map<GameScoreBoard.StatType, Integer> stat = this.scoreBoard.getStatMap(mostKillId);
                        broadcast(I18n.format("user.game.most_infernal_kill",
                                plugin.getServer().getOfflinePlayer(mostKillId).getName(),
                                stat.get(GameScoreBoard.StatType.INFERNALKILL)));
                    } else {
                        broadcast(I18n.format("user.game.no_most_infernal_kill"));
                    }
                    if (mostNormalId != null) {
                        Map<GameScoreBoard.StatType, Integer> stat = this.scoreBoard.getStatMap(mostNormalId);
                        broadcast(I18n.format("user.game.most_normal_kill",
                                plugin.getServer().getOfflinePlayer(mostNormalId).getName(),
                                stat.get(GameScoreBoard.StatType.NORMALKILL)));
                    } else {
                        broadcast(I18n.format("user.game.no_most_normal_kill"));
                    }
                    if (mostAssistId != null) {
                        Map<GameScoreBoard.StatType, Integer> stat = this.scoreBoard.getStatMap(mostAssistId);
                        broadcast(I18n.format("user.game.most_assist",
                                plugin.getServer().getOfflinePlayer(mostAssistId).getName(),
                                stat.get(GameScoreBoard.StatType.INFERNALASSIST)));
                    } else {
                        broadcast(I18n.format("user.game.no_most_assist"));
                    }
                    for (UUID id : fishermen) {
                        broadcast(I18n.format("user.game.great_fisherman", plugin.getServer().getOfflinePlayer(id).getName()));
                    }

                    for (UUID id : sortedPlayers) {
                        Map<GameScoreBoard.StatType, Integer> stat = scoreBoard.getStatMap(id);
                        @LangKey String prompt;
                        if (fishermen.contains(id)) {
                            prompt = "user.game.player_stats_fisherman";
                        } else {
                            prompt = "user.game.player_stats_active";
                        }
                        broadcast(I18n.format(
                                prompt
                                , plugin.getServer().getOfflinePlayer(id).getName()
                                , scoreMap.get(id)
                                , stat.get(GameScoreBoard.StatType.INFERNALKILL)
                                , stat.get(GameScoreBoard.StatType.INFERNALASSIST)
                                , stat.get(GameScoreBoard.StatType.NORMALKILL)
                                , stat.get(GameScoreBoard.StatType.DEATH)
                        ));

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
                        VaultUtils.deposit(plugin.getServer().getOfflinePlayer(e.getKey()), e.getValue());
                        Player p = plugin.getServer().getPlayer(e.getKey());
                        if (p != null) {
                            p.sendMessage(I18n.format("user.game.money_given", e.getValue()));
                        }
                    }
                    for (UUID id : sortedPlayers) {
                        PlayerStats playerStats = plugin.statsManager.getPlayerStats(id);
                        playerStats.add(scoreBoard.getStatMap(id));
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
                        broadcast(I18n.format("user.game.mobs_remaining", infernalMobs.size()));
                    }
                }
            }
            nextWave--;
            if (players.isEmpty()) {
                broadcast(I18n.format("user.game.fail"));
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
        new Message(I18n.format("user.prefix") + s)
                .broadcast(
                        Message.MessageType.CHAT,
                        p -> players.contains(p.getUniqueId())
                );
    }

    public void broadcastTitle(@LangKey(type = LangKeyType.PREFIX) String s, Object... args) {
        Message title = new Message(I18n.format(s + ".title", args));
        Message subtitle = new Message(I18n.format(s + ".subtitle", args));
        players.stream().map(plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(
                p -> Message.sendTitle(
                        p,
                        title.inner,
                        subtitle.inner,
                        plugin.cfg.title_fadein_tick,
                        plugin.cfg.title_stay_tick,
                        plugin.cfg.title_fadeout_tick)
        );
        plugin.getServer().getConsoleSender().sendMessage(title.inner.toLegacyText());
        plugin.getServer().getConsoleSender().sendMessage(subtitle.inner.toLegacyText());
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

    public enum ArenaState {
        WAIT,
        PLAYING,
        STOP
    }

}
