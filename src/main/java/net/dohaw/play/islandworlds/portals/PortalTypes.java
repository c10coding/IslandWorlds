package net.dohaw.play.islandworlds.portals;

import net.dohaw.play.islandworlds.files.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PortalTypes {

    DESERT("Desert", "islandworlds.access.desert", Material.SANDSTONE, "Owned", "&e&lSand Boss", "Sand.Boss"),
    OCEAN("Ocean", "islandworlds.access.ocean" , Material.PRISMARINE, "davdd_", "&b&lWater Boss", "Water.Boss"),
    MYCEL("Mycel","islandworlds.access.mycel" , Material.HUGE_MUSHROOM_1, "Akarnell", "&4Mushroom Boss", "Mushroom.Boss");

    private String name, accessPermission, npcSkinName, npcName, bossCommandName;
    private Material material;

    PortalTypes(String name, String accessPermission, Material material, String npcSkinName, String npcName, String bossCommandName){
        this.name = name;
        this.material = material;
        this.accessPermission = accessPermission;
        this.npcSkinName = npcSkinName;
        this.npcName = npcName;
        this.bossCommandName = bossCommandName;
    }

    public String getName(){
        return name;
    }

    public Material getMaterial(){
        return material;
    }

    public String getNpcName(){
        return npcName;
    }

    public String getAccessPermission(){
        return accessPermission;
    }

    public String getBossCommandName(){
        return bossCommandName;
    }

    public String getNpcSkinName(){
        return npcSkinName;
    }

    public static PortalTypes getType(Material mat){
        List<PortalTypes> types = getTypes();
        for(PortalTypes type : types){
            if(type.material.equals(mat)){
                return type;
            }
        }
        return null;
    }

    public static PortalTypes getType(String portalName){
        List<PortalTypes> types = getTypes();
        for(PortalTypes type : types){
            if(type.name.equalsIgnoreCase(portalName)){
                return type;
            }
        }
        return null;
    }

    public List<Material> getGeneratedMaterials(){
        List<Material> materials = new ArrayList<>();
        switch(this){
            case OCEAN:
                materials.add(Material.PRISMARINE);
                materials.add(Material.SEA_LANTERN);
                break;
            case DESERT:
                materials.add(Material.STAINED_CLAY);
                materials.add(Material.STONE);
                materials.add(Material.WOOD);
                break;
            case MYCEL:
                materials.add(Material.HUGE_MUSHROOM_2);
                materials.add(Material.HUGE_MUSHROOM_1);
                materials.add(Material.STAINED_CLAY);
                break;
        }
        return materials;
    }

    public static PortalTypes getType(ConfigManager cm, World world){
        for(PortalTypes type : PortalTypes.values()){
            if(cm.getWorld(type).equalsIgnoreCase(world.getName())){
                return type;
            }
        }
        return null;
    }

    public Location getCobbleGenBlock(Location spawnLocation){
        switch(this){
            case DESERT:
                return spawnLocation.subtract(1, 1, 8);
            case OCEAN:
                return spawnLocation.subtract(3,1,4 );
            case MYCEL:
                return spawnLocation.subtract(5, 0, 5);
        }
        return null;
    }

    public static List<PortalTypes> getTypes(){
        return Arrays.asList(PortalTypes.values());
    }

    public static List<Material> getMaterials(){
        List<Material> materials = new ArrayList<>();
        for(PortalTypes pt : PortalTypes.values()){
            materials.add(pt.material);
        }
        return materials;
    }

}
