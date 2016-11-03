package cat.nyaa.autobloodmoon.mobs;

import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;

public class Mob implements ISerializable {
    @Serializable(name = "mobtype")
    private String mobType;
    @Serializable(name = "skills")
    private ArrayList<String> skills;

    public Mob() {

    }

    public Mob(String type, ArrayList<String> skills) {
        this.mobType = type;
        this.skills = skills;
    }

    public String getMobType() {
        return mobType;
    }

    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    public ArrayList<String> getSkills() {
        return skills;
    }

    public void setSkills(ArrayList<String> skills) {
        this.skills = skills;
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
