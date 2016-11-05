package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.kits.KitCommands;
import cat.nyaa.autobloodmoon.level.Level;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;

public class CommandHandler extends CommandReceiver<AutoBloodmoon> {
    @SubCommand("kit")
    public KitCommands kitCommands;
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
}
