package net.dohaw.play.islandworlds.commands;

import me.c10coding.coreapi.chat.Chat;
import net.dohaw.play.islandworlds.IslandWorlds;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.files.MessagesConfigManager;
import net.dohaw.play.islandworlds.managers.IslandManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private IslandWorlds plugin;
    private Chat chatFactory;
    private String prefix;
    private MessagesConfigManager mcm;

    public Commands(IslandWorlds plugin){
        this.plugin = plugin;
        this.chatFactory = plugin.getApi().getChatFactory();
        this.prefix = plugin.getPluginPrefix();
        this.mcm = new MessagesConfigManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        IslandDataConfigManager idcm = new IslandDataConfigManager(plugin);
        if(args.length > 0){
            if(sender instanceof Player){
                Player player = (Player) sender;
                if(args[0].equalsIgnoreCase("purchase") && args.length == 2){
                    if(!sender.isOp()){
                        return false;
                    }
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

                    String playerName = args[1];
                    if(Bukkit.getPlayer(playerName) != null){
                        Player addedPlayer = Bukkit.getPlayer(playerName);

                        if(idcm.getMembersWithAccess(player.getUniqueId()).contains(addedPlayer.getUniqueId().toString())){
                            chatFactory.sendPlayerMessage(mcm.getMessage(MessagesConfigManager.Messages.IS_ALREADY_A_MEMBER), true, player, prefix);
                        }else{
                            idcm.addMember(player.getUniqueId(), addedPlayer.getUniqueId());
                        }

                    }else{
                        chatFactory.sendPlayerMessage("The player is either not online or not an actual player", true, player, prefix);
                    }

                }else if(args[0].equalsIgnoreCase("removemember") && args.length == 2){

                    if(!idcm.isPlayerInFile(player.getUniqueId())){
                        idcm.addPlayerToFile(player.getUniqueId());
                    }

                    String playerName = args[1];
                    if(Bukkit.getPlayer(playerName) != null){
                        Player removedPlayer = Bukkit.getPlayer(playerName);

                        if(!idcm.getMembersWithAccess(player.getUniqueId()).contains(removedPlayer.getUniqueId().toString())){
                            chatFactory.sendPlayerMessage(mcm.getMessage(MessagesConfigManager.Messages.NOT_A_MEMBER), true, player, prefix);
                        }else{
                            idcm.removeMember(player.getUniqueId(), removedPlayer.getUniqueId());
                        }

                    }else{
                        chatFactory.sendPlayerMessage("The player is either not online or not an actual player", true, player, prefix);
                    }
                }else if(args[0].equalsIgnoreCase("boss") && args[1].equalsIgnoreCase("cancel") && args.length == 2){
                    chatFactory.sendPlayerMessage("Aborting...", true, player, prefix);
                }
            }else{
                chatFactory.sendPlayerMessage("Only players can run these commands!", true, sender, prefix);
            }
        }
        return false;
    }
}
