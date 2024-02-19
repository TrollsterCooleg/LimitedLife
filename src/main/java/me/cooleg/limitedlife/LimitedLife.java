package me.cooleg.limitedlife;

import me.cooleg.easycommands.CommandRegistry;
import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import me.cooleg.limitedlife.listeners.EnchantmentCapListeners;
import me.cooleg.limitedlife.listeners.HelmetListeners;
import me.cooleg.limitedlife.listeners.LimitedLifeListener;
import me.cooleg.limitedlife.utils.OfflinePenaltyHandling;
import me.cooleg.limitedlife.utils.SQLUtils;
import me.cooleg.limitedlife.utils.TimerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;

public final class LimitedLife extends JavaPlugin {

    private static SQLUtils sql;
    private OfflinePenaltyHandling penalty;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigWrapper.loadConfig(this);

        File file = new File(getDataFolder().getAbsolutePath() + "/data.db");
        sql = new SQLUtils(file, this);
        sql.createTable();

        penalty = new OfflinePenaltyHandling(this, sql);

        Bukkit.getPluginManager().registerEvents(new LimitedLifeListener(penalty), this);
        Bukkit.getPluginManager().registerEvents(new EnchantmentCapListeners(), this);
        Bukkit.getPluginManager().registerEvents(new HelmetListeners(), this);
        new TimerRunnable(penalty).runTaskTimer(this, 20L, 20L);

        new CommandRegistry().registerCommand(new LimitedLifeCommand(this, penalty));
        setupTeams();
    }

    private void setupTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getTeam("RED") == null) {
            scoreboard.registerNewTeam("RED").setColor(ChatColor.RED);
        }
        if (scoreboard.getTeam("YELLOW") == null) {
            scoreboard.registerNewTeam("YELLOW").setColor(ChatColor.YELLOW);
        }
        if (scoreboard.getTeam("GREEN") == null) {
            scoreboard.registerNewTeam("GREEN").setColor(ChatColor.GREEN);
        }
        if (scoreboard.getTeam("DARKGREEN") == null) {
            scoreboard.registerNewTeam("DARKGREEN").setColor(ChatColor.DARK_GREEN);
        }
        if (scoreboard.getTeam("SPECTATOR") == null) {
            scoreboard.registerNewTeam("SPECTATOR").setColor(ChatColor.GRAY);
        }
    }

    @Override
    public void onDisable() {
        LimitedLifePlayer.shutdown();
        penalty.saveNow();
    }

    public static SQLUtils getSQL() {
        return sql;
    }
}
