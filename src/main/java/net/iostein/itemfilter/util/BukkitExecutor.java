package net.iostein.itemfilter.util;

import net.iostein.itemfilter.ItemFilter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class BukkitExecutor implements Executor {

    private final ItemFilter plugin;

    public BukkitExecutor(ItemFilter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Bukkit.getScheduler().runTask(plugin, command);
    }
}
