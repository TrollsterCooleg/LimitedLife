package me.cooleg.limitedlife.utils;

import me.cooleg.limitedlife.data.LimitedLifePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerRunnable extends BukkitRunnable {

    private final OfflinePenaltyHandling penalty;
    private int time = 0;
    public static boolean enabled = true;

    public TimerRunnable(OfflinePenaltyHandling penalty) {
        this.penalty = penalty;
    }

    @Override
    public void run() {
        if (!enabled) {return;}

        time++;

        LimitedLifePlayer.updateTimes();
        penalty.addRecordedTime();
        if (time >= 300) {
            LimitedLifePlayer.saveAll();
            penalty.save();
            time = 0;
        }
    }

}
