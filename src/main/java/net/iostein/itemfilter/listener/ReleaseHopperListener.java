package net.iostein.itemfilter.listener;

import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.util.Constants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ReleaseHopperListener implements Listener {

    private final HopperRepository hopperRepository;

    public ReleaseHopperListener(HopperRepository hopperRepository) {
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.hopperRepository.releaseHopper(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        final String inventoryName = event.getView().getTitle();
        if (inventoryName.equals(Constants.MAIN_INVENTORY_NAME) || inventoryName.equals(Constants.FORBIDDEN_ITEMS_NAME) || inventoryName.equals(Constants.ALLOWED_ITEMS_NAME)) {
            this.hopperRepository.releaseHopper(event.getPlayer().getUniqueId());
        }
    }

}
