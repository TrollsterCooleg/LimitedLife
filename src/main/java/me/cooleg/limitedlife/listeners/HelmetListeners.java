package me.cooleg.limitedlife.listeners;

import me.cooleg.limitedlife.data.ConfigWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class HelmetListeners implements Listener {

    @EventHandler
    public void craftPrepare(PrepareItemCraftEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        deleteIfHelmet(event.getInventory().getResult());
    }

    @EventHandler
    public void login(PlayerJoinEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            deleteIfHelmet(itemStack);
        }
    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        for (ItemStack stack : event.getInventory()) {
            deleteIfHelmet(stack);
        }

        for (ItemStack stack : event.getPlayer().getInventory()) {
            deleteIfHelmet(stack);
        }
    }

    @EventHandler
    public void pickup(EntityPickupItemEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        if (event.getItem().getItemStack().getType().toString().contains("HELMET")) {
            event.getItem().remove(); event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        for (ItemStack stack : event.getInventory()) {
            deleteIfHelmet(stack);
        }

        for (ItemStack stack : event.getWhoClicked().getInventory()) {
            deleteIfHelmet(stack);
        }
    }

    @EventHandler
    public void dispenseHelmet(BlockDispenseArmorEvent event) {
        if (!ConfigWrapper.helmetsDisabled) {return;}

        if (event.getItem().getType().toString().contains("HELMET")) {
            event.setCancelled(true);
        }
    }

    private void deleteIfHelmet(ItemStack stack) {
        if (stack == null) {return;}
        if (stack.getType().toString().contains("HELMET")) {stack.setAmount(0);}
    }

}
