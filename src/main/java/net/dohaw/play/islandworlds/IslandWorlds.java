package net.dohaw.play.islandworlds;

import me.c10coding.coreapi.CoreAPI;
import net.dohaw.play.islandworlds.commands.Commands;
import net.dohaw.play.islandworlds.events.GeneralListener;
import net.dohaw.play.islandworlds.events.PortalListener;
import net.dohaw.play.islandworlds.files.ConfigManager;
import net.dohaw.play.islandworlds.managers.SchematicLoader;
import net.dohaw.play.islandworlds.portals.PortalTypes;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class IslandWorlds extends JavaPlugin {

    private CoreAPI api = new CoreAPI();
    private static Economy econ;
    private SchematicLoader schemLoader = new SchematicLoader(this);

    @Override
    public void onEnable() {

        validateConfigs();
        registerEvents();
        registerCommands();

        if (!setupEconomy() ) {
            this.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConfigManager cm = new ConfigManager(this);
        for(PortalTypes portalType : PortalTypes.values()){
            String worldName = cm.getWorld(portalType);
            if(Bukkit.getWorld(worldName) == null){
                this.getLogger().warning("The world field for the portal type " + portalType.getName() + " is not valid! The world could not be found...");
            }
        }

        schemLoader.load();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerEvents(){
        Bukkit.getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
    }

    private void registerCommands(){
        this.getCommand("islandworlds").setExecutor(new Commands(this));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void validateConfigs(){
        File[] files = {new File(this.getDataFolder(), "config.yml"), new File(this.getDataFolder(), "islandData.yml"), new File(this.getDataFolder(), "messages.yml")};
        for(File f : files){
            if(!f.exists()){
                this.saveResource(f.getName(), false);
            }
        }
    }

    public SchematicLoader getSchemLoader(){
        return schemLoader;
    }

    public String getPluginPrefix(){
        return this.getConfig().getString("PluginPrefix");
    }

    public CoreAPI getApi(){
        return api;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
