package cat.nyaa.autobloodmoon;

import org.bukkit.plugin.java.JavaPlugin;

public class AutoBloodmoon extends JavaPlugin {
    public static AutoBloodmoon instance;

    @Override
    public void onLoad() {

    }

    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }
}
