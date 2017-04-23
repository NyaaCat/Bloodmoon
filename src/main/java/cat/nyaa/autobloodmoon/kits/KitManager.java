package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitManager {
    private AutoBloodmoon plugin;
    public HashMap<UUID, List<ItemStack>> unacquiredItemList = new HashMap<>();

    public KitManager(AutoBloodmoon pl) {
        plugin = pl;
    }

    public List<ItemStack> getKitItems(String kitName, KitConfig.KitType type) {
        if (plugin.cfg.rewardConfig.kits.containsKey(kitName)) {
            return plugin.cfg.rewardConfig.kits.get(kitName).getKit(type);
        }
        return null;
    }

    public boolean directGiveKit(String kitName, KitConfig.KitType type, Player player) {
        List<ItemStack> kitItems = getKitItems(kitName, type);
        if (kitItems != null) {
            List<ItemStack> remain = giveItemsToPlayer(kitItems, player);
            return remain == null || remain.size() <= 0;
            // return if there's enough space
        }
        return false;
    }

    private List<ItemStack> giveItemsToPlayer(List<ItemStack> items, Player p) {
        Map<Integer, ItemStack> tmp = p.getInventory().addItem(items.toArray(new ItemStack[0]));
        if (tmp == null || tmp.size() < items.size()) {
            p.sendMessage(I18n.format("user.prefix") + I18n.format("user.give.inventory"));
        }
        if (tmp == null || tmp.size() <= 0) {
            return null;
        }
        ItemStack[] remainedItems = tmp.values().toArray(new ItemStack[0]);
        tmp = p.getEnderChest().addItem(remainedItems);
        if (tmp == null || tmp.size() < items.size()) {
            p.sendMessage(I18n.format("user.prefix") + I18n.format("user.give.ender_chest"));
        }
        if (tmp == null || tmp.size() <= 0) {
            return null;
        }
        // Remained Items are returned
        return new ArrayList<>(tmp.values());
    }

    public void applyUnacquiredReward(UUID id) {
        if (id != null && unacquiredItemList.containsKey(id)) {
            Player player = plugin.getServer().getPlayer(id);
            if (player == null) return;
            List<ItemStack> items = unacquiredItemList.get(id);
            items = giveItemsToPlayer(items, player);
            if (items == null || items.size() <= 0) {
                unacquiredItemList.remove(id);
            } else {
                unacquiredItemList.put(id, items);
                player.sendMessage(I18n.format("user.prefix") + I18n.format("user.give.not_enough_space"));
                player.sendMessage(I18n.format("user.prefix") + I18n.format("user.reward.acquire"));
            }
        }
    }

    public void addUnacquiredReward(UUID id, List<ItemStack> items) {
        if (items == null) return;
        List<ItemStack> list = unacquiredItemList.get(id);
        if (list == null) {
            list = new ArrayList<>();
            unacquiredItemList.put(id, list);
        }
        list.addAll(items);
    }
}

