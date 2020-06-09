package net.dohaw.play.islandworlds.events;

import me.c10coding.coreapi.chat.Chat;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.files.MessagesConfigManager;
import net.dohaw.play.islandworlds.managers.IslandManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import net.dohaw.play.islandworlds.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import java.util.ArrayList;
import java.util.List;

public class PortalListener implements Listener {

    private IslandWorlds plugin;
    private List<Material> portalMaterials = PortalTypes.getMaterials();
    private Chat chatFactory;

    public PortalListener(IslandWorlds plugin){
        this.plugin = plugin;
        this.chatFactory = plugin.getApi().getChatFactory();
    }

    @EventHandler
    public void southLeftSide(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem().getType().equals(Material.FLINT_AND_STEEL)){
            if(portalMaterials.contains(e.getClickedBlock().getType())){

                Material matOfPortal = e.getClickedBlock().getType();
                Location locationOfFire = e.getClickedBlock().getLocation().add(0, 1, 0);

                Location firstCheckedLocation = locationOfFire.subtract(1, 0, 0);
                Material matOfCheckedBlock = firstCheckedLocation.getBlock().getType();
                if(matOfCheckedBlock.equals(Material.AIR)){
                    Location secondCheckedLocation = locationOfFire.add(2, 0, 0);
                    Material matOfSecondCheckBlock = secondCheckedLocation.getBlock().getType();
                    /*
                        The flint and steel was on the left side of a potential portal
                        The right block is air, but the block to the left of the fire is the material of a potential portal
                     */
                    if(matOfSecondCheckBlock.equals(matOfPortal)){
                        northSouthCheck(locationOfFire, matOfPortal);
                    }
                }
            }
        }
    }

    @EventHandler
    public void southRightSide(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem().getType().equals(Material.FLINT_AND_STEEL)) {
            if (portalMaterials.contains(e.getClickedBlock().getType())) {

                Material matOfPortal = e.getClickedBlock().getType();
                Location locationOfFire = e.getClickedBlock().getLocation().add(0, 1, 0);

                Location firstCheckedLocation = locationOfFire.add(1, 0, 0);
                Material matOfCheckedBlock = firstCheckedLocation.getBlock().getType();
                if (matOfCheckedBlock.equals(Material.AIR)) {
                    Location secondCheckedLocation = locationOfFire.add(1, 0, 0);
                    Material matOfSecondCheckBlock = secondCheckedLocation.getBlock().getType();

                     /*
                        The flint and steel was on the right side of a potential portal
                        It gets the location in the same postion as the other method
                     */
                    if (matOfSecondCheckBlock.equals(matOfPortal)) {
                        northSouthCheck(locationOfFire, matOfPortal);
                    }
                }
            }
        }
    }

    @EventHandler
    public void westLeftSide(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem().getType().equals(Material.FLINT_AND_STEEL)) {
            if (portalMaterials.contains(e.getClickedBlock().getType())) {

                Material matOfPortal = e.getClickedBlock().getType();
                Location locationOfFire = e.getClickedBlock().getLocation().add(0, 1, 0);

                Location firstCheckedLocation = locationOfFire.subtract(0, 0, 1);
                Material matOfCheckedBlock = firstCheckedLocation.getBlock().getType();
                if(matOfCheckedBlock.equals(Material.AIR)){
                    Location secondCheckedLocation = locationOfFire.add(0, 0, 2);
                    Material matOfSecondCheckBlock = secondCheckedLocation.getBlock().getType();
                    /*
                        The flint and steel was on the left side of a potential portal
                        The right block is air, but the block to the left of the fire is the material of a potential portal
                     */
                    if(matOfSecondCheckBlock.equals(matOfPortal)){
                        eastWestCheck(locationOfFire, matOfPortal);
                    }
                }
            }
        }
    }

    @EventHandler
    public void westRightSide(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem().getType().equals(Material.FLINT_AND_STEEL)) {
            if (portalMaterials.contains(e.getClickedBlock().getType())) {

                Material matOfPortal = e.getClickedBlock().getType();
                Location locationOfFire = e.getClickedBlock().getLocation().add(0, 1, 0);

                Location firstCheckedLocation = locationOfFire.add(0, 0, 1);
                Material matOfCheckedBlock = firstCheckedLocation.getBlock().getType();
                if(matOfCheckedBlock.equals(Material.AIR)){
                    Location secondCheckedLocation = locationOfFire.add(0, 0, 1);
                    Material matOfSecondCheckBlock = secondCheckedLocation.getBlock().getType();
                    /*
                        The flint and steel was on the left side of a potential portal
                        The right block is air, but the block to the left of the fire is the material of a potential portal
                     */
                    if(matOfSecondCheckBlock.equals(matOfPortal)){
                        eastWestCheck(locationOfFire, matOfPortal);
                    }
                }
            }
        }
    }

    public void northSouthCheck(Location locationOfFire, Material matOfPortal){
        for(int x = 0; x < 2; x++){
            locationOfFire.setY(locationOfFire.getY() + 1);
            if(!locationOfFire.getBlock().getType().equals(matOfPortal)){
                return;
            }
        }

        locationOfFire.setX(locationOfFire.getX() - 1);
        if(locationOfFire.getBlock().getType().equals(Material.AIR)){
            locationOfFire.setY(locationOfFire.getY() + 1);
            if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                locationOfFire.setX(locationOfFire.getX() - 1);
                if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                    locationOfFire.setY(locationOfFire.getY() - 1);
                    if(locationOfFire.getBlock().getType().equals(Material.AIR)){
                        locationOfFire.setX(locationOfFire.getX() - 1);
                        if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                            locationOfFire.setY(locationOfFire.getY() - 1);
                            if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                locationOfFire.setY(locationOfFire.getY() - 1);
                                if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                    locationOfFire.setX(locationOfFire.getX() + 1);
                                    locationOfFire.setY(locationOfFire.getY() - 1);
                                    if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                        List<Block> portalSpots = new ArrayList<>();
                                        for(int x = 0; x < 3; x++){
                                            locationOfFire.setY(locationOfFire.getY() + 1);
                                            if(!locationOfFire.getBlock().getType().equals(Material.AIR)){
                                                return;
                                            }
                                            portalSpots.add(locationOfFire.getBlock());
                                        }
                                        locationOfFire.setX(locationOfFire.getX() + 1);
                                        for(int x = 0; x < 3; x++){
                                            if(!locationOfFire.getBlock().getType().equals(Material.AIR)){
                                                return;
                                            }
                                            portalSpots.add(locationOfFire.getBlock());
                                            locationOfFire.setY(locationOfFire.getY() - 1);
                                        }

                                        for(Block b : portalSpots){
                                            b.setType(Material.PORTAL, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void eastWestCheck(Location locationOfFire, Material matOfPortal){

        for(int x = 0; x < 2; x++){
            locationOfFire.setY(locationOfFire.getY() + 1);
            if(!locationOfFire.getBlock().getType().equals(matOfPortal)){
                return;
            }
        }

        locationOfFire.setZ(locationOfFire.getZ() - 1);
        if(locationOfFire.getBlock().getType().equals(Material.AIR)){
            locationOfFire.setY(locationOfFire.getY() + 1);
            if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                locationOfFire.setZ(locationOfFire.getZ() - 1);
                if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                    locationOfFire.setY(locationOfFire.getY() - 1);
                    if(locationOfFire.getBlock().getType().equals(Material.AIR)){
                        locationOfFire.setZ(locationOfFire.getZ() - 1);
                        if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                            locationOfFire.setY(locationOfFire.getY() - 1);
                            if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                locationOfFire.setY(locationOfFire.getY() - 1);
                                if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                    locationOfFire.setZ(locationOfFire.getZ() + 1);
                                    locationOfFire.setY(locationOfFire.getY() - 1);
                                    if(locationOfFire.getBlock().getType().equals(matOfPortal)){
                                        List<Block> portalSpots = new ArrayList<>();
                                        for(int x = 0; x < 3; x++){
                                            locationOfFire.setY(locationOfFire.getY() + 1);
                                            if(!locationOfFire.getBlock().getType().equals(Material.AIR)){
                                                return;
                                            }
                                            portalSpots.add(locationOfFire.getBlock());
                                        }
                                        locationOfFire.setZ(locationOfFire.getZ() + 1);

                                        for(int x = 0; x < 3; x++){
                                            if(!locationOfFire.getBlock().getType().equals(Material.AIR)){
                                                return;
                                            }
                                            portalSpots.add(locationOfFire.getBlock());
                                            locationOfFire.setY(locationOfFire.getY() - 1);
                                        }

                                        for(Block b : portalSpots){
                                            b.setType(Material.PORTAL, false);
                                            b.setData((byte)2, false);
                                            b.getState().setRawData((byte)2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerEnterPortal(PlayerPortalEvent e){

        Player p = e.getPlayer();
        Block blockUnder = p.getLocation().subtract(0, 1, 0).getBlock();
        PortalTypes portalType = PortalTypes.getType(blockUnder.getType());

        if(portalType == null){
            chatFactory.sendPlayerMessage("Please stand in the middle of portal...", true, p, plugin.getPluginPrefix());
            e.setCancelled(true);
            return;
        }

        IslandDataConfigManager idcm = new IslandDataConfigManager(plugin);
        MessagesConfigManager mcm = new MessagesConfigManager(plugin);
        ConfigManager cm = new ConfigManager(plugin);

        if(idcm.isPlayerInFile(p.getUniqueId())){
            String msg;

            if(p.hasPermission(portalType.getAccessPermission())){
                if(idcm.is(p.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, portalType) || idcm.is(p.getUniqueId(), IslandDataConfigManager.DataKeys.PURCHASED, portalType)){
                    Location islandLocation = idcm.getIslandLocation(portalType, p.getUniqueId());
                    msg = mcm.getMessage(MessagesConfigManager.Messages.TELEPORTATION);
                    msg = msg.replace("%portalType%", portalType.getName());
                    p.teleport(islandLocation);
                    chatFactory.sendPlayerMessage(msg, true, p, plugin.getPluginPrefix());
                }else{
                    sendConfirmation(p, portalType, cm.getCost(portalType));
                }
            }else{
                msg = mcm.getMessage(MessagesConfigManager.Messages.NO_ACCESS_PERM);
                chatFactory.sendPlayerMessage(msg, true, p, plugin.getPluginPrefix());
            }
        }else{
            chatFactory.sendPlayerMessage("There has been an error. Please try relogging...", true, p, plugin.getPluginPrefix());
        }
        e.setCancelled(true);
    }

    private void sendConfirmation(Player p, PortalTypes portalType, int cost){

        MessagesConfigManager mcm = new MessagesConfigManager(plugin);
        String msg = mcm.getMessage(MessagesConfigManager.Messages.PORTAL_PURCHASE_CONFIRMATION);
        msg = msg.replace("%portalType%", portalType.getName());
        msg = msg.replace("%cost%", String.valueOf(cost));

        TextComponent regularMsg = new TextComponent(Utils.colorString(plugin.getPluginPrefix() + " &f" + msg));
        TextComponent yesMsg = new TextComponent(Utils.colorString(" Click Yes "));
        TextComponent noMsg = new TextComponent(Utils.colorString("or &cNo"));

        yesMsg.setColor(ChatColor.YELLOW);
        noMsg.setColor(ChatColor.RED);
        yesMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click yes to purchase").create()));
        noMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click no to cancel").create()));

        yesMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/iw purchase " + portalType.getName()));
        noMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/iw purchase null"));

        regularMsg.addExtra(yesMsg);
        regularMsg.addExtra(noMsg);

        p.spigot().sendMessage(regularMsg);

    }


}
