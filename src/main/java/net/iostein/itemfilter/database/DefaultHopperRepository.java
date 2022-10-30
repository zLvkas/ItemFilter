package net.iostein.itemfilter.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.iostein.itemfilter.database.object.FilterType;
import net.iostein.itemfilter.database.object.Hopper;
import net.iostein.itemfilter.database.object.HopperItem;
import net.iostein.itemfilter.database.object.ItemType;
import net.iostein.itemfilter.item.ItemSerializer;
import net.iostein.itemfilter.util.UUIDUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DefaultHopperRepository implements HopperRepository {

    private static final String CREATE_HOPPER_STORAGE = """
            CREATE TABLE IF NOT EXISTS `hopper`
            (
                `hopperId`      INT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
                `worldUniqueId` BINARY(16) NOT NULL,
                `locationX`     INT        NOT NULL,
                `locationY`     INT        NOT NULL,
                `locationZ`     INT        NOT NULL,
                `filterType`    SMALLINT,
                UNIQUE (`worldUniqueId`, `locationX`, `locationY`, `locationZ`)
            );
            """;
    private static final String CREATE_ITEM_STORAGE = """
                CREATE TABLE IF NOT EXISTS `hopper_items`
                (
                    `itemId`   INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    `hopperId` INT  NOT NULL,
                    `item`     BLOB NOT NULL,
                    `type`     SMALLINT NOT NULL,
                    CONSTRAINT FK_hopperId FOREIGN KEY (`hopperId`) REFERENCES `hopper` (`hopperId`) ON DELETE CASCADE
                );
            """;

    private final Cache<Integer, Hopper> hopperCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

    private final Map<UUID, Hopper> currentHopperEdits = new HashMap<>();

    private final DatabaseDriver databaseDriver;

    public DefaultHopperRepository(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;

        try (Connection connection = this.databaseDriver.getConnection();
             PreparedStatement hopperStorage = connection.prepareStatement(CREATE_HOPPER_STORAGE);
             PreparedStatement itemStorage = connection.prepareStatement(CREATE_ITEM_STORAGE)) {
            hopperStorage.executeUpdate();
            itemStorage.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public @NotNull CompletableFuture<Hopper> getHopper(@NotNull UUID worldUniqueId, int x, int y, int z) {
        Hopper cachedHopper = null;
        for (Hopper hopper : this.hopperCache.asMap().values()) {
            if (hopper.getWorldUniqueId().equals(worldUniqueId) && hopper.getX() == x && hopper.getY() == y && hopper.getZ() == z) {
                cachedHopper = hopper;
            }
        }

        if (cachedHopper != null) {
            return CompletableFuture.completedFuture(cachedHopper);
        }

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM `hopper` `h` LEFT JOIN `hopper_items` `hi` ON `h`.`hopperId` = `hi`.`hopperId` WHERE `worldUniqueId` = ? AND `locationX` = ? AND `locationY` = ? AND `locationZ` = ?;")) {
                statement.setBytes(1, UUIDUtils.asBytes(worldUniqueId));
                statement.setInt(2, x);
                statement.setInt(3, y);
                statement.setInt(4, z);

                final ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `hopper` (`worldUniqueId`, `locationX`, `locationY`, `locationZ`, `filterType`) VALUES (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
                    insertStatement.setBytes(1, UUIDUtils.asBytes(worldUniqueId));
                    insertStatement.setInt(2, x);
                    insertStatement.setInt(3, y);
                    insertStatement.setInt(4, z);
                    insertStatement.setInt(5, FilterType.DISABLED.ordinal());
                    insertStatement.executeUpdate();

                    final ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (!generatedKeys.next()) {
                        throw new IllegalStateException("Unable to insert hopper in database.");
                    }

                    Hopper hopper = new Hopper(generatedKeys.getInt(1), worldUniqueId, x, y, z, FilterType.DISABLED, new ArrayList<>());
                    hopperCache.put(hopper.getHopperId(), hopper);
                    return hopper;
                }
                return this.fromResultSet(resultSet);
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to insert hopper in database.", exception);
            }
        });
    }

    @Override
    public @Nullable CompletableFuture<Hopper> getHopper(int hopperId) {
        return CompletableFuture.supplyAsync(() -> {
            final Hopper cached = this.hopperCache.getIfPresent(hopperId);
            if (cached != null) {
                return cached;
            }

            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM `hopper` `h` LEFT JOIN `hopper_items` `hi` ON `h`.`hopperId` = `hi`.`hopperId` WHERE `h`.`hopperId` = ?;")) {
                statement.setInt(1, hopperId);
                final ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    return null;
                }

                return this.fromResultSet(resultSet);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> removeHopper(int hopperId) {
        return CompletableFuture.supplyAsync(() -> {
            hopperCache.invalidate(hopperId);
            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM `hopper` WHERE `hopperId` = ?;")) {
                statement.setInt(1, hopperId);

                return statement.executeUpdate() > 0;
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            return false;
        });
    }

    @NotNull
    private Hopper fromResultSet(@NotNull ResultSet resultSet) throws SQLException {
        Hopper hopper = new Hopper(
                resultSet.getInt("hopperId"),
                UUIDUtils.fromBytes(resultSet.getBytes("worldUniqueId")),
                resultSet.getInt("locationX"),
                resultSet.getInt("locationY"),
                resultSet.getInt("locationZ"),
                FilterType.values()[resultSet.getInt("filterType")],
                new ArrayList<>()
        );

        do {
            int itemId = resultSet.getInt("itemId");
            if (resultSet.wasNull()) {
                break;
            }

            hopper.getHopperItems().add(new HopperItem(
                    itemId,
                    ItemSerializer.deserializeItem(resultSet.getBytes("item")),
                    ItemType.values()[resultSet.getInt("type")]
            ));
        } while (resultSet.next());

        return hopper;
    }

    @Override
    public CompletableFuture<Boolean> updateHopperType(int hopperId, @NotNull FilterType type) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE `hopper` SET filterType = ? WHERE hopperId = ?")) {
                statement.setInt(1, type.ordinal());
                statement.setInt(2, hopperId);
                statement.executeUpdate();
                return statement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> addHopperItem(int hopperId, @NotNull ItemStack item, @NotNull ItemType type) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO `hopper_items` (`hopperId`, `item`, `type`) VALUES (?, ?, ?)")) {
                statement.setInt(1, hopperId);
                statement.setBytes(2, ItemSerializer.serializeItems(item));
                statement.setInt(3, type.ordinal());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            this.hopperCache.invalidate(hopperId);
            return true;
        });

    }

    @Override
    public CompletableFuture<Boolean> removeHopperItem(int hopperId, int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            int result = 0;
            try (Connection connection = this.databaseDriver.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM `hopper_items` WHERE `itemId` = ?")) {
                statement.setInt(1, itemId);
                result = statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            this.hopperCache.invalidate(hopperId);
            return result > 0;
        });
    }

    @Override
    public @Nullable Hopper getLastClickedHopper(@NotNull UUID playerUniqueId) {
        Hopper hopper = this.currentHopperEdits.get(playerUniqueId);
        if (hopper != null) {
            return hopper;
        } else {
            throw new IllegalStateException("No hopper selected.");
        }
    }

    @Override
    public void setLastClickedHopper(@NotNull UUID uniqueId, Hopper hopper) {
        this.currentHopperEdits.put(uniqueId, hopper);
    }

    @Override
    public void releaseHopper(@NotNull UUID uniqueId) {
        this.currentHopperEdits.remove(uniqueId);
    }

    @Override
    public boolean isHopperInUse(int hopperId) {
        for (Hopper editing : this.currentHopperEdits.values()) {
            if (editing.getHopperId() == hopperId) {
                return true;
            }
        }

        return false;
    }
}
