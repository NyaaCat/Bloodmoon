package cat.nyaa.autobloodmoon.arena;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommands extends CommandReceiver<AutoBloodmoon> {
    private AutoBloodmoon plugin;

    public ArenaCommands(Object plugin, Internationalization i18n) {
        super((AutoBloodmoon) plugin, i18n);
        this.plugin = (AutoBloodmoon) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "arena";
    }

    @SubCommand(value = "create", permission = "bm.admin")
    public void commandCreate(CommandSender sender, Arguments args) {
        if (args.length() != 5) {
            msg(sender, "manual.arena.create.usage");
            return;
        }
        Player player = asPlayer(sender);
        String arenaName = args.next();
        int radius = args.nextInt();
        int spawnRadius = args.nextInt();
        if (spawnRadius >= radius) {
            msg(sender, "user.arena.radius_error");
            return;
        }
        plugin.arenaManager.createArena(arenaName, player.getLocation(), radius, spawnRadius);
        Arena arena = plugin.arenaManager.getArena(arenaName);
        sender.sendMessage(I18n._("user.arena.info", arena.getName(), arena.getWorld(),
                arena.getCenterPoint().getX(), arena.getCenterPoint().getY(), arena.getCenterPoint().getZ(),
                arena.getRadius(), arena.getSpawnRadius()));
    }

    @SubCommand(value = "list", permission = "bm.admin")
    public void commandList(CommandSender sender, Arguments args) {
        for (String k : plugin.cfg.arenaConfig.arenaList.keySet()) {
            Arena arena = plugin.cfg.arenaConfig.arenaList.get(k);
            sender.sendMessage(I18n._("user.arena.info", arena.getName(), arena.getWorld(),
                    arena.getCenterPoint().getX(), arena.getCenterPoint().getY(), arena.getCenterPoint().getZ(),
                    arena.getRadius(), arena.getSpawnRadius()));
        }
    }

    @SubCommand(value = "remove", permission = "bm.admin")
    public void commandRemove(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.arena.remove.usage");
            return;
        }
        String arenaName = args.next();
        if (plugin.arenaManager.removeArena(arenaName)) {
            sender.sendMessage(I18n._("user.arena.remove", arenaName));
        } else {
            sender.sendMessage(I18n._("user.arena.not_found", arenaName));
        }
    }
}
