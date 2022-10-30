package net.iostein.itemfilter.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(@NotNull Material material) {
        this(new ItemStack(material));
    }

    @NotNull
    public ItemBuilder setDisplayName(@NotNull String displayName) {
        this.itemMeta.setDisplayName(displayName);
        return this;
    }

    @NotNull
    public ItemBuilder setLore(@NotNull List<String> lines) {
        this.itemMeta.setLore(lines);
        return this;
    }

    @NotNull
    public ItemBuilder setLore(@NotNull String... lines) {
        setLore(List.of(lines));
        return this;
    }

    @NotNull
    public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    @NotNull
    public ItemBuilder addItemFlag(@NotNull ItemFlag... itemFlags) {
        this.itemMeta.addItemFlags(itemFlags);
        return this;
    }

    @NotNull
    public <Z> ItemBuilder addPersistentData(@NotNull NamespacedKey key, @NotNull PersistentDataType<?, Z> data, Z value) {
        this.itemMeta.getPersistentDataContainer().set(key, data, value);
        return this;
    }

    @NotNull
    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }

}