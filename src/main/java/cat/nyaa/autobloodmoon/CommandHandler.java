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

    @Override
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.top();
        if (subCommand == null) subCommand = "";
        switch (subCommand) {
            case "in":
                commandJoin(sender, cmd);
                break;
            default:
                super.acceptCommand(sender, cmd);
        }
    }

    @SubCommand(value = "difficulty", permission = "bm.admin")
    public void commandDifficulty(CommandSender sender, Arguments args) {
        if (args.length() != 7) {
            msg(sender, "manual.difficulty.usage");
            return;
        }
        String type = args.next();
        Level level = new Level();
        level.setLevelType(type);
        level.setMinPlayerAmount(args.nextInt());
        level.setMobAmount(args.nextInt());
        level.setInfernalAmount(args.nextInt());
        level.setMobSpawnDelayTicks(args.nextInt());
        level.setMaxInfernalLevel(args.nextInt());
        plugin.cfg.levelConfig.levels.put(level.getLevelType(), level);
        plugin.cfg.levelConfig.save();
        msg(sender, "user.difficulty.save_success");
    }

    @SubCommand(value = "start", permission = "bm.start")
    public void commandStart(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.start.usage");
            return;
        }
        if (plugin.currentArena != null && plugin.currentArena.state == Arena.ArenaState.STOP) {
            plugin.currentArena = null;
        }
        if (plugin.currentArena == null) {
            String arenaName = args.next();
            String difficulty = args.next();
            String kitName = args.next();
            if (plugin.arenaManager.getArena(arenaName) == null) {
                msg(sender, "user.arena.not_found", arenaName);
                return;
            }
            if (!plugin.cfg.levelConfig.levels.containsKey(difficulty)) {
                msg(sender, "user.difficulty.type_error");
                return;
            }
            if (!plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
                msg(sender, "user.kit.not_found");
                return;
            }
            plugin.currentArena = plugin.arenaManager.getArena(arenaName);
            plugin.currentArena.init(plugin, difficulty, kitName);
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

    @SubCommand(value = "acquire", permission = "bm.player")
    public void commandAcquire(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (plugin.rewardList.containsKey(player.getUniqueId())) {
            plugin.kitManager.applyRewardFromList(player);
        }
    }

    @SubCommand(value = "reload", permission = "bm.admin")
    public void commandReload(CommandSender sender, Arguments args) {
        AutoBloodmoon p = plugin;
        p.reloadConfig();
        p.cfg.deserialize(p.getConfig());
        p.cfg.serialize(p.getConfig());
        p.saveConfig();
        p.i18n = new I18n(plugin, plugin.cfg.language);
    }
}
