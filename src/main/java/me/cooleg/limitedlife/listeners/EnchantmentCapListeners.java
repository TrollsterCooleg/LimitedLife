package me.cooleg.limitedlife.listeners;

import me.cooleg.limitedlife.data.ConfigWrapper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

public class EnchantmentCapListeners implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            if (itemStack == null) {continue;}
            if (itemStack.getType() == Material.AIR) {continue;}

            restrictItemEnchant(itemStack);
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
    public void inventoryOpen(InventoryOpenEvent event) {
        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            if (itemStack == null) {continue;}
            if (itemStack.getType() == Material.AIR) {continue;}

            restrictItemEnchant(itemStack);
        }

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

        for (ItemStack itemStack : event.getInventory()) {
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
