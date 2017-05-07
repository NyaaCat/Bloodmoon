package cat.nyaa.autobloodmoon.api;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class InfernalMobsAPI {
    private static Method spawnMobMethod;
    private static Method isInfernalMobMethod;

    public static void load(Plugin InfernalMobs) {
        try {
            spawnMobMethod = Class.forName("com.jacob_vejvoda.infernal_mobs.InfernalMobsAPI").getMethod(
                    "spawnInfernalMob",
                    EntityType.class, Location.class, Collection.class
            );
            isInfernalMobMethod = Class.forName("com.jacob_vejvoda.infernal_mobs.InfernalMobsAPI").getMethod(
                    "asInfernalMob",
                    UUID.class
            );
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean spawnMob(String mobName, ArrayList<String> abilityList, Location loc) {
        try {
            EntityType type = EntityType.fromName(mobName);
            if (type == null) {
                return false;
            }
            Object mob = spawnMobMethod.invoke(null, type, loc, abilityList);
            return mob != null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isInfernalMob(Entity mob) {
        try {
            if (isInfernalMobMethod.invoke(null, mob.getUniqueId()) != null) {
                return true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
