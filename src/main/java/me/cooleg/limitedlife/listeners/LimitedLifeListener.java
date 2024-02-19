package me.cooleg.limitedlife.listeners;

import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import me.cooleg.limitedlife.utils.OfflinePenaltyHandling;
import me.cooleg.limitedlife.utils.TextFormatting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LimitedLifeListener implements Listener {

    private final OfflinePenaltyHandling penalty;

    public LimitedLifeListener(OfflinePenaltyHandling handling) {
        this.penalty = handling;
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        LimitedLifePlayer.byUUID(event.getPlayer().getUniqueId());
        penalty.penalizePlayer(event.getPlayer());
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
    public void handleNetherite(PrepareSmithingEvent event) {
        if (!ConfigWrapper.netheriteDisabled) {return;}

        if (event.getInventory().contains(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void handlePotions(EntityPotionEffectEvent event) {
        if (!ConfigWrapper.potionsDisabled) {return;}

        boolean apply = switch (event.getCause()) {
            case AREA_EFFECT_CLOUD, ARROW, POTION_DRINK, POTION_SPLASH -> true;
            default -> false;
        };

        if (apply) {
            event.setCancelled(true);
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
