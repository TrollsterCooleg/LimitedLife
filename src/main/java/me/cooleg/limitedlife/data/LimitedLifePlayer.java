package me.cooleg.limitedlife.data;

import me.cooleg.limitedlife.LimitedLife;
import me.cooleg.limitedlife.utils.TextFormatting;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LimitedLifePlayer {

    private static final HashMap<UUID, LimitedLifePlayer> playerMap = new HashMap<>();
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    private final UUID id;
    private long seconds;
    private boolean timeFound;
    private boolean boogeyman;

    private LimitedLifePlayer(UUID id) {
        this.id = id;
        LimitedLife.getSQL().getTimeLeft(id, (time) -> {
            if (time == null) {
                this.seconds = ConfigWrapper.initialTime;
            } else {
                this.seconds = time;
            }

            timeFound = true;
        });
    }

    public UUID getId() {
        return id;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public boolean isBoogeyman() {
        return boogeyman;
    }

    public void setBoogeyman(boolean boogeyman) {
        this.boogeyman = boogeyman;
    }

    public static void updateTimes() {
        for (LimitedLifePlayer player : playerMap.values()) {
            if (!player.timeFound) {return;}
            player.seconds--;
            Player p = Bukkit.getPlayer(player.id);
            if (p == null) {continue;}

            if (player.seconds <= 0) {
                p.kickPlayer("You've ran out of time.");
            }

            String string = TextFormatting.secondsToTime(player.seconds);
            string = TextFormatting.replaceWithUnicode(string);

            ComponentBuilder builder = new ComponentBuilder("\uDB00\uDE00" + string);
            if (player.seconds > 57600) {
                builder.color(ChatColor.GREEN);
                p.setDisplayName(ChatColor.GREEN + p.getName());
                scoreboard.getTeam("GREEN").addPlayer(p);
            } else if (player.seconds > 28800) {
                builder.color(ChatColor.YELLOW);
                p.setDisplayName(ChatColor.YELLOW + p.getName());
                scoreboard.getTeam("YELLOW").addPlayer(p);
            } else {
                builder.color(ChatColor.RED);
                p.setDisplayName(ChatColor.RED + p.getName());
                scoreboard.getTeam("RED").addPlayer(p);
            }

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, builder.create());
        }
    }

    public static void logoff(UUID id) {
        LimitedLifePlayer player = playerMap.get(id);
        if (player == null) {return;}
        LimitedLife.getSQL().setTimeLeft(player.id, player.seconds);
        playerMap.remove(id);
    }

    public static void saveAll() {
        for (LimitedLifePlayer player : playerMap.values()) {
            LimitedLife.getSQL().setTimeLeft(player.id, player.seconds);
        }
    }

    public static void shutdown() {
        for (LimitedLifePlayer player : playerMap.values()) {
            LimitedLife.getSQL().setTimeLeftNow(player.id, player.seconds);
        }
    }

    public static Collection<LimitedLifePlayer> getPlayers() {return playerMap.values();}

    public static LimitedLifePlayer byUUID(UUID id) {
        return playerMap.computeIfAbsent(id, LimitedLifePlayer::new);
    }

}
