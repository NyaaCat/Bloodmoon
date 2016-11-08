package cat.nyaa.autobloodmoon.utils;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import com.earth2me.essentials.Essentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportUtil {
    private AutoBloodmoon plugin;
    private Essentials ess;

    public TeleportUtil(AutoBloodmoon pl) {
        this.plugin = pl;
        if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        }
    }

    public void Teleport(Player player, Location loc) {
        if (!player.isOnline()) {
            return;
        }
        if (ess != null) {
            try {
                ess.getUser(player).getTeleport().now(loc, false, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            player.setFallDistance(0);
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    public void Teleport(List<Player> players, Location loc) {
        for (Player p : players) {
            Teleport(p, loc);
        }
    }
}
