package me.cooleg.limitedlife.data;

import me.cooleg.limitedlife.LimitedLife;

public class ConfigWrapper {

    public static long initialTime;
    public static long timeGained;
    public static long timeLost;

    public static void loadConfig(LimitedLife plugin) {
        initialTime = plugin.getConfig().getLong("initialtime");
        timeGained = plugin.getConfig().getLong("timegainedforkill");
        timeLost = plugin.getConfig().getLong("timelostondeath");
    }

}
