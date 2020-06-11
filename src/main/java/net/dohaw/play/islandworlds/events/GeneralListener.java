package net.dohaw.play.islandworlds.events;

import me.c10coding.coreapi.chat.Chat;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GeneralListener implements Listener {

    private IslandWorlds plugin;
    private ConfigManager cm;
    private IslandDataConfigManager idcm;

    public GeneralListener(IslandWorlds plugin){
        this.plugin = plugin;
        this.cm = new ConfigManager(plugin);
        this.idcm = new IslandDataConfigManager(plugin);
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
        Location blockBrokenLocation = e.getBlock().getLocation();

        if(PortalTypes.getType(cm, playerWorld) != null){
            PortalTypes portalType = PortalTypes.getType(cm, playerWorld);
            if(idcm.getOwnerFromLocation(blockBrokenLocation, portalType) != null){
                UUID ownerUUID = idcm.getOwnerFromLocation(blockBrokenLocation, portalType);
                if(!idcm.getMembersWithAccess(ownerUUID).contains(p.getUniqueId().toString())){
                    e.setCancelled(true);
                }else{
                    Location spawnLocation = idcm.getIslandLocation(portalType, ownerUUID);
                    Location cobbleGenBlock = portalType.getCobbleGenBlock(spawnLocation);

                    if(!cobbleGenBlock.equals(blockBrokenLocation)){
                        e.setCancelled(true);
                    }
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

            if(isGeneratingBlockOnIsland(b.getLocation(), portalType)){
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
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent e){
        if(isFightingBoss(e.getPlayer())){
            String[] args = e.getMessage().split(" ");
            if(!args[0].equalsIgnoreCase("heal") && !args[0].equalsIgnoreCase("feed")){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void rightClickNPC(PlayerInteractEntityEvent e){

        Player player = e.getPlayer();
        Entity rightClickedEntity = e.getRightClicked();
        idcm = new IslandDataConfigManager(plugin);

        //Prevents the method from running twice
        if(e.getHand().equals(EquipmentSlot.OFF_HAND)){
            return;
        }

        if(isAllowedToInteract(player)){
            if(rightClickedEntity.hasMetadata("NPC")){
                NPCRegistry registry = CitizensAPI.getNPCRegistry();
                NPC boss = registry.getNPC(rightClickedEntity);
                World bossWorld = player.getWorld();
                PortalTypes portalType = PortalTypes.getType(new ConfigManager(plugin), bossWorld);
                String name = boss.getName();
                sendBossConfirmation(player, name, portalType);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
        for(PortalTypes type : PortalTypes.values()){
            if(idcm.is(player.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, type)){
                World w = player.getWorld();
                PortalTypes portalType = PortalTypes.getType(cm, w);
                Location islandLocation = idcm.getIslandLocation(type, player.getUniqueId());

                //If it's been tracked that the play has killed the boss
                if(!idcm.isBossKilled(player.getUniqueId(), portalType)){
                    respawnNPC(islandLocation);
                }

                //If the boss is physically alive
                if(isBossAlive(islandLocation)){
                    killNearbyBoss(islandLocation);
                }
            }
        }
    }

    /*
        Kills the boss if the player dies and respawns the
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        PortalTypes portalType = PortalTypes.getType(cm, player.getWorld());
        if(portalType != null){
            Location islandLocation = idcm.getIslandLocation(portalType, player.getUniqueId());
            if(islandLocation != null){
                if(islandLocation.getWorld().equals(player.getLocation().getWorld())){
                    //They died within their own island
                    if(player.getLocation().distance(islandLocation) < 50){
                        if(isBossAlive(islandLocation)){
                            killNearbyBoss(islandLocation);
                            respawnNPC(islandLocation);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e){
        if(isABoss(e.getEntity())){
            if(e.getEntity().getKiller() != null){
                MessagesConfigManager mcm = new MessagesConfigManager(plugin);
                Chat chatFactory = plugin.getApi().getChatFactory();
                Player killer = e.getEntity().getKiller();
                PortalTypes portalType = PortalTypes.getType(cm, killer.getWorld());
                idcm.setBossKilled(killer.getUniqueId(), portalType);
                chatFactory.sendPlayerMessage(mcm.getMessage(MessagesConfigManager.Messages.BOSS_DEFEAT), true, killer, plugin.getPluginPrefix());
            }
        }
    }

    private boolean isFightingBoss(Player p){
        World world = p.getWorld();
        PortalTypes portalType = PortalTypes.getType(cm, world);
        if(portalType != null){
            Location playerIslandLocation = idcm.getIslandLocation(portalType, p.getUniqueId());
            if(playerIslandLocation.distance(p.getLocation()) < 50){
                return isBossAlive(playerIslandLocation);
            }
        }
        return false;
    }

    private boolean isAllowedToInteract(Player playerInteracting){
        PortalTypes portalType = PortalTypes.getType(cm, playerInteracting.getWorld());
        if(portalType != null){
            if(idcm.isPlayerInFile(playerInteracting.getUniqueId())){
                if(idcm.getOwnerFromLocation(playerInteracting.getLocation(), portalType) != null){
                    UUID ownerUUID = idcm.getOwnerFromLocation(playerInteracting.getLocation(), portalType);
                    return ownerUUID.equals(playerInteracting.getUniqueId());
                }
            }
        }
        return false;
    }

    private boolean isABoss(Entity entity){
        if(entity instanceof LivingEntity) {
            if (!(entity instanceof Player)) {
                return !entity.hasMetadata("NPC");
            }
        }
        return false;
    }

    private boolean isGeneratingBlockOnIsland(Location blockLocation, PortalTypes portalType){
        idcm = new IslandDataConfigManager(plugin);
        for(Location islandLocation : idcm.getWorldIslandLocations(portalType)){
            if(blockLocation.distance(islandLocation) < 20){
                return true;
            }
        }
        return false;
    }

    /*
        Checks if boss is alive on the player's island
     */
    private boolean isBossAlive(Location islandLocation){
        Collection<Entity> nearbyEntities = islandLocation.getWorld().getNearbyEntities(islandLocation, 30, 30, 30);
        for(Entity entity : nearbyEntities){
            if(isABoss(entity)){
                return true;
            }
        }
        return false;
    }

    private void killNearbyBoss(Location islandLocation){
        Collection<Entity> nearbyEntities = islandLocation.getWorld().getNearbyEntities(islandLocation, 30, 30, 30);
        for(Entity entity : nearbyEntities){
            if(isABoss(entity)){
                entity.remove();
            }
        }
    }

    private void respawnNPC(Location locationNearIsland){
        World world = locationNearIsland.getWorld();
        PortalTypes portalType = PortalTypes.getType(new ConfigManager(plugin), world);
        UUID ownerOfIsland = idcm.getOwnerFromLocation(locationNearIsland, portalType);
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC bossSpawner = registry.getById(idcm.getNPCID(ownerOfIsland, portalType));
        if(idcm.getNPCLocation(ownerOfIsland, portalType) != null){
            Location npcLocation = idcm.getNPCLocation(ownerOfIsland, portalType);
            bossSpawner.spawn(npcLocation);
        }else{
            plugin.getLogger().warning("There was an error trying to respawn the NPC with the ID of " + bossSpawner.getId());
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
        clickablePart1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/iw boss spawn " + portalType));
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
