package net.iostein.itemfilter.database.object;

import org.bukkit.inventory.ItemStack;

public class HopperItem {

    private final int itemId;
    private final ItemStack stack;
    private final ItemType type;

    public HopperItem(int itemId, ItemStack stack, ItemType type) {
        this.itemId = itemId;
        this.stack = stack;
        this.type = type;
    }

    public int getItemId() {
        return this.itemId;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public ItemType getType() {
        return this.type;
    }
}
