package net.iostein.itemfilter.util;

import org.bukkit.entity.Player;

public class InventoryUtil {

    public static int getMaxSlots(Player player) {
        if (player.hasPermission("rank.alpha")) {
            return 5;
        } else if (player.hasPermission("rank.pro")) {
            return 4;
        }

        return 3;
    }
}
