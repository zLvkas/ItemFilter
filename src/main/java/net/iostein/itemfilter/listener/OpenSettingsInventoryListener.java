package net.iostein.itemfilter.listener;

import com.google.common.collect.Lists;
import net.iostein.itemfilter.ItemFilter;
import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.database.object.FilterType;
import net.iostein.itemfilter.item.ItemBuilder;
import net.iostein.itemfilter.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class OpenSettingsInventoryListener implements Listener {

    private static final ItemStack DENIED_ITEMS_ITEM = new ItemBuilder(Material.TNT_MINECART)
            .setDisplayName(Constants.FORBIDDEN_ITEMS_NAME)
            .build();

    private static final ItemStack ALLOWED_ITEMS_ITEM = new ItemBuilder(Material.CHEST_MINECART)
            .setDisplayName(Constants.ALLOWED_ITEMS_NAME)
            .build();

    private static final ItemStack DISABLED_FILTER_ITEM = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§cAbschalten")
            .build();

    private final ItemFilter plugin;
    private final HopperRepository hopperRepository;

    public OpenSettingsInventoryListener(ItemFilter plugin, HopperRepository hopperRepository) {
        this.plugin = plugin;
        this.hopperRepository = hopperRepository;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        final Player player = event.getPlayer();
        if (block == null || !player.isSneaking()) {
            return;
        }

        if (!block.getType().equals(Material.HOPPER)) {
            return;
        }

        event.setCancelled(true);

        this.hopperRepository.getHopper(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ())
                .thenAcceptAsync(hopper -> {
                    final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, Constants.MAIN_INVENTORY_NAME);
                    for (int i = 0; i < inventory.getSize(); i++) {
                        inventory.setItem(i, Constants.GLASS);
                    }

                    if (this.hopperRepository.isHopperInUse(hopper.getHopperId())) {
                        player.sendMessage(Constants.PREFIX + "§7Dieser Hopper wird bereits bearbeitet.");
                        return;
                    }

                    final ItemBuilder deniedItems = new ItemBuilder(DENIED_ITEMS_ITEM);
                    final ItemBuilder allowedItems = new ItemBuilder(ALLOWED_ITEMS_ITEM);
                    final ItemBuilder disableFilter = new ItemBuilder(DISABLED_FILTER_ITEM);

                    allowedItems.setLore(this.getLore(hopper.getFilterType() == FilterType.WHITELIST, FilterType.WHITELIST));
                    deniedItems.setLore(this.getLore(hopper.getFilterType() == FilterType.BLACKLIST, FilterType.BLACKLIST));

                    inventory.setItem(10, deniedItems.build());
                    inventory.setItem(12, allowedItems.build());
                    inventory.setItem(16, disableFilter.build());

                    this.hopperRepository.setLastClickedHopper(player.getUniqueId(), hopper);
                    player.closeInventory();
                    this.hopperRepository.setLastClickedHopper(player.getUniqueId(), hopper);
                    player.openInventory(inventory);
                }, plugin.getExecutor());


    }

    private List<String> getLore(boolean enabled, FilterType type) {
        if (type == FilterType.DISABLED) {
            return Collections.emptyList();
        }

        final String firstLine = switch (type) {
            case BLACKLIST -> "§7Filtere hier nach Items, welche §cnicht §7eingesammelt werden sollen.";
            case WHITELIST -> "§7Filtere hier Items, die §aeingesammelt §7werden sollen.";
            default -> "";
        };

        return Lists.newArrayList(firstLine,
                "",
                "§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert"),
                "",
                "§8Klicke um diese Filter Option zu " + (enabled ? "aktivieren" : "deaktivieren") + ".");
    }

}
