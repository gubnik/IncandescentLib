package xyz.nikgub.incandescent.autogen_network.interfaces;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Functional interface for packet decoding function
 *
 * @param <T> Packet type
 */
@FunctionalInterface
public interface DecoderFunc<T>
{
    T decode (FriendlyByteBuf buf);
}
