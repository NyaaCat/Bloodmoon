package cat.nyaa.autobloodmoon.level;

import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

public class Level implements ISerializable {
    @Serializable
    private String levelType;
    @Serializable
    private int minPlayerAmount;
    @Serializable
    private int mobAmount;
    @Serializable
    private int infernalAmount;
    @Serializable
    private int mobSpawnDelayTicks;
    @Serializable
    private int maxInfernalLevel;

    public Level() {

    }

    public String getLevelType() {
        return levelType;
    }

    public void setLevelType(String level) {
        this.levelType = level;
    }

    public int getMinPlayerAmount() {
        return minPlayerAmount;
    }

    public void setMinPlayerAmount(int minPlayerAmount) {
        this.minPlayerAmount = minPlayerAmount;
    }

    public int getMobAmount() {
        return mobAmount;
    }

    public void setMobAmount(int mobAmount) {
        this.mobAmount = mobAmount;
    }

    public int getInfernalAmount() {
        return infernalAmount;
    }

    public void setInfernalAmount(int infernalAmount) {
        this.infernalAmount = infernalAmount;
    }

    public int getMobSpawnDelayTicks() {
        return mobSpawnDelayTicks;
    }

    public void setMobSpawnDelayTicks(int mobSpawnDelayTicks) {
        this.mobSpawnDelayTicks = mobSpawnDelayTicks;
    }

    public int getMaxInfernalLevel() {
        return maxInfernalLevel;
    }

    public void setMaxInfernalLevel(int maxInfernalLevel) {
        this.maxInfernalLevel = maxInfernalLevel;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }
}
