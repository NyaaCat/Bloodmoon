package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.utils.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Arena implements ISerializable {
    @Serializable
    private String name;
    @Serializable
    private String world;
    @Serializable
    private int radius;
    @Serializable
    private int spawnRadius;
    @Serializable
    private int x;
    @Serializable
    private int y;
    @Serializable
    private int z;

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Location getCenterPoint() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public void setCenterPoint(Location loc) {
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        world = loc.getWorld().getName();
    }

    public void setCenterPoint(String world, int x, int y, int z) {
        setCenterPoint(new Location(Bukkit.getWorld(world), x, y, z));
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

}
