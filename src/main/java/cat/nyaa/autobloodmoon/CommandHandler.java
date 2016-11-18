package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.arena.ArenaCommands;
import cat.nyaa.autobloodmoon.kits.KitCommands;
import cat.nyaa.autobloodmoon.level.Level;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler extends CommandReceiver<AutoBloodmoon> {
    @SubCommand("kit")
    public KitCommands kitCommands;
    @SubCommand("arena")
    public ArenaCommands arenaCommands;
    private AutoBloodmoon plugin;

    public CommandHandler(AutoBloodmoon pl, Internationalization i18n) {
        super(pl, i18n);
        plugin = pl;
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "difficulty", permission = "bm.admin")
    public void commandDifficulty(CommandSender sender, Arguments args) {
        String type = args.next().toUpperCase();
        Level level = new Level();
        Level.LevelType levelType = null;
        for (Level.LevelType k : Level.LevelType.values()) {
            if (k.name().equals(type)) {
                levelType = k;
                break;
            }
        }
        if (levelType == null) {
            sender.sendMessage(I18n._("user.difficulty.type_error"));
            return;
        }
        level.setLevelType(levelType);
        level.setMinPlayerAmount(args.nextInt());
        level.setMobAmount(args.nextInt());
        level.setInfernalAmount(args.nextInt());
        level.setMobSpawnDelayTicks(args.nextInt());
        level.setMaxInfernalLevel(args.nextInt());
        plugin.cfg.levelConfig.levels.put(level.getLevelType(), level);
        plugin.cfg.levelConfig.save();
    }

    @SubCommand(value = "start", permission = "bm.start")
    public void commandStart(CommandSender sender, Arguments args) {
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.STOP) {
            plugin.currentArena = null;
        }
        if (plugin.currentArena == null) {
            String arenaName = args.next();
            plugin.currentArena = plugin.arenaManager.getArena(arenaName);
            plugin.currentArena.init(plugin, plugin.cfg.levelConfig.levels.get(Level.LevelType.valueOf(args.next())), args.next());
            return;
        }
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.WAIT) {
            plugin.currentArena.start();
            return;
        }
    }

    @SubCommand(value = "stop", permission = "bm.stop")
    public void commandStop(CommandSender sender, Arguments args) {
        if (plugin.currentArena != null) {
            plugin.currentArena.stop();
        }
    }

    @SubCommand(value = "join", permission = "bm.join")
    public void commandJoin(CommandSender sender, Arguments args) {
        if (plugin.currentArena != null && plugin.currentArena.state != Arena.ArenaState.STOP) {
            Player player = asPlayer(sender);
            plugin.currentArena.join(player);
        }
    }

    @SubCommand(value = "quit", permission = "bm.quit")
    public void commandQuit(CommandSender sender, Arguments args) {
        if (plugin.currentArena != null && plugin.currentArena.state != Arena.ArenaState.STOP) {
            Player player = asPlayer(sender);
            plugin.currentArena.quit(player);
        }
    }

    @SubCommand(value = "reload", permission = "bm.admin")
    public void commandReload(CommandSender sender, Arguments args) {
        AutoBloodmoon p = plugin;
        p.reloadConfig();
        p.cfg.deserialize(p.getConfig());
        p.cfg.serialize(p.getConfig());
        p.saveConfig();
        p.i18n.reset();
        p.i18n.load(p.cfg.language);
    }
}
