package me.cooleg.limitedlife;

import me.cooleg.easycommands.Command;
import me.cooleg.easycommands.commandmeta.SubCommand;
import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import me.cooleg.limitedlife.utils.TextFormatting;
import me.cooleg.limitedlife.utils.TimerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class LimitedLifeCommand implements Command {

    private final Random random = new Random();

    @Override
    public boolean rootCommand(CommandSender commandSender, String alias) {
        commandSender.sendMessage(ChatColor.YELLOW + "Plugin coded by @cooleg on discord!");
        commandSender.sendMessage(ChatColor.AQUA + "Subcommands: addtime <player> <seconds>, removetime <player> <seconds>, addkill <player>, adddeath <player>, pausetime, boogeyman <count>, cure <player>, punish");
        return true;
    }

    @Override
    public boolean noMatch(CommandSender commandSender, String alias, String[] strings) {
        return rootCommand(commandSender, alias);
    }

    @SubCommand("addtime")
    public boolean addTimeSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 3) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        Player target = Bukkit.getPlayer(strings[1]);
        if (target == null) {sender.sendMessage(ChatColor.RED + "Could not find player by the name of " + strings[1] + "."); return true;}
        try {
            int time = Integer.parseInt(strings[2]);
            LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
            player.setSeconds(player.getSeconds() + time);
            target.sendTitle(ChatColor.GREEN + "+" + TextFormatting.secondsToTime(time), null);
            sender.sendMessage(ChatColor.GREEN + "Time successfully added to player!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for seconds!");
        }

        return true;
    }

    @SubCommand("removetime")
    public boolean removeTimeSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 3) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        Player target = Bukkit.getPlayer(strings[1]);
        if (target == null) {sender.sendMessage(ChatColor.RED + "Could not find player by the name of " + strings[1] + "."); return true;}
        try {
            int time = Integer.parseInt(strings[2]);
            LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
            player.setSeconds(player.getSeconds() - time);
            target.sendTitle(ChatColor.RED + "-" + TextFormatting.secondsToTime(time), null);
            sender.sendMessage(ChatColor.GREEN + "Time successfully removed from player!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for seconds!");
        }

        return true;
    }

    @SubCommand("addkill")
    public boolean addKillSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        Player target = Bukkit.getPlayer(strings[1]);
        if (target == null) {sender.sendMessage(ChatColor.RED + "Could not find player by the name of " + strings[1] + "."); return true;}

        LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
        player.setSeconds(player.getSeconds() + ConfigWrapper.timeGained);
        target.sendTitle(ChatColor.GREEN + "+" + TextFormatting.secondsToTime(ConfigWrapper.timeGained), null);
        sender.sendMessage(ChatColor.GREEN + "Kill time given to player!");

        return true;
    }

    @SubCommand("adddeath")
    public boolean addDeathSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        Player target = Bukkit.getPlayer(strings[1]);
        if (target == null) {sender.sendMessage(ChatColor.RED + "Could not find player by the name of " + strings[1] + "."); return true;}

        LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
        player.setSeconds(player.getSeconds() +- ConfigWrapper.timeLost);
        target.sendTitle(ChatColor.RED + "-" + TextFormatting.secondsToTime(ConfigWrapper.timeLost), null);
        sender.sendMessage(ChatColor.GREEN + "Death time removed from player!");

        return true;
    }

    @SubCommand("pausetime")
    public boolean pauseTimeSubcommand(CommandSender sender, String alias, String[] strings) {
        TimerRunnable.enabled = !TimerRunnable.enabled;
        sender.sendMessage(ChatColor.GREEN + "Time being enabled is now " + TimerRunnable.enabled);
        return true;
    }

    @SubCommand("boogeyman")
    public boolean boogeymanSubcommand(CommandSender sender, String alias, String[] strings) {
        for (LimitedLifePlayer player : LimitedLifePlayer.getPlayers()) {
            if (player.isBoogeyman()) {sender.sendMessage(ChatColor.RED + "There is still an active boogeyman!"); return true;}
        }

        if (strings.length < 2) {return true;}
        try {
            int boogeymen = Integer.parseInt(strings[1]);
            int size = Bukkit.getOnlinePlayers().size();
            List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();

            if (boogeymen > size) {sender.sendMessage(ChatColor.RED + "More boogeymen than players online!"); return true;}

            int done = 0;

            for (int i = 0; i < boogeymen; i++) {
                int rand = random.nextInt(size);
                LimitedLifePlayer player = LimitedLifePlayer.byUUID(players.get(rand).getUniqueId());
                if (player.isBoogeyman()) {i--; continue;}
                player.setBoogeyman(true);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());
                String title = limitedLifePlayer.isBoogeyman() ? ChatColor.RED + "You ARE the boogeyman!" : ChatColor.GREEN + "You are NOT the boogeyman!";
                player.sendTitle(title, null);
            }

            sender.sendMessage(ChatColor.GREEN + "Chose boogeymen!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for number of boogeymen!");
        }

        return true;
    }

    @SubCommand("cure")
    public boolean cureSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {return true;}
        Player player = Bukkit.getPlayer(strings[1]);
        if (player == null) {sender.sendMessage(ChatColor.RED + "That player is not online!"); return true;}
        LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());
        if (!limitedLifePlayer.isBoogeyman()) {sender.sendMessage(ChatColor.RED + player.getName() + " is not a boogeyman!"); return true;}
        limitedLifePlayer.setBoogeyman(false);
        sender.sendMessage(ChatColor.GREEN + "Successfully removed Boogeyman status from " + player.getName() + ".");
        return true;
    }

    @SubCommand("punish")
    public boolean punishSubcommand(CommandSender sender, String alias, String[] strings) {
        String remaining = "";
        for (Player player : Bukkit.getOnlinePlayers()) {
            LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());
            if (!limitedLifePlayer.isBoogeyman()) {continue;}

            limitedLifePlayer.setBoogeyman(false);
            remaining += player.getName() + ",";

            if (limitedLifePlayer.getSeconds() > 57600) {
                limitedLifePlayer.setSeconds(57600);
            } else if (limitedLifePlayer.getSeconds() > 28800) {
                limitedLifePlayer.setSeconds(28800);
            } else {
                limitedLifePlayer.setSeconds(0);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Punished remaining boogeymen: " + remaining);
        return true;
    }

    @Nonnull
    @Override
    public String name() {
        return "limitedlife";
    }

    @Override
    public String permission() {
        return "limitedlife.command";
    }
}
