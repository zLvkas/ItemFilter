package net.iostein.itemfilter.database.object;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Hopper {

    private final int hopperId;
    private final UUID worldUniqueId;
    private final int x;

    private final int y;

    private final int z;

    private FilterType filterType;

    private final List<HopperItem> hopperItems;

    public Hopper(int hopperId, UUID worldUniqueId, int x, int y, int z, FilterType filterType, List<HopperItem> hopperItems) {
        this.hopperId = hopperId;
        this.worldUniqueId = worldUniqueId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.filterType = filterType;
        this.hopperItems = hopperItems;
    }

    public int getHopperId() {
        return this.hopperId;
    }

    @NotNull
    public UUID getWorldUniqueId() {
        return this.worldUniqueId;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    @NotNull
    public FilterType getFilterType() {
        return this.filterType;
    }

    public void setFilterType(@NotNull FilterType filterType) {
        this.filterType = filterType;
    }

    @NotNull
    public List<HopperItem> getHopperItems() {
        return this.hopperItems;
    }

    @NotNull
    public List<ItemStack> getItemsByType(ItemType type) {
        return this.hopperItems.stream()
                .filter(hopperItem -> hopperItem.getType() == type)
                .map(HopperItem::getStack)
                .toList();
    }
}
