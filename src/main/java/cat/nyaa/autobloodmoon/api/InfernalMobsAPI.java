package cat.nyaa.autobloodmoon.api;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class InfernalMobsAPI {
    private static Plugin InfernalMobsPlugin;
    private static Method spawnMob;

    public static void load(Plugin InfernalMobs) {
        InfernalMobsAPI.InfernalMobsPlugin = InfernalMobs;
        try {
            spawnMob = InfernalMobsPlugin.getClass().getDeclaredMethod("cSpawn", CommandSender.class,
                    String.class, Location.class, ArrayList.class);
            spawnMob.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public static boolean spawnMob(String mobName, ArrayList<String> abilityList, Location loc) {
        try {
            return (Boolean) spawnMob.invoke(InfernalMobsPlugin, Bukkit.getConsoleSender(), mobName, loc, abilityList);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
