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
        startTimers();
        if(isFirstTime()){

            getConfig().set("IsFirstTime", false);
            saveConfig();

            getLogger().info("This is your first time loading the plugin. Please fill out the World fields in the config.yml file. ");
            getLogger().info("Also, make sure you put the schematics into the schematics folder (IslandsWorlds/schematics)");
            return;
        }

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
        getLogger().info("Bye bye!");
    }

    private void registerEvents(){
        getLogger().info("Registering events...");
        Bukkit.getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
    }

    private void registerCommands(){
        getLogger().info("Registering commands...");
        this.getCommand("islandworlds").setExecutor(new Commands(this));
    }

    private void startTimers(){
        getLogger().info("Starting timers...");
        new BossCooldownTimer(this).runTaskTimer(this, 0L, 1200L);
    }

    private boolean setupEconomy() {
        getLogger().info("Setting up Vault economy...");
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
        getLogger().info("Loading configuration files and folders...");
        File[] files = {new File(this.getDataFolder(), "config.yml"), new File(this.getDataFolder(), "islandData.yml"), new File(this.getDataFolder(), "messages.yml")};
        for(File f : files){
            if(!f.exists()){
                this.saveResource(f.getName(), false);
            }
        }

        File rootFolder = new File(this.getDataFolder(), "schematics");
        if(!rootFolder.exists()){
            rootFolder.mkdirs();
        }

    }

    private boolean isFirstTime(){
        return this.getConfig().getBoolean("IsFirstTime");
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
