package net.dohaw.play.islandworlds.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import net.dohaw.play.islandworlds.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.w3c.dom.events.EventException;

import java.util.List;

public class IslandManager {

    private IslandWorlds plugin;
    private PortalTypes portalType;
    private Clipboard paste;
    private World world;
    private IslandDataConfigManager idcm;
    final private int ISLAND_DISTANCE = 500;
    private Player player;
    private Location spawnLocation;

    public IslandManager(IslandWorlds plugin, PortalTypes portalType, Player player, IslandDataConfigManager idcm){
        this.plugin = plugin;
        this.portalType = portalType;
        this.paste = plugin.getSchemLoader().getClipboard(portalType);
        this.world = plugin.getSchemLoader().getWorld(portalType);
        this.idcm = idcm;
        this.player = player;
    }

    public void generateIsland(){

        List<Location> currentWorldIslandLocations = idcm.getWorldIslandLocations(portalType);
        Location generatedLocation = null;
        if(!currentWorldIslandLocations.isEmpty()){
            boolean isValidLocation = false;
            while(!isValidLocation){
                generatedLocation = getNewIslandLocation();
                isValidLocation = isValidLocation(currentWorldIslandLocations, generatedLocation);
            }
        }else{
            generatedLocation = getNewIslandLocation();
        }
        this.spawnLocation = generatedLocation;
        idcm.createNewIsland(player.getUniqueId(), portalType, generatedLocation);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, Integer.MAX_VALUE);
        editSession.enableQueue();
        editSession.setFastMode(true);
        Operation operation = new ClipboardHolder(paste, world.getWorldData()).createPaste(editSession, world.getWorldData()).to(new com.sk89q.worldedit.Vector(generatedLocation.getX(), generatedLocation.getY(), generatedLocation.getZ())).ignoreAirBlocks(false).build();

        try {
            Operations.complete(operation);
            editSession.flushQueue();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }

        spawnNPC();

    }

    public Location getNewIslandLocation(){

        ConfigManager cm = new ConfigManager(plugin);
        int minRange = cm.getMinRange();
        int maxRange = cm.getMaxRange();

        int x = Utils.getRandomNumberInRange(minRange, maxRange);
        int y = 80;
        int z = Utils.getRandomNumberInRange(minRange, maxRange);

        return new Location(Bukkit.getWorld(cm.getWorld(portalType)), x, y, z);
    }

    private boolean isValidLocation(List<Location> locations, Location generatedLocation){
        for(Location loc : locations){
            if(loc.distance(generatedLocation) < ISLAND_DISTANCE){
                return false;
            }
        }
        return true;
    }

    private void spawnNPC(){
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC boss = registry.createNPC(EntityType.PLAYER, Utils.colorString(portalType.getNpcName()));
        switch(portalType){
            case DESERT:
                spawnLocation.add(0, 1, 0);
                spawnLocation.add(0, 0, 2);
                spawnLocation.setYaw(spawnLocation.getYaw() + 180);
                break;
            case OCEAN:
                spawnLocation.add(5, 1, 0);
                spawnLocation.setYaw(spawnLocation.getYaw() + 45);
                break;
            case MYCEL:
                spawnLocation.add(2, 0, 7);
                spawnLocation.setYaw(spawnLocation.getYaw() + 180);
                break;
        }

        boss.spawn(spawnLocation);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "npc select " + boss.getId();
        String command2 = "npc skin " + portalType.getNpcSkinName() + " -l";
        Bukkit.dispatchCommand(console, command);
        Bukkit.dispatchCommand(console, command2);

        idcm.storeNPCID(boss.getId(), player.getUniqueId(), portalType);
        idcm.storeNPCLocation(player.getUniqueId(), portalType, spawnLocation);
    }

}
