package me.cooleg.limitedlife.utils;

import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LimitedLifeListener implements Listener {

    private final SQLUtils utils;

    public LimitedLifeListener(SQLUtils utils) {
        this.utils = utils;
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        LimitedLifePlayer.byUUID(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        LimitedLifePlayer player = LimitedLifePlayer.byUUID(event.getEntity().getUniqueId());
        player.setSeconds(player.getSeconds() - ConfigWrapper.timeLost);

        event.getEntity().sendTitle(ChatColor.RED + "-" + TextFormatting.secondsToTime(ConfigWrapper.timeLost), null);

        Player killer = event.getEntity().getKiller();
        if (killer == null) {return;}
        LimitedLifePlayer killerPlayer = LimitedLifePlayer.byUUID(killer.getUniqueId());
        killerPlayer.setSeconds(killerPlayer.getSeconds() + ConfigWrapper.timeGained);
        if (killerPlayer.isBoogeyman()) {
            killer.sendTitle(ChatColor.GREEN + "+" + TextFormatting.secondsToTime(2*ConfigWrapper.timeGained), null);
            event.getEntity().sendTitle(ChatColor.RED + "-" + TextFormatting.secondsToTime(2*ConfigWrapper.timeLost), null);
            player.setSeconds(player.getSeconds() - ConfigWrapper.timeLost);
            killerPlayer.setSeconds(killerPlayer.getSeconds() + ConfigWrapper.timeGained);
            killerPlayer.setBoogeyman(false);
        } else {
            killer.sendTitle(ChatColor.GREEN + "+" + TextFormatting.secondsToTime(ConfigWrapper.timeGained), null);
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        LimitedLifePlayer.logoff(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat("%s" + ChatColor.RESET + ": %s");
    }

}
