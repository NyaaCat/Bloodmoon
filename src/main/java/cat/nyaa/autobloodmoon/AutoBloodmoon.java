package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.api.InfernalMobsAPI;
import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.arena.ArenaManager;
import cat.nyaa.autobloodmoon.events.MobListener;
import cat.nyaa.autobloodmoon.events.PlayerListener;
import cat.nyaa.autobloodmoon.kits.KitListener;
import cat.nyaa.autobloodmoon.kits.KitManager;
import cat.nyaa.autobloodmoon.mobs.MobManager;
import cat.nyaa.autobloodmoon.stats.StatsManager;
import cat.nyaa.autobloodmoon.utils.TeleportUtil;
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
    public KitListener kitListener;
    public MobManager mobManager;
    public TeleportUtil teleportUtil;
    public Arena currentArena = null;
    public PlayerListener playerListener;
    public MobListener mobListener;
    public StatsManager statsManager;
    public NyaaUtils nyaaUtils;
    public KitManager kitManager;

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
        this.arenaManager = new ArenaManager(this);
        this.kitListener = new KitListener(this);
        this.mobManager = new MobManager(this);
        InfernalMobsAPI.load(getServer().getPluginManager().getPlugin("InfernalMobs"));
        this.teleportUtil = new TeleportUtil(this);
        this.playerListener = new PlayerListener(this);
        this.mobListener = new MobListener(this);
        this.statsManager = new StatsManager(this);
        this.nyaaUtils = getPlugin(NyaaUtils.class);
        this.kitManager = new KitManager(this);
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
