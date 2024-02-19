package me.cooleg.limitedlife.data;

import me.cooleg.limitedlife.LimitedLife;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigWrapper {

    public static long initialTime;
    public static long timeGained;
    public static long timeLost;
    public static double offlinePenaltyMultiplier;
    public static int maxEnchantmentLevel;
    public static boolean netheriteDisabled;
    public static boolean potionsDisabled;

    public static void loadConfig(LimitedLife plugin) {
        FileConfiguration config = plugin.getConfig();

        initialTime = config.getLong("initialtime");
        timeGained = config.getLong("timegainedforkill");
        timeLost = config.getLong("timelostondeath");
        offlinePenaltyMultiplier = config.getDouble("offlinepenalty");
        maxEnchantmentLevel = config.getInt("maxenchantlevel");
        netheriteDisabled = config.getBoolean("netheritedisabled");
        potionsDisabled = config.getBoolean("potionsdisabled");
    }

}
