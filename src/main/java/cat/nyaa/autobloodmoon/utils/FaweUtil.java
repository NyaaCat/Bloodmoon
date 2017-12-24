package cat.nyaa.autobloodmoon.utils;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.transform.Transform;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class FaweUtil {
    private static EditSession session;

    public static File getSchematicsDir() {
        File dir = new File(AutoBloodmoon.instance.cfg.schematicsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean hasFawe() {
        return Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    public static boolean pasteSchematic(CommandSender sender, Arena arena, String fileName) {
        try {
            if (sender == null || (sender instanceof Player && !((Player) sender).isOnline())) {
                sender = Bukkit.getConsoleSender();
            }
            File file = new File(getSchematicsDir(), fileName);
            if (file.exists() && file.isFile()) {
                Location center = arena.getCenterPoint();
                BukkitWorld bukkitWorld = new BukkitWorld(center.getWorld());
                Vector to = new Vector(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                Schematic schematic = FaweAPI.load(file);
                session = schematic.paste(bukkitWorld, to, true, true, (Transform) null);
                sender.sendMessage(I18n.format("user.schematic.pasted", to.toString()));
                return true;
            } else {
                sender.sendMessage(I18n.format("user.schematic.not_exist", file.getCanonicalPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void undo(CommandSender sender) {
        if (session != null) {
            session.undo(session);
            sender.sendMessage(I18n.format("user.schematic.undo"));
        }
    }
}
