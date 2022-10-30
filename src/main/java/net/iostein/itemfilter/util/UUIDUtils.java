package net.iostein.itemfilter.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtils {

    @NotNull
    public static UUID fromBytes(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static byte[] asBytes(@NotNull UUID uuid) {
        return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
    }

}
