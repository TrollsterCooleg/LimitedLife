package me.cooleg.limitedlife;

import me.cooleg.easycommands.Command;
import me.cooleg.easycommands.commandmeta.SubCommand;
import me.cooleg.easycommands.commandmeta.TabCompleter;
import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import me.cooleg.limitedlife.utils.OfflinePenaltyHandling;
import me.cooleg.limitedlife.utils.TextFormatting;
import me.cooleg.limitedlife.utils.TimerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class LimitedLifeCommand implements Command {

    private final Random random = new Random();
    private final LimitedLife limitedLife;
    private final OfflinePenaltyHandling penalty;

    public LimitedLifeCommand(LimitedLife limitedLife, OfflinePenaltyHandling handling) {
        this.penalty = handling;
        this.limitedLife = limitedLife;
    }

    @Override
    public boolean rootCommand(CommandSender commandSender, String alias) {
        commandSender.sendMessage(ChatColor.YELLOW + "Plugin coded by @cooleg on discord!");
        commandSender.sendMessage(ChatColor.AQUA + "Subcommands: addtime <player> <seconds>, removetime <player> <seconds>, addkill <player>, adddeath <player>, pausetime, boogeyman <count>, cure <player>, punish, checktime <player>, setdeduction <number>, offlinepenalty <true/false>");
        return true;
    }

    @Override
    public boolean noMatch(CommandSender commandSender, String alias, String[] strings) {
        return rootCommand(commandSender, alias);
    }

    @SubCommand("addtime")
    public boolean addTimeSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 3) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[1]);
        if (!target.isOnline()) {
            try {
                int time = Integer.parseInt(strings[2]);
                LimitedLife.getSQL().addTimeOffline(target.getUniqueId(), time);
                sender.sendMessage(ChatColor.GREEN + "Time hopefully added to offline player!");
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "Invalid argument for seconds!");
            }
            return true;
        }

        try {
            Player online = target.getPlayer();
            int time = Integer.parseInt(strings[2]);
            LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
            player.setSeconds(player.getSeconds() + time);
            online.sendTitle(ChatColor.GREEN + "+" + TextFormatting.secondsToTime(time), null);
            sender.sendMessage(ChatColor.GREEN + "Time successfully added to player!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for seconds!");
        }

        return true;
    }

    @SubCommand("removetime")
    public boolean removeTimeSubcommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 3) {sender.sendMessage(ChatColor.RED + "Incorrect Arguments!"); return true;}
        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[1]);
        if (!target.isOnline()) {
            try {
                int time = Integer.parseInt(strings[2]);
                LimitedLife.getSQL().subtractTimeOffline(target.getUniqueId(), time);
                sender.sendMessage(ChatColor.GREEN + "Time hopefully removed from offline player!");
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "Invalid argument for seconds!");
            }
            return true;
        }

        try {
            Player online = target.getPlayer();
            int time = Integer.parseInt(strings[2]);
            LimitedLifePlayer player = LimitedLifePlayer.byUUID(target.getUniqueId());
            player.setSeconds(player.getSeconds() - time);
            online.sendTitle(ChatColor.RED + "-" + TextFormatting.secondsToTime(time), null);
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
            List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
            players = players.stream().filter((player) -> LimitedLifePlayer.byUUID(player.getUniqueId()).getSeconds() > 28800).toList();

            if (boogeymen > players.size()) {sender.sendMessage(ChatColor.RED + "More boogeymen than non-red players online!"); return true;}

            for (int i = 0; i < boogeymen; i++) {
                int rand = random.nextInt(players.size());
                LimitedLifePlayer player = LimitedLifePlayer.byUUID(players.get(rand).getUniqueId());
                if (player.isBoogeyman()) {i--; continue;}
                player.setBoogeyman(true);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(ChatColor.RED + "You are...", null);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());
                        String title = limitedLifePlayer.isBoogeyman() ? ChatColor.RED + "The boogeyman!" : ChatColor.GREEN + "NOT the boogeyman!";
                        player.sendTitle(title, null);
                    }
                }
            }.runTaskLater(limitedLife,60);


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

            if (limitedLifePlayer.getSeconds() > ConfigWrapper.initialTime) {
                limitedLifePlayer.setSeconds(ConfigWrapper.initialTime);
            } else if (limitedLifePlayer.getSeconds() > ConfigWrapper.yellowTime) {
                limitedLifePlayer.setSeconds(ConfigWrapper.yellowTime);
            } else if (limitedLifePlayer.getSeconds() > ConfigWrapper.redTime) {
                limitedLifePlayer.setSeconds(ConfigWrapper.redTime);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Punished remaining boogeymen: " + remaining);
        return true;
    }

    @SubCommand("checktime")
    public boolean checkTimeCommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {return true;}
        String name = strings[1];
        Player player = Bukkit.getPlayer(name);
        if (player == null) {sender.sendMessage(ChatColor.RED + "Player does not exist!");}
        LimitedLifePlayer limitedLifePlayer = LimitedLifePlayer.byUUID(player.getUniqueId());

        long seconds = limitedLifePlayer.getSeconds();
        if (seconds > ConfigWrapper.initialTime) {
            sender.sendMessage(ChatColor.DARK_GREEN + TextFormatting.secondsToTime(seconds));
        } else if (seconds > ConfigWrapper.yellowTime) {
            sender.sendMessage(ChatColor.GREEN + TextFormatting.secondsToTime(seconds));
        } else if (seconds > ConfigWrapper.redTime) {
            sender.sendMessage(ChatColor.YELLOW + TextFormatting.secondsToTime(seconds));
        } else if (seconds > 0) {
            sender.sendMessage(ChatColor.RED + TextFormatting.secondsToTime(seconds));
        } else {
            sender.sendMessage(ChatColor.GRAY + "This player is dead :(");
        }

        return true;
    }

    @SubCommand("setdeduction")
    public boolean setDeductionCommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {return true;}
        String deductionString = strings[1];

        try {
            int deduction = Integer.parseInt(deductionString);
            LimitedLifePlayer.setSecondDeduction(deduction);
            sender.sendMessage(ChatColor.GREEN + "Set time deduction multiplier to " + deduction + "!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for deduction!");
        }

        return true;
    }

    @SubCommand("offlinepenalty")
    public boolean togglePenaltyCommand(CommandSender sender, String alias, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(ChatColor.AQUA + "Offline penalty is currently: " + penalty.isEnabled());
            return true;
        }

        String penaltyString = strings[1];

        try {
            boolean doPenalty = Boolean.parseBoolean(penaltyString);
            penalty.setEnabled(doPenalty);
            sender.sendMessage(ChatColor.GREEN + "Set offline penalty timer to " + doPenalty + "!");
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatColor.RED + "Invalid argument for on/off!");
        }

        return true;
    }

    @TabCompleter("offlinepenalty")
    public List<String> togglePenaltyTabComplete(CommandSender sender, String alias, String[] strings) {
        return List.of("true", "false");
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
