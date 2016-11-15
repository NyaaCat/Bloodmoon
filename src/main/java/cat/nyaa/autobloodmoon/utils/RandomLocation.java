package cat.nyaa.autobloodmoon.utils;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Random;

public class RandomLocation {
    public static Location RandomLocation(Location loc, int min, int max) {
        World world = loc.getWorld();
        for (int i = 0; i < 10; i++) {
            Block block = getRandomLocation(loc, min, max);
            if (block != null) {
                Block b = world.getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                if (block.getY() > 5) {
                    if (!b.getType().equals(Material.LAVA)) {
                        return block.getLocation().add(new Vector(0.5D, 0.5D, 0.5D));
                    }
                }
            }
        }
        return null;
    }

    private static Block getRandomLocation(Location loc, int min, int max) {
        World world = loc.getWorld();
        for (int i = 0; i < 10; i++) {
            int x = new Random().nextInt(max);
            int z = new Random().nextInt(max);
            if (new Random().nextBoolean()) {
                x *= -1;
            }
            if (new Random().nextBoolean()) {
                z *= -1;
            }
            if (loc.getX() + x < 1.0) {
                x = x - 1;
            }
            if (loc.getZ() + z < 1.0) {
                z = z - 1;
            }
            Block block = world.getHighestBlockAt((int) loc.getX() + x, (int) loc.getZ() + z);
            Location randomLocation = block.getLocation();
            randomLocation.setY(loc.getY());
            double distance = randomLocation.distance(loc);
            if (distance <= max && distance > min + 1) {
                return block;
            }
        }
        return null;
    }
}
