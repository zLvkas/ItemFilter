package net.iostein.itemfilter.listener;

import com.google.common.collect.Lists;
import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.database.object.Hopper;
import net.iostein.itemfilter.database.object.HopperItem;
import net.iostein.itemfilter.database.object.ItemType;
import net.iostein.itemfilter.util.Constants;
import net.iostein.itemfilter.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FilterEditInventoryListener implements Listener {

    private final HopperRepository hopperRepository;

    public FilterEditInventoryListener(HopperRepository hopperRepository) {
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        final String title = event.getView().getTitle();
        if (event.getClickedInventory().getType() == InventoryType.CHEST && (title.equals(Constants.FORBIDDEN_ITEMS_NAME) || title.equals(Constants.ALLOWED_ITEMS_NAME))) {
            final int maxSlots = InventoryUtil.getMaxSlots((Player) event.getWhoClicked());
            if (event.getSlot() < 12 || event.getSlot() >= 12 + maxSlots) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        final String title = event.getView().getTitle();
        if (!(event.getInventory().getType() == InventoryType.CHEST && (title.equals(Constants.FORBIDDEN_ITEMS_NAME) || title.equals(Constants.ALLOWED_ITEMS_NAME)))) {
            return;
        }

        final Hopper hopper = this.hopperRepository.getLastClickedHopper(event.getPlayer().getUniqueId());
        if (hopper == null) {
            return;
        }

        final Player player = (Player) event.getPlayer();

        if (title.equals(Constants.ALLOWED_ITEMS_NAME)) {
            this.updateItems(hopper, this.getItems(player, event.getInventory()), ItemType.ALLOWED);
        } else if (title.equals(Constants.FORBIDDEN_ITEMS_NAME)) {
            this.updateItems(hopper, this.getItems(player, event.getInventory()), ItemType.BLOCKED);
        }
    }

    private void updateItems(Hopper hopper, List<ItemStack> items, ItemType type) {
        List<ItemStack> hopperItems = hopper.getHopperItems().stream()
                .filter(hopperItem -> hopperItem.getType() == type)
                .map(HopperItem::getStack)
                .toList();

        for (ItemStack item : items) {
            if (!this.contains(hopperItems, item)) {
                hopperRepository.addHopperItem(hopper.getHopperId(), item, type);
            }
        }

        for (HopperItem hopperItem : hopper.getHopperItems()) {
            if (hopperItem.getType() == type && !this.contains(items, hopperItem.getStack())) {
                this.hopperRepository.removeHopperItem(hopper.getHopperId(), hopperItem.getItemId());
            }
        }
    }

    private boolean contains(List<ItemStack> items, ItemStack item) {
        for (ItemStack itemStack : items) {
            if (itemStack.isSimilar(item)) {
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> getItems(Player player, Inventory inventory) {
        List<ItemStack> items = Lists.newLinkedList();
        for (int i = 12; i < 12 + InventoryUtil.getMaxSlots(player); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

}
