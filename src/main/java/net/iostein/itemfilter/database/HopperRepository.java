package net.iostein.itemfilter.database;

import net.iostein.itemfilter.database.object.FilterType;
import net.iostein.itemfilter.database.object.Hopper;
import net.iostein.itemfilter.database.object.ItemType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HopperRepository {

    @NotNull CompletableFuture<Hopper> getHopper(@NotNull UUID worldUniqueId, int x, int y, int z);

    @Nullable CompletableFuture<Hopper> getHopper(int hopperId);

    CompletableFuture<Boolean> removeHopper(int hopperId);

    CompletableFuture<Boolean> addHopperItem(int hopperId, @NotNull ItemStack item, @NotNull ItemType type);

    CompletableFuture<Boolean> removeHopperItem(int hopperId, int itemId);

    CompletableFuture<Boolean> updateHopperType(int hopperId, @NotNull FilterType type);

    @Nullable Hopper getLastClickedHopper(@NotNull UUID playerUniqueId);

    void setLastClickedHopper(@NotNull UUID uniqueId, Hopper hopper);

    void releaseHopper(@NotNull UUID uniqueId);

    boolean isHopperInUse(int hopperId);
}
