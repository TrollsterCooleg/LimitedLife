package me.cooleg.limitedlife.utils;

import me.cooleg.limitedlife.data.LimitedLifePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerRunnable extends BukkitRunnable {

    private int time = 0;
    public static boolean enabled = true;

    @Override
    public void run() {
        if (!enabled) {return;}

        time++;

        LimitedLifePlayer.updateTimes();
        if (time >= 300) {
            LimitedLifePlayer.saveAll();
            time = 0;
        }
    }

}
