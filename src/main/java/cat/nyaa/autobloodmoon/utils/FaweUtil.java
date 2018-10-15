package cat.nyaa.autobloodmoon.utils;

import cat.nyaa.autobloodmoon.AutoBloodmoon;
import cat.nyaa.autobloodmoon.I18n;
import cat.nyaa.autobloodmoon.arena.Arena;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
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

    public static boolean pasteSchematic(CommandSender sender, Arena arena, String fileName) {
        try {
            if (sender == null || (sender instanceof Player && !((Player) sender).isOnline())) {
                sender = Bukkit.getConsoleSender();
            }
            File file = new File(getSchematicsDir(), fileName);
            if (file.exists() && file.isFile()) {
                Location center = arena.getCenterPoint();
                Vector to = new Vector(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                ClipboardFormat format = ClipboardFormats.findByFile(file);
                if (format == null) return false;
                Clipboard read = format.getReader(new FileInputStream(file)).read();
                ClipboardHolder clipboardHolder = new ClipboardHolder(read);
                session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(center.getWorld()), Integer.MAX_VALUE);
                Operation build = clipboardHolder.createPaste(session).build();
                Operations.complete(build);
                // TODO
                // Schematic schematic = FaweAPI.load(file);
                // session = schematic.paste(BukkitAdapter.adapt(center.getWorld()), to, true, true, (Transform) null);
                sender.sendMessage(I18n.format("user.schematic.pasted", to.toString()));
                return true;
            } else {
                sender.sendMessage(I18n.format("user.schematic.not_exist", file.getCanonicalPath()));
            }
        } catch (IOException | WorldEditException e) {
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
