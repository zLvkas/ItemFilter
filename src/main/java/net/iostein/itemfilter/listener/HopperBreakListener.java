package net.iostein.itemfilter.listener;

import net.iostein.itemfilter.ItemFilter;
import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.database.object.HopperItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class HopperBreakListener implements Listener {

    private final ItemFilter plugin;
    private final HopperRepository hopperRepository;

    public HopperBreakListener(ItemFilter plugin, HopperRepository hopperRepository) {
        this.plugin = plugin;
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!event.getBlock().getType().equals(Material.HOPPER)) {
            return;
        }

        final Location blockLocation = event.getBlock().getLocation();
        if (blockLocation.getWorld() == null) {
            return;
        }

        this.hopperRepository.getHopper(blockLocation.getWorld().getUID(), blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ())
                .thenAcceptAsync(hopper -> {
                    if (hopper == null) {
                        return;
                    }
                    for (HopperItem item : hopper.getHopperItems()) {
                        blockLocation.getWorld().dropItemNaturally(blockLocation, item.getStack());
                    }
                    this.hopperRepository.removeHopper(hopper.getHopperId());
                }, plugin.getExecutor());
    }

}
