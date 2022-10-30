package net.iostein.itemfilter.listener;

import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.database.object.FilterType;
import net.iostein.itemfilter.database.object.Hopper;
import net.iostein.itemfilter.database.object.ItemType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class ItemFilterListener implements Listener {

    private final HopperRepository hopperRepository;

    public ItemFilterListener(HopperRepository hopperRepository) {
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onPickup(InventoryPickupItemEvent event) {
        if (!event.getInventory().getType().equals(InventoryType.HOPPER)) {
            return;
        }

        final Location hopperLocation = event.getInventory().getLocation();
        if (hopperLocation == null || hopperLocation.getWorld() == null) {
            return;
        }

        final Hopper hopper;
        try {
            hopper = this.hopperRepository.getHopper(hopperLocation.getWorld().getUID(),
                    hopperLocation.getBlockX(),
                    hopperLocation.getBlockY(),
                    hopperLocation.getBlockZ())
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (hopper.getFilterType().equals(FilterType.DISABLED)) {
            return;
        }

        ItemStack pickedUpItem = event.getItem().getItemStack();
        if (hopper.getFilterType().equals(FilterType.WHITELIST)) {
            if (hopper.getItemsByType(ItemType.ALLOWED).stream().noneMatch(item -> item.isSimilar(pickedUpItem))) {
                event.setCancelled(true);
            }

            return;
        }

        if (hopper.getFilterType().equals(FilterType.BLACKLIST)) {
            if (hopper.getItemsByType(ItemType.BLOCKED).stream().anyMatch(item -> item.isSimilar(pickedUpItem))) {
                event.setCancelled(true);
            }
        }

    }

}
