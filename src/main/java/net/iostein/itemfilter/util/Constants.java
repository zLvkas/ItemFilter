package net.iostein.itemfilter.util;

import com.google.gson.Gson;
import net.iostein.itemfilter.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public interface Constants {

    Gson GSON = new Gson();
    String PREFIX = "§a§lFilter §8» ";
    String MAIN_INVENTORY_NAME = "§8» §e§lTrichter§8-§6Filter";
    String FORBIDDEN_ITEMS_NAME = "§8» §c§lVerbotene Items";
    String ALLOWED_ITEMS_NAME = "§8» §a§lErlaubte Items";

    NamespacedKey PLACEHOLDER_KEY = NamespacedKey.fromString("itemfilter:placeholder");

    ItemStack GLASS = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setDisplayName(" ").build();

}
