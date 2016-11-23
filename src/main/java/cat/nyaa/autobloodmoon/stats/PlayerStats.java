package cat.nyaa.autobloodmoon.stats;

import cat.nyaa.utils.ISerializable;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerStats implements ISerializable {
    @Serializable
    public String playerUUID;
    @Serializable
    public String playerName = "";
    @Serializable
    public int infernal_kill = 0;
    @Serializable
    public int normal_kill = 0;
    @Serializable
    public int assist = 0;
    @Serializable
    public int death = 0;
    @Serializable
    public int joined = 0;
    @Serializable
    public int wining = 0;

    public PlayerStats() {
    }

    public PlayerStats(OfflinePlayer player) {
        playerUUID = player.getUniqueId().toString();
        playerName = player.getName();
    }

    public PlayerStats(UUID uuid) {
        playerUUID = uuid.toString();
    }

    public UUID getUUID() {
        return UUID.fromString(playerUUID);
    }

    public void incrementStats(StatsType type) {
        incrementStats(type, 1);

    }

    public void incrementStats(StatsType type, int amount) {
        if (amount > 0) {
            switch (type) {
                case NORMAL_KILL:
                    normal_kill += amount;
                    break;
                case INFERNAL_KILL:
                    infernal_kill += amount;
                    break;
                case ASSIST:
                    assist += amount;
                    break;
                case DEATH:
                    death += amount;
                    break;
                case JOINED:
                    joined += amount;
                    break;
                case WINING:
                    wining += amount;
                    break;
            }
        }
    }

    public void add(PlayerStats stats) {
        this.playerName = stats.playerName;
        this.wining += stats.wining;
        this.death += stats.death;
        this.joined += stats.joined;
        this.normal_kill += stats.normal_kill;
        this.infernal_kill += stats.infernal_kill;
        this.assist += stats.assist;
    }

    public PlayerStats clone() {
        PlayerStats stats = new PlayerStats();
        stats.playerUUID = playerUUID;
        stats.playerName = playerName;
        stats.wining = wining;
        stats.death = death;
        stats.joined = joined;
        stats.normal_kill = normal_kill;
        stats.infernal_kill = infernal_kill;
        stats.assist = assist;
        return stats;
    }

    public enum StatsType {INFERNAL_KILL, NORMAL_KILL, ASSIST, DEATH, JOINED, WINING}
}
