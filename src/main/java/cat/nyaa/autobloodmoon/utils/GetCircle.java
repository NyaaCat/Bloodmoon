/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cat.nyaa.autobloodmoon.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class GetCircle {
    /*
    * https://github.com/sk89q/WorldEdit/blob/34c31dc020307a45482ad53ae4d1459b5f653a94/worldedit-core/src/main/java/com/sk89q/worldedit/EditSession.java#L1336 
    */
    public static List<Block> getCylinder(Location loc, World world, double radiusX, double radiusZ, int height, boolean filled) {
        List<Block> blocks = new ArrayList<>();
        Location pos = loc.clone();
        radiusX += 0.5;
        radiusZ += 0.5;
        if (pos.getBlockY() + height - 1 > world.getMaxHeight()) {
            height = world.getMaxHeight() - pos.getBlockY() + 1;
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ:
            for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue;
                    }
                }
                for (int y = 0; y < height; ++y) {
                    blocks.add(loc.clone().add(x, y, z).getBlock());
                    blocks.add(loc.clone().add(-x, y, z).getBlock());
                    blocks.add(loc.clone().add(x, y, -z).getBlock());
                    blocks.add(loc.clone().add(-x, y, -z).getBlock());
                }
            }
        }
        return blocks;
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }
}
