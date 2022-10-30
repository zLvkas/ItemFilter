package net.iostein.itemfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.iostein.itemfilter.config.DatabaseConfig;
import net.iostein.itemfilter.database.DatabaseDriver;
import net.iostein.itemfilter.database.DefaultHopperRepository;
import net.iostein.itemfilter.database.HopperRepository;
import net.iostein.itemfilter.listener.*;
import net.iostein.itemfilter.util.BukkitExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ItemFilter extends JavaPlugin {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private DatabaseDriver databaseDriver;
    private BukkitExecutor executor;

    @Override
    public void onEnable() {
        try {
            Files.createDirectories(super.getDataFolder().toPath());
            final File file = new File(super.getDataFolder(), "database.json");
            if (!file.exists()) {
                try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
                    GSON.toJson(new DatabaseConfig(), fileWriter);
                }
            }

            try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
                this.databaseDriver = new DatabaseDriver(GSON.fromJson(fileReader, DatabaseConfig.class));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        this.executor = new BukkitExecutor(this);

        final HopperRepository hopperRepository = new DefaultHopperRepository(this.databaseDriver);
        Bukkit.getPluginManager().registerEvents(new ItemFilterListener(hopperRepository), this);
        Bukkit.getPluginManager().registerEvents(new FilterSettingsInventoryListener(this, hopperRepository), this);
        Bukkit.getPluginManager().registerEvents(new ReleaseHopperListener(hopperRepository), this);
        Bukkit.getPluginManager().registerEvents(new OpenSettingsInventoryListener(this, hopperRepository), this);
        Bukkit.getPluginManager().registerEvents(new FilterEditInventoryListener(hopperRepository), this);
        Bukkit.getPluginManager().registerEvents(new HopperBreakListener(this, hopperRepository), this);
    }

    @Override
    public void onDisable() {
        this.databaseDriver.close();
    }

    public BukkitExecutor getExecutor() {
        return this.executor;
    }
}