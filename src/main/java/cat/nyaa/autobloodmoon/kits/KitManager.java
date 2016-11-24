package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class KitManager {
    private AutoBloodmoon plugin;

    public KitManager(AutoBloodmoon pl) {
        plugin = pl;
    }

    public static KitItems.KitType getKitType(String type) {
        for (KitItems.KitType k : KitItems.KitType.values()) {
            if (k.name().equals(type.toUpperCase())) {
                return k;
            }
        }
        return null;
    }

    public KitItems getKitItems(String kitName, KitItems.KitType type) {
        if (plugin.cfg.rewardConfig.kits.containsKey(kitName) &&
                plugin.cfg.rewardConfig.kits.get(kitName).containsKey(type)) {
            return plugin.cfg.rewardConfig.kits.get(kitName).get(type).clone();
        }
        return null;
    }

    public HashMap<KitItems.KitType, KitItems> getKits(String kitName) {
        if (plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            return plugin.cfg.rewardConfig.kits.get(kitName);
        }
        return null;
    }

    public boolean giveKit(String kitName, KitItems.KitType type, Player player) {
        return giveKit(kitName, type, player, false);
    }

    public boolean giveKit(String kitName, KitItems.KitType type, Player player, boolean enderChest) {
        KitItems kitItems = getKitItems(kitName, type);
        if (kitItems != null) {
            Inventory inventory;
            if (!enderChest) {
                inventory = player.getInventory();
            } else {
                inventory = player.getEnderChest();
            }
            List<ItemStack> items = kitItems.getItems();
            ArrayList<Integer> emptySlots = new ArrayList<>();
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i < 36 || i > 39 || !(inventory instanceof PlayerInventory)) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                        emptySlots.add(i);
                    }
                }
            }
            if (emptySlots.size() >= items.size()) {
                for (int i = 0; i < items.size(); i++) {
                    inventory.setItem(emptySlots.get(i), items.get(i));
                }
                if (enderChest) {
                    player.sendMessage(I18n._("user.prefix") + I18n._("user.give.ender_chest"));
                } else {
                    player.sendMessage(I18n._("user.prefix") + I18n._("user.give.inventory"));
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void giveReward(Player player) {
        if (player != null && player.isOnline() && plugin.rewardList.containsKey(player.getUniqueId())) {
            ArrayList<KitItems> reward = plugin.rewardList.get(player.getUniqueId());
            Iterator<KitItems> iter = reward.iterator();
            while (iter.hasNext()) {
                KitItems item = iter.next();
                if (item == null) {
                    iter.remove();
                } else if (plugin.kitManager.giveKit(item.getKitName(), item.getType(), player)) {
                    iter.remove();
                } else if (plugin.kitManager.giveKit(item.getKitName(), item.getType(), player, true)) {
                    iter.remove();
                } else {
                    break;
                }
            }
            if (reward.isEmpty()) {
                plugin.rewardList.remove(player.getUniqueId());
            } else {
                plugin.rewardList.put(player.getUniqueId(), reward);
                player.sendMessage(I18n._("user.prefix") + I18n._("user.give.not_enough_space"));
                player.sendMessage(I18n._("user.prefix") + I18n._("user.reward.acquire"));
            }
        }
    }
}

