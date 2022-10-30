package net.iostein.itemfilter.listener;

import net.iostein.itemfilter.ItemFilter;
import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.database.object.FilterType;
import net.iostein.itemfilter.database.object.Hopper;
import net.iostein.itemfilter.database.object.HopperItem;
import net.iostein.itemfilter.database.object.ItemType;
import net.iostein.itemfilter.item.ItemBuilder;
import net.iostein.itemfilter.util.Constants;
import net.iostein.itemfilter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilterSettingsInventoryListener implements Listener {

    private static final ItemStack RANK_REQUIRED_ITEM = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§cNicht Freigeschaltet")
            .setLore("§7Du kannst diesen Slot durch den Kauf eines Ranges freischalten.", "§estore.iostein.net")
            .addPersistentData(Constants.PLACEHOLDER_KEY, PersistentDataType.BYTE, (byte) 0)
            .build();

    private final ItemFilter plugin;
    private final HopperRepository hopperRepository;

    public FilterSettingsInventoryListener(ItemFilter plugin, HopperRepository hopperRepository) {
        this.plugin = plugin;
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        if (!event.getView().getTitle().equals(Constants.MAIN_INVENTORY_NAME)) {
            return;
        }

        event.setCancelled(true);

        final Hopper hopper = this.hopperRepository.getLastClickedHopper(event.getWhoClicked().getUniqueId());
        if (hopper == null) {
            //TODO: Send message?
            player.closeInventory();
            return;
        }

        final Material itemType = event.getCurrentItem().getType();
        if (itemType.equals(Material.BARRIER)) {
            if (event.getClick().equals(ClickType.LEFT)) {
                this.updateFilter(player, hopper, FilterType.DISABLED);
                return;
            }
        }

        final List<HopperItem> hopperItems = hopper.getHopperItems();
        if (itemType.equals(Material.TNT_MINECART)) {
            if (event.getClick().equals(ClickType.LEFT)) {
                this.updateFilter(player, hopper, FilterType.BLACKLIST);
                return;
            }

            player.openInventory(this.createDefaultInventory(player, Constants.FORBIDDEN_ITEMS_NAME, event.getCurrentItem(), getItems(hopperItems, ItemType.BLOCKED)));
            this.hopperRepository.setLastClickedHopper(player.getUniqueId(), hopper);
            return;
        }

        if (itemType.equals(Material.CHEST_MINECART)) {
            if (event.getClick().equals(ClickType.LEFT)) {
                this.updateFilter(player, hopper, FilterType.WHITELIST);
                return;
            }

            player.openInventory(this.createDefaultInventory(player, Constants.ALLOWED_ITEMS_NAME, event.getCurrentItem(), getItems(hopperItems, ItemType.ALLOWED)));
            this.hopperRepository.setLastClickedHopper(player.getUniqueId(), hopper);
        }
    }

    private List<ItemStack> getItems(List<HopperItem> items, ItemType type) {
        return items.stream()
                .filter(hopperItem -> hopperItem.getType() == type)
                .map(HopperItem::getStack)
                .toList();
    }

    private void updateFilter(@NotNull Player player, @NotNull Hopper hopper, @NotNull FilterType filterType) {
        this.hopperRepository.updateHopperType(hopper.getHopperId(), filterType).thenAcceptAsync(result -> {
            hopper.setFilterType(filterType);
            player.closeInventory();
            player.sendMessage(Constants.PREFIX + String.format("§7Du hast den Filtermodus §e%s §7ausgewählt.", filterType.getDisplayName()));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }, plugin.getExecutor());

    }

    private Inventory createDefaultInventory(@NotNull Player player, @NotNull String inventoryTitle, @NotNull ItemStack mainItem, @NotNull List<ItemStack> items) {
        final Inventory inventory = Bukkit.createInventory(null, 27, inventoryTitle);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, Constants.GLASS);
        }

        inventory.setItem(10, mainItem);

        for (int i = 12; i < 17; i++) {
            inventory.setItem(i, null);
        }

        for (int i = 0; i < items.size(); i++) {
            inventory.setItem(12 + i, items.get(i));
        }

        for (int slot = 12 + InventoryUtil.getMaxSlots(player); slot < 17; slot++) {
            inventory.setItem(slot, RANK_REQUIRED_ITEM);
        }

        return inventory;
    }

}
