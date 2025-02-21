package xyz.nikgub.incandescent.autogen_network.interfaces;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Functional interface for packet encoding function
 *
 * @param <T> Packet type
 */
@FunctionalInterface
public interface EncoderFunc<T>
{
    void encode (T obj, FriendlyByteBuf buf);
}
