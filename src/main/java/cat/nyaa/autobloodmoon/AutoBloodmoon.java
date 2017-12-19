package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.api.CoreProtectAPI;
import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.arena.ArenaManager;
import cat.nyaa.autobloodmoon.events.MobListener;
import cat.nyaa.autobloodmoon.events.PlayerListener;
import cat.nyaa.autobloodmoon.kits.KitListener;
import cat.nyaa.autobloodmoon.kits.KitManager;
import cat.nyaa.autobloodmoon.mobs.MobManager;
import cat.nyaa.autobloodmoon.stats.StatsManager;
import cat.nyaa.nyaautils.api.DamageStatistic;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class AutoBloodmoon extends JavaPlugin {
    public static AutoBloodmoon instance;
    public CommandHandler commandHandler;
    public I18n i18n;
    public Configuration cfg;
    public ArenaManager arenaManager;
    public KitListener kitListener;
    public MobManager mobManager;
    public Arena currentArena = null;
    public HashMap<UUID, Long> tempPVPProtection = new HashMap<>();
    public PlayerListener playerListener;
    public MobListener mobListener;
    public StatsManager statsManager;
    public DamageStatistic damageStatistic;
    public KitManager kitManager;
    public CoreProtectAPI coreProtectAPI;

    @Override
    public void onEnable() {
        instance = this;
        this.cfg = new Configuration(this);
        this.cfg.load();
        this.i18n = new I18n(this, this.cfg.language);

        this.commandHandler = new CommandHandler(this, this.i18n);
        this.arenaManager = new ArenaManager(this);
        this.kitListener = new KitListener(this);
        this.mobManager = new MobManager(this);
        this.playerListener = new PlayerListener(this);
        this.mobListener = new MobListener(this);
        this.statsManager = new StatsManager(this);
        this.kitManager = new KitManager(this);
        damageStatistic = DamageStatistic.instance();
        this.getCommand("bloodmoon").setExecutor(this.commandHandler);
        this.coreProtectAPI = new CoreProtectAPI(this);
        if (Bukkit.getScoreboardManager().getMainScoreboard().getTeam("bloodmoon") != null) {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("bloodmoon").unregister();
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getCommand("bloodmoon").setExecutor(null);
        HandlerList.unregisterAll(this);
        cfg.save();
        tempPVPProtection.clear();
    }

    public void doReload() {
        getServer().getScheduler().cancelTasks(this);
        getCommand("bloodmoon").setExecutor(null);
        HandlerList.unregisterAll(this);
        i18n.load();
        onEnable();
    }
}
