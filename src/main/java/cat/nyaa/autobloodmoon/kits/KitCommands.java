package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KitCommands extends CommandReceiver<AutoBloodmoon> {
    private AutoBloodmoon plugin;

    public KitCommands(Object plugin, Internationalization i18n) {
        super((AutoBloodmoon) plugin, i18n);
        this.plugin = (AutoBloodmoon) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "kit";
    }

    @SubCommand(value = "create", permission = "bm.admin")
    public void commandCreateKit(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.kit.create.usage");
            return;
        }
        Player player = asPlayer(sender);
        String kitName = args.next();
        KitConfig.KitType kitType = args.nextEnum(KitConfig.KitType.class);
        if (!plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            plugin.cfg.rewardConfig.kits.put(kitName, new KitConfig());
        }
        plugin.kitListener.selectChest.put(player, new KitListener.Pair<>(kitName, kitType));
        msg(player, "user.kit.right_click_chest");
    }

    @SubCommand(value = "view", permission = "bm.admin")
    public void commandViewKit(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.kit.view.usage");
            return;
        }
        Player player = asPlayer(sender);
        String kitName = args.next();
        KitConfig.KitType type = args.nextEnum(KitConfig.KitType.class);
        if (!plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            msg(sender, "user.kit.not_found");
            return;
        }
        List<ItemStack> items = plugin.cfg.rewardConfig.kits.get(kitName).getKit(type);
        if (items == null) {
            msg(sender, "user.kit.not_found");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, kitName + " - " + type.name());
        inv.setContents(items.toArray(new ItemStack[0]));
        player.openInventory(inv);
    }

    @SubCommand(value = "remove", permission = "bm.admin")
    public void commandRemoveKit(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.kit.remove.usage");
            return;
        }
        Player player = asPlayer(sender);
        String kitName = args.next();
        KitConfig.KitType kitType = args.nextEnum(KitConfig.KitType.class);
        if (!plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            msg(sender, "user.kit.not_found");
            return;
        }
        if (!plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            msg(sender, "user.kit.not_found");
            return;
        }
        plugin.cfg.rewardConfig.kits.get(kitName).setKit(kitType, null);
        plugin.cfg.rewardConfig.save();
        msg(player, "user.kit.save_success");
    }

    @SubCommand(value = "list", permission = "bm.admin")
    public void commandListKit(CommandSender sender, Arguments args) {
        for (String kitName : plugin.cfg.rewardConfig.kits.keySet()) {
            sender.sendMessage("Kit: " + kitName);
        }
    }

    @SubCommand(value = "give", permission = "bm.admin")
    public void commandGiveKit(CommandSender sender, Arguments args) {
        if (args.length() >= 4) {
            String kitName = args.next();
            KitConfig.KitType kitType = args.nextEnum(KitConfig.KitType.class);
            if (kitType == null) {
                msg(sender, "user.kit.kit_type_error");
                return;
            }
            if (plugin.kitManager.getKitItems(kitName, kitType) == null) {
                msg(sender, "user.kit.not_found");
                return;
            }
            Player player;
            if (args.length() == 5) {
                player = Bukkit.getPlayer(args.next());
            } else {
                player = asPlayer(sender);
            }
            if (player != null && player.isOnline()) {
                if (plugin.kitManager.getKitItems(kitName, kitType) != null) {
                    msg(sender, "user.kit.not_found");
                } else if (plugin.kitManager.directGiveKit(kitName, kitType, player)) {
                    msg(sender, "user.kit.give_success", kitName, kitType.toString(), player.getName());
                } else {
                    msg(sender, "user.give.not_enough_space");
                }
                return;
            }
            msg(sender, "user.info.player_not_found");
        } else {
            msg(sender, "manual.kit.give.usage");
        }
    }
}
