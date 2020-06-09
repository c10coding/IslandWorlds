package net.dohaw.play.islandworlds.events;

import me.c10coding.coreapi.chat.Chat;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.files.MessagesConfigManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import net.dohaw.play.islandworlds.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeneralListener implements Listener {

    private IslandWorlds plugin;
    private ConfigManager cm;

    public GeneralListener(IslandWorlds plugin){
        this.plugin = plugin;
        this.cm = new ConfigManager(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        IslandDataConfigManager dataManager = new IslandDataConfigManager(plugin);
        if(!dataManager.isPlayerInFile(e.getPlayer().getUniqueId())){
            dataManager.addPlayerToFile(e.getPlayer().getUniqueId());
        }

    }

    @EventHandler
    public void onBlockOnFire(BlockIgniteEvent e){
        if(e.getCause().equals(BlockIgniteEvent.IgniteCause.LAVA)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        IslandDataConfigManager idcm = new IslandDataConfigManager(plugin);
        Player p = e.getPlayer();
        World playerWorld = p.getWorld();

        if(PortalTypes.getType(cm, playerWorld) != null){
            PortalTypes portalType = PortalTypes.getType(cm, playerWorld);
            Location spawnLocation = idcm.getIslandLocation(portalType, p.getUniqueId());
            Location cobbleGenBlock = portalType.getCobbleGenBlock(spawnLocation);
            Location blockBrokenLocation = e.getBlock().getLocation();

            if(cobbleGenBlock != null){
                if(blockBrokenLocation.equals(cobbleGenBlock)){
                    if(idcm.getOwnerFromLocation(blockBrokenLocation, portalType) != null){
                        UUID ownerUUID = idcm.getOwnerFromLocation(blockBrokenLocation, portalType);
                        if(!idcm.getMembersWithAccess(ownerUUID).contains(p.getUniqueId().toString())){
                            e.setCancelled(true);
                        }
                    }else{
                        e.setCancelled(true);
                    }
                }else{
                    e.setCancelled(true);
                }
            }else{
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        World playerWorld = e.getPlayer().getWorld();
        if(PortalTypes.getType(cm, playerWorld) != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCobbleGenGeneration(BlockFormEvent e){
        Block b = e.getBlock();
        World world = b.getWorld();
        if(PortalTypes.getType(cm, world) != null){
            PortalTypes portalType = PortalTypes.getType(cm, world);
            if(!portalType.getGeneratedMaterials().isEmpty()){

                e.setCancelled(true);
                List<Material> potentialMaterials = portalType.getGeneratedMaterials();
                int randNumMaterial = Utils.getRandomNumberInRange(0, potentialMaterials.size() - 1);
                Material randomMaterial = potentialMaterials.get(randNumMaterial);

                short maxShort;
                byte randByte = 0;

                b.setType(randomMaterial);
                if(randomMaterial.equals(Material.STAINED_CLAY)){
                    if(portalType.equals(PortalTypes.DESERT)){
                        b.setData((byte) 4);
                    }else if(portalType.equals(PortalTypes.MYCEL)){
                        b.setData((byte) 14);
                    }
                }

                /*
                    For blocks that have multiple byte variations
                 */
                if(randomMaterial.equals(Material.PRISMARINE) || randomMaterial.equals(Material.STONE) || randomMaterial.equals(Material.WOOD)){
                    if(randomMaterial.equals(Material.PRISMARINE)){
                        maxShort = 2;
                        randByte = (byte) Utils.getRandomNumberInRange(0, maxShort);
                    }else if(randomMaterial.equals(Material.STONE)){
                        randByte = 3;
                    }else if(randomMaterial.equals(Material.WOOD)){
                        randByte = 2;
                        final int CHANCE = 2;
                        int randomNum = Utils.getRandomNumberInRange(1, 10);
                        if(CHANCE < randomNum){
                            randomMaterial = Material.STAINED_CLAY;
                            b.setType(randomMaterial);
                            if(portalType.equals(PortalTypes.DESERT)){
                                randByte = 4;
                            }else if(portalType.equals(PortalTypes.MYCEL)){
                                randByte = 14;
                            }
                        }
                    }
                    b.setData(randByte);
                }
            }
        }
    }

    @EventHandler
    public void rightClickNPC(PlayerInteractEntityEvent e){
        Player player = e.getPlayer();
        Entity rightClickedEntity = e.getRightClicked();

        if(rightClickedEntity.hasMetadata("NPC")){
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            NPC boss = registry.getNPC(rightClickedEntity);
            World bossWorld = player.getWorld();
            PortalTypes portalType = PortalTypes.getType(new ConfigManager(plugin), bossWorld);
            String name = boss.getName();
            sendBossConfirmation(player, name, portalType);
        }
    }

    private void sendBossConfirmation(Player p, String name, PortalTypes portalType){

        Chat chatFactory = plugin.getApi().getChatFactory();
        name = chatFactory.chat(name + "&f");
        MessagesConfigManager mcm = new MessagesConfigManager(plugin);
        String msg = mcm.getMessage(MessagesConfigManager.Messages.BOSS_SPAWN_CONFIRMATION);
        msg = msg.replace("%bossName%", name);
        msg = msg.replace("%bossCooldown%", String.valueOf(cm.getBossCooldown()));

        int bossCooldownInHours = cm.getBossCooldown();
        if(bossCooldownInHours > 1){
            msg = msg + "s - ";
        }else{
            msg = msg + " - ";
        }

        TextComponent regularMsg = new TextComponent(msg);
        TextComponent clickablePart1 = new TextComponent("Yes /");
        clickablePart1.setColor(ChatColor.GOLD);
        clickablePart1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/boss spawn " + portalType.getBossCommandName()));
        clickablePart1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Spawn boss").create()));

        TextComponent clickablePart2 = new TextComponent(" No");
        clickablePart2.setColor(ChatColor.RED);
        clickablePart2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/iw boss cancel"));
        clickablePart2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cancel").create()));

        regularMsg.addExtra(clickablePart1);
        regularMsg.addExtra(clickablePart2);
        p.spigot().sendMessage(regularMsg);
    }

}
