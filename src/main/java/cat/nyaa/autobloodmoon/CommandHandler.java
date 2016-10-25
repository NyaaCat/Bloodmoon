package cat.nyaa.autobloodmoon;

import cat.nyaa.autobloodmoon.kits.KitCommands;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;

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

}
