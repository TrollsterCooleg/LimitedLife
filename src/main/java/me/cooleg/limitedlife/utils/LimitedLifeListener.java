package me.cooleg.limitedlife.utils;

import me.cooleg.limitedlife.data.ConfigWrapper;
import me.cooleg.limitedlife.data.LimitedLifePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class LimitedLifeListener implements Listener {

    private final SQLUtils utils;
    private final OfflinePenaltyHandling penalty;

    public LimitedLifeListener(SQLUtils utils, OfflinePenaltyHandling handling) {
        this.penalty = handling;
        this.utils = utils;
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        LimitedLifePlayer.byUUID(event.getPlayer().getUniqueId());
        penalty.penalizePlayer(event.getPlayer());

        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            if (itemStack == null) {continue;}
            if (itemStack.getType() == Material.AIR) {continue;}

            restrictItemEnchant(itemStack);
        }
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
    public void prepareEnchant(PrepareItemEnchantEvent event) {
        for (EnchantmentOffer offer : event.getOffers()) {
            if (offer.getEnchantmentLevel() > ConfigWrapper.maxEnchantmentLevel) {
                offer.setEnchantmentLevel(ConfigWrapper.maxEnchantmentLevel);
            }
        }
    }

    @EventHandler
    public void enchant(EnchantItemEvent event) {
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getValue() > ConfigWrapper.maxEnchantmentLevel) {
                enchants.put(entry.getKey(), ConfigWrapper.maxEnchantmentLevel);
            }
        }
    }

    @EventHandler
    public void prepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) {return;}

        restrictItemEnchant(result);
    }

    @EventHandler
    public void villagerTradeAquire(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        restrictItemEnchant(result);
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
    public void inventoryOpen(InventoryOpenEvent event) {
        for (ItemStack itemStack : event.getInventory()) {
            if (itemStack == null) {continue;}
            if (itemStack.getType() == Material.AIR) {continue;}

            restrictItemEnchant(itemStack);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        for (ItemStack itemStack : event.getWhoClicked().getInventory()) {
            if (itemStack == null) {continue;}
            if (itemStack.getType() == Material.AIR) {continue;}

            restrictItemEnchant(itemStack);
        }
    }

    @EventHandler
    public void pickupItem(EntityPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();

        restrictItemEnchant(itemStack);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        LimitedLifePlayer.logoff(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat("%s" + ChatColor.RESET + ": %s");
    }

    private void restrictItemEnchant(ItemStack stack) {
        if (stack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();

            if (meta != null) {
                for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
                    if (entry.getValue() > ConfigWrapper.maxEnchantmentLevel) {
                        meta.addStoredEnchant(entry.getKey(), ConfigWrapper.maxEnchantmentLevel, false);
                    }
                }

                stack.setItemMeta(meta);
            }
        }

        for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
            if (entry.getValue() > ConfigWrapper.maxEnchantmentLevel) {
                stack.addEnchantment(entry.getKey(), ConfigWrapper.maxEnchantmentLevel);
            }
        }
    }

}
