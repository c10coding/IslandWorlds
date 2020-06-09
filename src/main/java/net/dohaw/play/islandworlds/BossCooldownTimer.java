package net.dohaw.play.islandworlds;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.files.IslandDataConfigManager;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class BossCooldownTimer extends BukkitRunnable {

    private IslandWorlds plugin;
    private IslandDataConfigManager idcm;
    private NPCRegistry registry;
    private ConfigManager cm;

    public BossCooldownTimer(IslandWorlds plugin){
        this.plugin = plugin;
        this.idcm = new IslandDataConfigManager(plugin);
        this.registry = CitizensAPI.getNPCRegistry();
        this.cm = new ConfigManager(plugin);
    }

    @Override
    public void run() {

        idcm = new IslandDataConfigManager(plugin);
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if(idcm.isPlayerInFile(onlinePlayer.getUniqueId())){
                for(PortalTypes portalType : PortalTypes.values()){
                    if(idcm.isBossOnCooldown(onlinePlayer.getUniqueId(), portalType)){
                        if(idcm.getCooldownTime(onlinePlayer.getUniqueId(), portalType) != 0){
                            idcm.decreaseBossCooldown(onlinePlayer.getUniqueId(), portalType);
                        }else{
                            idcm.removeCooldownTime(onlinePlayer.getUniqueId(), portalType);
                        }
                    }
                }
            }
        }
    }

}
