package cat.nyaa.autobloodmoon.api;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CoreProtectAPI {

    private Method logPlacementMethod;
    private AutoBloodmoon plugin;
    private Plugin coreProtectPlugin;
    private Object coreProtectAPI = null;
    private boolean enable = false;

    public CoreProtectAPI(AutoBloodmoon pl) {
        this.plugin = pl;
        this.coreProtectPlugin = pl.getServer().getPluginManager().getPlugin("CoreProtect");
        try {
            if (coreProtectPlugin != null) {
                Method getAPIMethod = coreProtectPlugin.getClass().getDeclaredMethod("getAPI");
                this.coreProtectAPI = getAPIMethod.invoke(this.coreProtectPlugin);
                this.logPlacementMethod = coreProtectAPI.getClass().
                        getDeclaredMethod("logPlacement", String.class, Location.class, Material.class, BlockData.class);
                coreProtectAPI.getClass().getDeclaredMethod("testAPI").invoke(coreProtectAPI);
                enable = (boolean) coreProtectAPI.getClass().getDeclaredMethod("isEnabled").invoke(coreProtectAPI);
                return;
            } else {
                enable = false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        enable = false;
    }

    public boolean isEnabled() {
        return enable;
    }

    public boolean logPlacement(Player player, Location location, Material type, BlockData data) {
        if (!isEnabled()) {
            return false;
        }
        try {
            String playerName = "#bm_" + player.getName();
            return (boolean) logPlacementMethod.invoke(this.coreProtectAPI, playerName, location, type, data);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
