package xyz.nikgub.incandescent.autogen_network.interfaces;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Functional interface for packet handling function
 *
 * @param <T> Packet type
 */
@FunctionalInterface
public interface HandlerFunc<T>
{
    void handle (T obj, Supplier<NetworkEvent.Context> supplier);
}
