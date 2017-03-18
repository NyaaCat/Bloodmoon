package cat.nyaa.autobloodmoon.api;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class InfernalMobsAPI {
    private static Plugin InfernalMobsPlugin;
    private static Field mobManagerField;
    private static Method spawnMobMethod;
    private static Method isInfernalMobMethod;
    private static Object mobManager;

    public static void load(Plugin InfernalMobs) {
        InfernalMobsAPI.InfernalMobsPlugin = InfernalMobs;
        try {
            mobManagerField = InfernalMobsPlugin.getClass().getDeclaredField("mobManager");
            mobManagerField.setAccessible(true);
            mobManager = mobManagerField.get(InfernalMobsPlugin);
            spawnMobMethod = mobManager.getClass().getDeclaredMethod("spawnMob", EntityType.class, Location.class, ArrayList.class);
            isInfernalMobMethod = InfernalMobsPlugin.getClass().getDeclaredMethod("idSearch", UUID.class);
            isInfernalMobMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static boolean spawnMob(String mobName, ArrayList<String> abilityList, Location loc) {
        try {
            EntityType type = EntityType.fromName(mobName);
            if (type == null) {
                return false;
            }
            Object mob = spawnMobMethod.invoke(mobManager, type, loc, abilityList);
            return mob != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isInfernalMob(Entity mob) {
        try {
            if (((int) isInfernalMobMethod.invoke(InfernalMobsPlugin, mob.getUniqueId())) != -1) {
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
