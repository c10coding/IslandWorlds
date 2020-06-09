package net.dohaw.play.islandworlds.files;

import me.c10coding.coreapi.files.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesConfigManager extends ConfigManager {

    public MessagesConfigManager(JavaPlugin plugin) {
        super(plugin, "messages.yml");
    }

    public enum Messages{

        PORTAL_PURCHASE_CONFIRMATION("Portal Purchase Confirmation"),
        PORTAL_PURCHASE("Purchase Valid"),
        PORTAL_PURCHASE_REJECT("Purchase Invalid"),
        PORTAL_ALREADY_PURCHASED("Already Purchased"),
        PORTAL_ALREADY_UNLOCKED("Already Unlocked"),
        NO_ACCESS_PERM("No Access Permission"),
        TELEPORTATION("Teleportation"),
        IS_ALREADY_A_MEMBER("Is Already a Member"),
        BOSS_SPAWN_CONFIRMATION("Boss Confirmation"),
        NOT_A_MEMBER("Not a Member"),
        NO_ISLANDS("No Islands"),
        ADDED_MEMBER("Added Member"),
        REMOVED_MEMBER("Removed Member"),
        BOSS_DEFEAT("Boss Defeat");

        private String configKey;
        Messages(String configKey){
            this.configKey = configKey;
        }

        public String getConfigKey(){
            return configKey;
        }

    }

    public String getMessage(Messages msg){
        return config.getString(msg.configKey);
    }

}
