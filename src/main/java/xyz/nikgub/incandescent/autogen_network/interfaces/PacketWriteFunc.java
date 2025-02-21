package xyz.nikgub.incandescent.autogen_network.interfaces;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Functional interface representing a write
 * function of a {@link FriendlyByteBuf}. Return type
 * is set a {@code void} to account for potential
 * {@link FriendlyByteBuf}/{@link io.netty.buffer.ByteBuf}/{@code void} return types,
 * since we do not care for their return values
 *
 * @param <T> Type of the object to be written into {@link FriendlyByteBuf}
 */
@FunctionalInterface
public interface PacketWriteFunc<T>
{
    /**
     * Abstract function to write to a {@link FriendlyByteBuf}
     *
     * @param buf A {@link FriendlyByteBuf} to be written to
     * @param obj A {@code T} to be written into a {@code buf}
     */
    void write (FriendlyByteBuf buf, T obj);
}
