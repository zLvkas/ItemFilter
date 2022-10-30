package net.iostein.itemfilter.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemSerializer {

    public static byte[] serializeItems(ItemStack item) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(byteStream)) {
            outputStream.writeObject(item);
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack deserializeItem(byte[] data) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream inputStream = new BukkitObjectInputStream(byteStream)) {
            return (ItemStack) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
