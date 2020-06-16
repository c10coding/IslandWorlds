package net.dohaw.play.islandworlds.commands;

import me.c10coding.coreapi.chat.Chat;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.files.MessagesConfigManager;
import net.dohaw.play.islandworlds.managers.IslandManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private IslandWorlds plugin;
    private Chat chatFactory;
    private String prefix;
    private MessagesConfigManager mcm;
    private IslandDataConfigManager idcm;

    public Commands(IslandWorlds plugin){
        this.plugin = plugin;
        this.chatFactory = plugin.getApi().getChatFactory();
        this.prefix = plugin.getPluginPrefix();
        this.mcm = new MessagesConfigManager(plugin);
        this.idcm = new IslandDataConfigManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        idcm = new IslandDataConfigManager(plugin);

        /*
            The removing of the player's island by an admin
         */
        if(args.length > 0){

            if(sender.hasPermission("islandworlds.delete")){
                if(args[0].equalsIgnoreCase("delete") && args.length == 3){
                    String portalTypeString = args[1];
                    String playerName = args[2];
                    PortalTypes portalType = PortalTypes.getType(portalTypeString);
                    OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);

                    if(op != null){
                        if(portalType != null){
                            if(idcm.is(op.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, portalType)){
                                if(idcm.getIslandLocation(portalType, op.getUniqueId()) != null){
                                    IslandManager islandManager = new IslandManager(plugin, portalType, op);
                                    islandManager.removeIslandAdmin();
                                    chatFactory.sendPlayerMessage("You have deleted &e" + op.getName() + "'s &b" + portalType.getName() + " &fisland!", true, sender, prefix);
                                }else{
                                    chatFactory.sendPlayerMessage("This island isn't generated!", true, sender, prefix);
                                }
                            }
                        }else{
                            chatFactory.sendPlayerMessage("This isn't a valid island type. The types are &eocean, mycel, and desert", true, sender, prefix);
                        }
                    }else{
                        chatFactory.sendPlayerMessage("This is not a valid player!", true, sender, prefix);
                    }
                    return true;
                }
            }else{
                chatFactory.sendPlayerMessage("You don't have permission to use that command!", true, sender, prefix);
            }

            /*
                Non-Op commands
             */
            if(sender instanceof Player){
                Player player = (Player) sender;
                if(args[0].equalsIgnoreCase("purchase") && args.length == 2){
                    String portalType = args[1];
                    if(args[1].equalsIgnoreCase("null")){
                        return false;
                    }else{
                        ConfigManager cm = new ConfigManager(plugin);
                        PortalTypes type = PortalTypes.getType(portalType);

                        int costOfIsland = cm.getCost(type);
                        int playerBalance = (int) IslandWorlds.getEconomy().getBalance(player);
                        String msg;

                        if(!idcm.is(player.getUniqueId(), IslandDataConfigManager.DataKeys.PURCHASED, type)){
                            if(!idcm.is(player.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, type)){
                                if(playerBalance >= costOfIsland){

                                    IslandManager islandManager = new IslandManager(plugin, type, player, idcm);
                                    idcm.set(player.getUniqueId(), IslandDataConfigManager.DataKeys.PURCHASED, type, true);
                                    idcm.set(player.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, type, true);
                                    msg = mcm.getMessage(MessagesConfigManager.Messages.PORTAL_PURCHASE);
                                    msg = msg.replace("%portalType%", portalType);
                                    IslandWorlds.getEconomy().withdrawPlayer(player, costOfIsland);
                                    islandManager.generateIsland();

                                }else{
                                    msg = mcm.getMessage(MessagesConfigManager.Messages.PORTAL_PURCHASE_REJECT);
                                }
                            }else{
                                msg = mcm.getMessage(MessagesConfigManager.Messages.PORTAL_ALREADY_UNLOCKED);
                            }
                        }else{
                            msg = mcm.getMessage(MessagesConfigManager.Messages.PORTAL_ALREADY_PURCHASED);
                        }
                        chatFactory.sendPlayerMessage(msg, true, player, prefix);

                    }
                }else if(args[0].equalsIgnoreCase("addmember") && args.length == 2){

                    if(!idcm.isPlayerInFile(player.getUniqueId())){
                        idcm.addPlayerToFile(player.getUniqueId());
                    }

                    if(!idcm.hasAtleastOneIsland(player.getUniqueId())){
                        chatFactory.sendPlayerMessage(mcm.getMessage(MessagesConfigManager.Messages.NO_ISLANDS), true, player, prefix);
                    }

                    String playerName = args[1];
                    if(Bukkit.getPlayer(playerName) != null){
                        Player addedPlayer = Bukkit.getPlayer(playerName);

                        String msg;
                        if(idcm.getMembersWithAccess(player.getUniqueId()).contains(addedPlayer.getUniqueId().toString())){
                            msg = mcm.getMessage(MessagesConfigManager.Messages.IS_ALREADY_A_MEMBER);
                        }else{
                            msg = mcm.getMessage(MessagesConfigManager.Messages.ADDED_MEMBER);
                            msg = msg.replace("%playerName%", addedPlayer.getName());
                            idcm.addMember(player.getUniqueId(), addedPlayer.getUniqueId());
                        }
                        chatFactory.sendPlayerMessage(msg, true, player, prefix);

                    }else{
                        chatFactory.sendPlayerMessage("The player is either not online or not an actual player", true, player, prefix);
                    }

                }else if(args[0].equalsIgnoreCase("removemember") && args.length == 2){

                    if(!idcm.isPlayerInFile(player.getUniqueId())){
                        idcm.addPlayerToFile(player.getUniqueId());
                    }

                    if(!idcm.hasAtleastOneIsland(player.getUniqueId())){
                        chatFactory.sendPlayerMessage(mcm.getMessage(MessagesConfigManager.Messages.NO_ISLANDS), true, player, prefix);
                    }

                    String playerName = args[1];
                    if(Bukkit.getPlayer(playerName) != null){
                        Player removedPlayer = Bukkit.getPlayer(playerName);

                        String msg;
                        if(!idcm.getMembersWithAccess(player.getUniqueId()).contains(removedPlayer.getUniqueId().toString())){
                            msg = mcm.getMessage(MessagesConfigManager.Messages.NOT_A_MEMBER);
                        }else{
                            msg = mcm.getMessage(MessagesConfigManager.Messages.REMOVED_MEMBER);
                            msg = msg.replace("%playerName%", removedPlayer.getName());
                            idcm.removeMember(player.getUniqueId(), removedPlayer.getUniqueId());
                        }
                        chatFactory.sendPlayerMessage(msg, true, player, prefix);

                    }else{
                        chatFactory.sendPlayerMessage("The player is either not online or not an actual player", true, player, prefix);
                    }
                }else if(args[0].equalsIgnoreCase("boss")){

                    if(args[1].equalsIgnoreCase("cancel")){
                        chatFactory.sendPlayerMessage("Aborting...", true, player, prefix);
                    }else if(args[1].equalsIgnoreCase("spawn")){
                        String portalType = args[2];
                        PortalTypes type = PortalTypes.getType(portalType);
                        if(!idcm.isBossOnCooldown(player.getUniqueId(), type)){
                            idcm.setBossCooldown(player.getUniqueId(), type);
                            despawnNearestNPC(player, PortalTypes.getType(portalType));
                            disableFlyForPlayers(player.getLocation());
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "boss spawn " + type.getBossCommandName() + " " + player.getWorld().getName() + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ());
                        }else{
                            chatFactory.sendPlayerMessage("This boss is on cooldown! &cTime left: " + idcm.getCooldownTime(player.getUniqueId(), type) + " minutes", true, player, prefix);
                        }
                    }

                }/*else if(args[0].equalsIgnoreCase("delete") && args.length == 2){
                    String portalType = args[1];
                    if(portalType.equalsIgnoreCase("ocean") || portalType.equalsIgnoreCase("desert") || portalType.equalsIgnoreCase("mycel")){
                        PortalTypes type = PortalTypes.getType(portalType);
                        IslandManager islandmanager = new IslandManager(plugin, type, player);
                        if(idcm.is(player.getUniqueId(), IslandDataConfigManager.DataKeys.UNLOCKED, type)){
                            if(idcm.getIslandLocation(type, player.getUniqueId()) != null){
                                islandmanager.removeIsland();
                                chatFactory.sendPlayerMessage("Removing island...", true, player, prefix);
                            }else{
                                chatFactory.sendPlayerMessage("No island of this type was found!", true, player, prefix);
                            }
                        }else{
                            chatFactory.sendPlayerMessage("You do not have this island unlocked!", true, player, prefix);
                        }
                    }else{
                        chatFactory.sendPlayerMessage("You did not give a valid island type. The island types are &edesert, ocean, and mycel!", true, player, prefix);
                    }
                }*/
            }else{
                chatFactory.sendPlayerMessage("Only players can run these commands!", true, sender, prefix);
            }
        }
        return false;
    }

    private void disableFlyForPlayers(Location playerLocation) {
        for(Entity e : playerLocation.getWorld().getNearbyEntities(playerLocation, 30, 30, 30)){
            if(e instanceof Player){
                Player p = (Player) e;
                if(p.isFlying() || p.getAllowFlight()){
                    p.setFlying(false);
                    p.setAllowFlight(false);
                }
            }
        }
    }

    private void despawnNearestNPC(Player player, PortalTypes portalType){
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC bossSpawner = registry.getById(idcm.getNPCID(player.getUniqueId(), portalType));
        bossSpawner.despawn(DespawnReason.PENDING_RESPAWN);
    }

}
