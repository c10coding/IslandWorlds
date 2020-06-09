package net.dohaw.play.islandworlds.files;

import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager extends me.c10coding.coreapi.files.ConfigManager {

    public ConfigManager(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }

    public String getWorld(PortalTypes portalType){
        return config.getString("Worlds." + portalType.getName());
    }

    public int getCost(PortalTypes portalType){
        return config.getInt("Costs." + portalType.getName());
    }

    public String getPluginPrefix(){
        return config.getString("PluginPrefix");
    }

    public int getMaxRange(){
        return config.getInt("MaxRange");
    }

    public int getMinRange(){
        return config.getInt("MinRange");
    }

    public int getBossCooldown(){
        return config.getInt("BossCooldown");
    }


}
