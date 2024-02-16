package me.cooleg.limitedlife;

import me.cooleg.easycommands.CommandRegistry;
import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import me.cooleg.limitedlife.utils.LimitedLifeListener;
import me.cooleg.limitedlife.utils.SQLUtils;
import me.cooleg.limitedlife.utils.TimerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;

public final class LimitedLife extends JavaPlugin {

    private static SQLUtils sql;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigWrapper.loadConfig(this);

        File file = new File(getDataFolder().getAbsolutePath() + "/data.db");
        sql = new SQLUtils(file, this);
        sql.createTable();

        Bukkit.getPluginManager().registerEvents(new LimitedLifeListener(sql), this);
        new TimerRunnable().runTaskTimer(this, 20L, 20L);

        new CommandRegistry().registerCommand(new LimitedLifeCommand(this));
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
        if (scoreboard.getTeam("SPECTATOR") == null) {
            scoreboard.registerNewTeam("SPECTATOR").setColor(ChatColor.GRAY);
        }
    }

    @Override
    public void onDisable() {
        LimitedLifePlayer.shutdown();
    }

    public static SQLUtils getSQL() {
        return sql;
    }
}
