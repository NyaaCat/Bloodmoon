package cat.nyaa.autobloodmoon;

import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;

public class CommandHandler extends CommandReceiver<AutoBloodmoon> {
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
