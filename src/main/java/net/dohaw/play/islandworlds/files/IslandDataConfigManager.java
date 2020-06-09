package net.dohaw.play.islandworlds.files;

import me.c10coding.coreapi.files.ConfigManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandDataConfigManager extends ConfigManager {

    public IslandDataConfigManager(JavaPlugin plugin) {
        super(plugin, "islandData.yml");
    }

    public enum DataKeys{
        PURCHASED,
        UNLOCKED
    }

    public void addPlayerToFile(UUID u){

        config.set("Data." + u.toString() + ".Purchased.Ocean", false);
        config.set("Data." + u.toString() + ".Purchased.Mycel", false);
        config.set("Data." + u.toString() + ".Purchased.Desert", false);

        config.set("Data." + u.toString() + ".Unlocked.Ocean", false);
        config.set("Data." + u.toString() + ".Unlocked.Mycel", false);
        config.set("Data." + u.toString() + ".Unlocked.Desert", false);

        List<String> membersWithAccess = new ArrayList<>();
        membersWithAccess.add(u.toString());
        config.set("Data." + u.toString() + ".Members With Access", membersWithAccess);
        saveConfig();
    }

    public boolean isPlayerInFile(UUID u){
        return config.getString("Data." + u.toString() + ".Purchased.Ocean") != null;
    }

    /*
        Sets something as purchased or unlocked
     */
    public void set(UUID u, DataKeys key, PortalTypes portalType, boolean value){
        if (key.equals(DataKeys.PURCHASED)) {
            config.set("Data." + u.toString() + ".Purchased." + portalType.getName(), value);
        } else {
            config.set("Data." + u.toString() + ".Unlocked." + portalType.getName(), value);
        }
        saveConfig();
    }

    /*
        Checks to see if something is either purchased or unlocked.
     */
    public boolean is(UUID u, DataKeys key, PortalTypes portalType){
        return key.equals(DataKeys.PURCHASED) ?
                config.getBoolean("Data." + u.toString() + ".Purchased." + portalType.getName()) :
                config.getBoolean("Data." + u.toString() + ".Unlocked." + portalType.getName());
    }

    public void setBossCooldown(UUID u, PortalTypes portalType){
        net.dohaw.play.islandworlds.files.ConfigManager cm = new net.dohaw.play.islandworlds.files.ConfigManager(plugin);
        int cooldownInHours = cm.getBossCooldown();
        int cooldownInMinutes = cooldownInHours * 60;
        config.set("Data." + u.toString() + "." + portalType.getName() + ".BossCooldown", cooldownInMinutes);
        saveConfig();
    }

    public void decreaseBossCooldown(UUID u, PortalTypes portalType){
        int cooldownInMinutes = config.getInt("Data." + u.toString() + "." + portalType.getName() + ".BossCooldown");
        cooldownInMinutes--;
        config.set("Data." + u.toString() + "." + portalType.getName() + ".BossCooldown", cooldownInMinutes);
        saveConfig();
    }

    public int getCooldownTime(UUID u, PortalTypes portalType){
        return config.getInt("Data." + u.toString() + "." + portalType.getName() + ".BossCooldown");
    }

    public void createNewIsland(UUID u, PortalTypes portalType, Location locationForIsland){
        config.set("Data." + u.toString() + "." + portalType.getName() + ".Location.World", locationForIsland.getWorld().getName());
        config.set("Data." + u.toString() + "." + portalType.getName() + ".Location.X", locationForIsland.getBlockX());
        config.set("Data." + u.toString() + "." + portalType.getName() + ".Location.Y", locationForIsland.getBlockY());
        config.set("Data." + u.toString() + "." + portalType.getName() + ".Location.Z", locationForIsland.getBlockZ());
        saveConfig();
    }

    public Location getIslandLocation(PortalTypes portalType, UUID u){

        String worldString = config.getString("Data." + u.toString() + "." + portalType.getName() + ".Location.World");
        if(worldString == null){
            return null;
        }
        World world = Bukkit.getWorld(worldString);

        int x = config.getInt("Data." + u.toString() + "." + portalType.getName() + ".Location.X");
        int y = config.getInt("Data." + u.toString() + "." + portalType.getName() + ".Location.Y");
        int z = config.getInt("Data." + u.toString() + "." + portalType.getName() + ".Location.Z");

        return new Location(world,x,y,z);
    }

    public List<String> getMembersWithAccess(UUID u){
        return config.getStringList("Data." + u.toString() + ".Members With Access");
    }

    public void addMember(UUID u, UUID playerToAdd){
        List<String> currentMembers = getMembersWithAccess(u);
        currentMembers.add(playerToAdd.toString());
        config.set("Data." + u.toString() + ".Members With Access", currentMembers);
        saveConfig();
    }

    public void removeMember(UUID u, UUID playerToRemove){
        List<String> currentMembers = getMembersWithAccess(u);
        currentMembers.remove(playerToRemove.toString());
        config.set("Data." + u.toString() + ".Members With Access", currentMembers);
        saveConfig();
    }

    /*
        Is null if the player is on an island and the owner has possibly refunded their rank and lost permission to unlock that island from that point on.
     */
    public UUID getOwnerFromLocation(Location blockOnIsland, PortalTypes portalType){
        for(Location spawnLocation : getWorldIslandLocations(portalType)){
            if(spawnLocation.distance(blockOnIsland) <= 50){
                return getOwnerFromSpawnLocation(spawnLocation, portalType);
            }
        }
        return null;
    }

    /*
        Is null if there isn't anyone in the data file.
     */
    public UUID getOwnerFromSpawnLocation(Location spawnLocation, PortalTypes portalType){
        ConfigurationSection uuidSection = config.getConfigurationSection("Data");

        if(uuidSection != null){
            for(String uuid : uuidSection.getKeys(false)){
                UUID u = UUID.fromString(uuid);
                if(is(u, DataKeys.UNLOCKED, portalType)){
                    Location islandSpawnLocation = getIslandLocation(portalType, u);
                    if(islandSpawnLocation.equals(spawnLocation)){
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public List<Location> getWorldIslandLocations(PortalTypes portalType){
        ConfigurationSection uuidSection = config.getConfigurationSection("Data");
        List<Location> locationsOfIslands = new ArrayList<>();
        if(uuidSection != null){
            for(String uuid : uuidSection.getKeys(false)){
                UUID u = UUID.fromString(uuid);
                //If the UUID has the island unlocked, then it should be generated and should have a location
                if(is(u, DataKeys.UNLOCKED, portalType)){
                    if(getIslandLocation(portalType, u) != null){
                        locationsOfIslands.add(getIslandLocation(portalType, u));
                    }
                }
            }
        }
        return locationsOfIslands;
    }



}
