package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.ArenaManager;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.VaultUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoBloodmoon extends JavaPlugin {
    public static AutoBloodmoon instance;
    public CommandHandler commandHandler;
    public Internationalization i18n;
    public boolean serverEnabled;
    public Configuration cfg;
    public VaultUtil vaultUtil;
    public ArenaManager arenaManager;

    @Override
    public void onLoad() {
        this.serverEnabled = false;
        instance = this;
        this.saveDefaultConfig();
        this.cfg = new Configuration(this);
        this.cfg.deserialize(this.getConfig());
        this.cfg.serialize(this.getConfig());
        this.saveConfig();
        this.i18n = new I18n(this, this.cfg.language);
    }

    @Override
    public void onDisable() {
        instance = null;
        this.cfg.serialize(this.getConfig());
        this.saveConfig();
        I18n.instance.reset();
        this.serverEnabled = false;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.serverEnabled = true;
        this.i18n.load(this.cfg.language);
        this.commandHandler = new CommandHandler(this, this.i18n);
        this.getCommand("bloodmoon").setExecutor(this.commandHandler);
        this.vaultUtil = getPlugin(NyaaUtils.class).vaultUtil;
        this.arenaManager=new ArenaManager(this);
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
