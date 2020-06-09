package net.dohaw.play.islandworlds.managers;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.World;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicLoader {

    private IslandWorlds plugin;
    private File desertSchematicFile, oceanSchematicFile, mycelSchematicFile;
    private Clipboard desertPaste, oceanPaste, mycelPaste;
    private World desertWorld, oceanWorld, mycelWorld;
    private ConfigManager cm;

    /*
        Class made to load things into memory for easy use and speed.
     */
    public SchematicLoader(IslandWorlds plugin){
        this.plugin = plugin;
        this.cm = new ConfigManager(plugin);
    }

    public void load(){

        plugin.getLogger().info("Loading schematics into memory...");
        loadSchematicFiles();
        try {
            loadClipboards();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadClipboards() throws IOException {

        org.bukkit.World oceanBukkitWorld = Bukkit.getWorld(cm.getWorld(PortalTypes.OCEAN));
        if(oceanBukkitWorld != null){
            this.oceanWorld = BukkitUtil.getLocalWorld(oceanBukkitWorld);
            ClipboardFormat format = ClipboardFormat.findByFile(oceanSchematicFile);
            ClipboardReader reader = format.getReader(new FileInputStream(oceanSchematicFile));
            this.oceanPaste = reader.read(oceanWorld.getWorldData());
        }else{
            plugin.getLogger().severe("Failed to load the Ocean schematic clipboard. The world isn't valid!");
        }

        org.bukkit.World mycelBukkitWorld = Bukkit.getWorld(cm.getWorld(PortalTypes.MYCEL));
        if(mycelBukkitWorld != null){
            this.mycelWorld = BukkitUtil.getLocalWorld(mycelBukkitWorld);
            ClipboardFormat format = ClipboardFormat.findByFile(mycelSchematicFile);
            ClipboardReader reader = format.getReader(new FileInputStream(mycelSchematicFile));
            this.mycelPaste = reader.read(mycelWorld.getWorldData());
        }else{
            plugin.getLogger().severe("Failed to load the Mycel schematic clipboard. The world isn't valid!");
        }

        org.bukkit.World desertBukkitWorld = Bukkit.getWorld(cm.getWorld(PortalTypes.DESERT));
        if(desertBukkitWorld != null){
            this.desertWorld = BukkitUtil.getLocalWorld(desertBukkitWorld);
            ClipboardFormat format = ClipboardFormat.findByFile(desertSchematicFile);
            ClipboardReader reader = format.getReader(new FileInputStream(desertSchematicFile));
            this.desertPaste = reader.read(desertWorld.getWorldData());
        }else{
            plugin.getLogger().severe("Failed to load the Desert schematic clipboard. The world isn't valid!");
        }

    }

    /*
        Loads the schematic files into memory for easy use
     */
    private void loadSchematicFiles(){

        this.desertSchematicFile = new File(plugin.getDataFolder() + File.separator + "/schematics", "desert.gz");
        this.oceanSchematicFile = new File(plugin.getDataFolder() + File.separator + "/schematics", "ocean1.schematic");
        this.mycelSchematicFile = new File(plugin.getDataFolder() + File.separator + "/schematics", "mycel1.schematic");

    }

    public Clipboard getClipboard(PortalTypes portalType){
        switch(portalType){
            case OCEAN:
                return oceanPaste;
            case MYCEL:
                return mycelPaste;
            case DESERT:
                return desertPaste;
            default:
                return null;
        }
    }

    public World getWorld(PortalTypes portalType){
        switch(portalType){
            case OCEAN:
                return oceanWorld;
            case MYCEL:
                return mycelWorld;
            case DESERT:
                return desertWorld;
            default:
                return null;
        }
    }

}
