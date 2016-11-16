package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.api.InfernalMobsAPI;
import cat.nyaa.autobloodmoon.events.MobListener;
import cat.nyaa.autobloodmoon.level.Level;
import cat.nyaa.autobloodmoon.mobs.Mob;
import cat.nyaa.autobloodmoon.utils.RandomLocation;
import cat.nyaa.utils.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

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
    public ArenaState state;
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
    private AutoBloodmoon plugin;
    private int time = 0;
    private int infernal;
    private int ticks = 0;


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

    public void init(AutoBloodmoon plugin, Level level, String kitName) {
        this.plugin = plugin;
        this.level = level;
        this.kitName = kitName;
        state = ArenaState.WAIT;
        nextWave = plugin.cfg.call_timeout;
        this.runTaskTimer(this.plugin, 20, 1);
        broadcast(I18n._("user.game.new_game_0"));
        broadcast(I18n._("user.game.new_game_1", level.getLevelType().toString(), level.getMaxInfernalLevel(),
                level.getMinPlayerAmount()));
        broadcast(I18n._("user.game.new_game_2"));
    }

    public void join(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            if (players.size() >= level.getMinPlayerAmount()) {
                broadcast(I18n._("user.game.join", player.getName(), players.size(), players.size() + 1));
            } else {
                broadcast(I18n._("user.game.join", player.getName(), players.size(), level.getMinPlayerAmount()));
            }
            if (state == ArenaState.PLAYING) {
                plugin.teleportUtil.Teleport(player, getCenterPoint());
            }
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

    public void start() {
        state = ArenaState.PLAYING;
        lockTime();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (players.contains(p.getUniqueId())) {
                plugin.teleportUtil.Teleport(p, getCenterPoint());
            }
        }
        nextWave = 0;
        currentLevel = 0;
    }

    public void stop() {
        state = ArenaState.STOP;
        this.cancel();
        removeAllMobs();
        plugin.currentArena = null;
    }

    @Override
    public void run() {
        time++;
        ticks++;
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
                    broadcast(I18n._("user.game.win"));
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
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(I18n._("user.prefix") + s);
        }
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

    public enum ArenaState {
        WAIT,
        PLAYING,
        STOP
    }

}
