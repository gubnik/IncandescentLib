package xyz.nikgub.incandescent.autogen_network.interfaces;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Functional interface representing a read
 * function of a {@link FriendlyByteBuf}. Return type
 * is set a {@code T} to account for different types of values
 *
 * @param <T> Type of the object to be read from {@link FriendlyByteBuf}
 */
@FunctionalInterface
public interface PacketReadFunc<T>
{
    /**
     * Abstract function to read from a {@link FriendlyByteBuf}
     *
     * @param buf A {@link FriendlyByteBuf} to be read from
     * @return Value read from {@link FriendlyByteBuf}
     */
    T read (FriendlyByteBuf buf);
}
