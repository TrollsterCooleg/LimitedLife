package me.cooleg.limitedlife.utils;

import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class OfflinePenaltyHandling {

    private final UUID TIMETRACKER = UUID.nameUUIDFromBytes("Cooleg".getBytes(StandardCharsets.UTF_8));
    private final NamespacedKey key;
    private final SQLUtils sql;
    private final JavaPlugin plugin;
    private long globalRecordedTime;
    private boolean enabled = false;

    public OfflinePenaltyHandling(JavaPlugin plugin, SQLUtils utils) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "LimitedLifeRecordedTime");
        Long recordedTimeObject = utils.getTimeLeftNow(TIMETRACKER);
        globalRecordedTime = recordedTimeObject == null ? 0 : recordedTimeObject;
        this.sql = utils;
    }

    public void penalizePlayer(Player player) {
        long time = getRecordedTime(player);
        if (time > globalRecordedTime) {
            setRecordedTime(player, globalRecordedTime);
        } else if (time < globalRecordedTime) {
            long diff = globalRecordedTime - time;
            LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    limitedLifePlayer.setSeconds(limitedLifePlayer.getSeconds() - (long) (diff * ConfigWrapper.offlinePenaltyMultiplier));
                }
            }.runTaskLater(plugin, 2);

            setRecordedTime(player, globalRecordedTime);
        }
    }

    public long getRecordedTime(Player player) {
        Long time = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        if (time == null) {return 0;}

        return time;
    }

    public void setRecordedTime(Player player, long time) {
        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, time);
    }

    public void addRecordedTime() {
        if (!enabled) {return;}

        globalRecordedTime++;

        for (Player player : Bukkit.getOnlinePlayers()) {
            long time = getRecordedTime(player);
            setRecordedTime(player, time + 1);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void save() {
        sql.setTimeLeft(TIMETRACKER, globalRecordedTime);
    }

    public void saveNow() {
        sql.setTimeLeftNow(TIMETRACKER, globalRecordedTime);
    }

}
