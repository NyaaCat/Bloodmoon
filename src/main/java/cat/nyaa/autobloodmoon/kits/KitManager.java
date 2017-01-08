package cat.nyaa.autobloodmoon.kits;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class KitManager {
    private AutoBloodmoon plugin;
    public HashMap<UUID, List<ItemStack>> rewardList = new HashMap<>();

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

    public boolean directGiveKit(String kitName, KitItems.KitType type, Player player) {
        KitItems kitItems = getKitItems(kitName, type);
        if (kitItems != null) {
            List<ItemStack> remain = giveItemsToPlayer(kitItems.getItems(), player);
            return remain == null || remain.size() <= 0;
            // return if there's enough space
        }
        return false;
    }

    private List<ItemStack> giveItemsToPlayer(List<ItemStack> items, Player p) {
        Map<Integer, ItemStack> tmp = p.getInventory().addItem(items.toArray(new ItemStack[0]));
        if (tmp == null || tmp.size() < items.size()) {
            p.sendMessage(I18n._("user.prefix") + I18n._("user.give.inventory"));
        }
        if (tmp == null || tmp.size() <= 0) {
            return null;
        }
        ItemStack[] remainedItems = tmp.values().toArray(new ItemStack[0]);
        tmp = p.getEnderChest().addItem(remainedItems);
        if (tmp == null || tmp.size() < items.size()) {
            p.sendMessage(I18n._("user.prefix") + I18n._("user.give.ender_chest"));
        }
        if (tmp == null || tmp.size() <= 0) {
            return null;
        }
        // Remained Items are returned
        return new ArrayList<>(tmp.values());
    }

    public void applyRewardFromList(Player player) {
        if (player != null && player.isOnline() && rewardList.containsKey(player.getUniqueId())) {
            UUID id = player.getUniqueId();
            List<ItemStack> items = rewardList.get(id);
            items = giveItemsToPlayer(items, player);
            if (items == null || items.size() <= 0) {
                rewardList.remove(id);
            } else {
                rewardList.put(id, items);
                player.sendMessage(I18n._("user.prefix") + I18n._("user.give.not_enough_space"));
                player.sendMessage(I18n._("user.prefix") + I18n._("user.reward.acquire"));
            }
        }
    }

    public void addRewardToList(UUID id, String kitName, KitItems.KitType kitType) {
        KitItems rewardKit = getKitItems(kitName, kitType);
        if (rewardKit == null) return;
        List<ItemStack> clonedItems = rewardKit.getItems().stream().map(ItemStack::clone).collect(Collectors.toList());
        if (rewardList.containsKey(id)) {
            rewardList.get(id).addAll(clonedItems);
        } else {
            rewardList.put(id, new ArrayList<>(clonedItems));
        }
    }
}

