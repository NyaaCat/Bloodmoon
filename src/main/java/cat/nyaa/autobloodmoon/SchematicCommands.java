package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.arena.Arena;
import cat.nyaa.autobloodmoon.arena.ArenaCommands;
import cat.nyaa.autobloodmoon.utils.FaweUtil;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.command.CommandSender;

public class SchematicCommands extends CommandReceiver {
    private AutoBloodmoon plugin;

    public SchematicCommands(Object plugin, LanguageRepository i18n) {
        super((AutoBloodmoon) plugin, i18n);
        this.plugin = (AutoBloodmoon) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "schematic";
    }

    @SubCommand(value = "paste", permission = "bm.admin")
    public void commandPaste(CommandSender sender, Arguments args) {
        if (args.length() < 4) {
            msg(sender, "manual.schematic.paste.usage");
            return;
        }
        Arena arena = ArenaCommands.getArena(args.nextString());
        FaweUtil.pasteSchematic(sender, arena, args.nextString());
    }

    @SubCommand(value = "undo", permission = "bm.admin")
    public void commandUndo(CommandSender sender, Arguments args) {
        FaweUtil.undo(sender);
    }
}
