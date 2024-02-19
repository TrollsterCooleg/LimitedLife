package me.cooleg.limitedlife.data;

import me.cooleg.limitedlife.LimitedLife;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigWrapper {

    public static long initialTime;
    public static long yellowTime;
    public static long redTime;
    public static long timeGained;
    public static long timeLost;
    public static double offlinePenaltyMultiplier;
    public static int maxEnchantmentLevel;
    public static boolean netheriteDisabled;
    public static boolean potionsDisabled;
    public static boolean helmetsDisabled;

    public static void loadConfig(LimitedLife plugin) {
        FileConfiguration config = plugin.getConfig();

        initialTime = config.getLong("initialtime", 86400);
        yellowTime = initialTime * 2/3;
        redTime = yellowTime/2;

        timeGained = config.getLong("timegainedforkill", 1800);
        timeLost = config.getLong("timelostondeath", 3600);
        offlinePenaltyMultiplier = config.getDouble("offlinepenalty", 1.25);
        maxEnchantmentLevel = config.getInt("maxenchantlevel", 2);
        netheriteDisabled = config.getBoolean("netheritedisabled", true);
        potionsDisabled = config.getBoolean("potionsdisabled", true);
        helmetsDisabled = config.getBoolean("helmetsdisabled", true);
    }

}
