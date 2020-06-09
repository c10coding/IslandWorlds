package net.dohaw.play.islandworlds.utils;

import org.bukkit.ChatColor;

public class Utils {

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

    public static String colorString(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
