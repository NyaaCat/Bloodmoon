package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class KitListener implements Listener {
    static class Pair<A, B> {
        public Pair(A a, B b) {
            first = a;
            second = b;
        }

        final public A first;
        final public B second;
    }

    public final HashMap<Player, Pair<String, KitConfig.KitType>> selectChest = new HashMap<>();
    private final AutoBloodmoon plugin;

    public KitListener(AutoBloodmoon plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (selectChest.containsKey(e.getPlayer())) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = e.getClickedBlock();
                if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
                    Player p = e.getPlayer();
                    String kitName = selectChest.get(p).first;
                    KitConfig.KitType type = selectChest.get(p).second;
                    e.setCancelled(true);
                    Chest chest = (Chest) block.getState();
                    KitConfig kit = plugin.cfg.rewardConfig.kits.get(kitName);
                    if (kit == null) {
                        kit = new KitConfig();
                        plugin.cfg.rewardConfig.kits.put(kitName, kit);
                    }
                    kit.setKit(type, chest.getInventory().getContents());
                    plugin.cfg.rewardConfig.save();
                    e.getPlayer().sendMessage(I18n._("user.kit.save_success"));
                }
                selectChest.remove(e.getPlayer());
            }
        }
    }
}

