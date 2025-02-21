package xyz.nikgub.incandescent.autogen_network.core;

import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.autogen_network.IncandescentNetworkAPI;
import xyz.nikgub.incandescent.autogen_network.IncandescentPacket;
import xyz.nikgub.incandescent.autogen_network.exception.MalformedPacketException;
import xyz.nikgub.incandescent.autogen_network.interfaces.DecoderFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.EncoderFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.PacketReadFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.PacketWriteFunc;
import xyz.nikgub.incandescent.util.CacheMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Static class that houses generator functions and their caches.
 */
class NetworkFunctionGenerator
{
    /**
     * Cache for a reading function appropriate for a field.
     * Is being collected in {@link #generateDecoder(Class)}
     */
    private final Map<Field, PacketReadFunc<?>> READER_CACHE;

    /**
     * Cache for a writing function appropriate for a field.
     * Is being collected in {@link #generateEncoder(Class)}
     */
    private final Map<Field, PacketWriteFunc<?>> WRITER_CACHE;

    public NetworkFunctionGenerator ()
    {
        READER_CACHE = new CacheMap<>(32);
        WRITER_CACHE = new CacheMap<>(32);
    }

    /**
     * Generator for a reflection-based decoder acting as a fallback.
     * It runs on {@link IncandescentPacket.Value} fields
     * that are being used for automated reader matching.
     * Readers are gathered from {@link PacketIOMapping#bufRead(Class)},
     * and should it not contain a reader for a field's class, this
     * function will throw a {@link MalformedPacketException}.
     * Intermediate mappings of reader functions are stored in {@link NetworkFunctionGenerator#READER_CACHE}
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return Generated {@link DecoderFunc}
     * @see PacketIOMapping#bufRead(Class)
     */
    public <T> DecoderFunc<T> generateDecoder (Class<T> clazz)
    {
        final List<Field> fields = IncandescentNetworkAPI.getAnnotatedMethods(clazz);
        for (var field : fields)
        {
            if (READER_CACHE.get(field) != null)
            {
                continue;
            }
            PacketReadFunc<?> readFunc = PacketIOMapping.bufRead(field.getType());
            if (readFunc == null)
            {
                throw new MalformedPacketException("Cannot decode " + field.getType().getName() + " because no such reader exists");
            }
            READER_CACHE.putIfAbsent(field, readFunc);
        }
        return (buf) ->
        {
            T instance = instantiatePacket(clazz);
            for (var field : fields)
            {
                boolean wasPrivate = Modifier.isPrivate(field.getModifiers());
                if (wasPrivate)
                {
                    field.setAccessible(true);
                }
                PacketReadFunc<Object> readFunc = (PacketReadFunc<Object>) READER_CACHE.get(field);
                if (readFunc == null)
                {
                    throw new MalformedPacketException("Cannot decode " + field.getType().getName() + " because no such reader exists");
                }
                try
                {
                    field.set(instance, readFunc.read(buf));
                } catch (IllegalAccessException e)
                {
                    throw new MalformedPacketException("Cannot decode " + field.getType().getName() + " because the access was denied");
                } finally
                {
                    if (wasPrivate)
                    {
                        field.setAccessible(false);
                    }
                }
            }
            return instance;
        };
    }

    /**
     * Generator for a reflection-based encoder acting as a fallback.
     * It runs on {@link IncandescentPacket.Value} fields
     * that are being used for automated writer matching.
     * Readers are gathered from {@link PacketIOMapping#bufWrite(Class)},
     * and should it not contain a writer for a field's class, this
     * function will throw a {@link MalformedPacketException}.
     * Intermediate mappings of writer functions are stored in {@link NetworkFunctionGenerator#WRITER_CACHE}
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return Generated {@link EncoderFunc}
     * @see PacketIOMapping#bufWrite(Class)
     */
    public <T> EncoderFunc<T> generateEncoder (Class<T> clazz)
    {
        final List<Field> fields = IncandescentNetworkAPI.getAnnotatedMethods(clazz);
        for (var field : fields)
        {
            if (WRITER_CACHE.get(field) != null)
            {
                continue;
            }
            PacketWriteFunc<?> writeFunc = PacketIOMapping.bufWrite(field.getType());
            if (writeFunc == null)
            {
                throw new MalformedPacketException("Cannot encode " + field.getType().getName() + " because no such writer exists");
            }
            WRITER_CACHE.putIfAbsent(field, writeFunc);
        }
        return (t, buf) ->
        {
            for (var field : fields)
            {
                boolean wasPrivate = Modifier.isPrivate(field.getModifiers());
                if (wasPrivate)
                {
                    field.setAccessible(true);
                }
                PacketWriteFunc<Object> writeFunc = (PacketWriteFunc<Object>) WRITER_CACHE.get(field);
                if (writeFunc == null)
                {
                    throw new MalformedPacketException("Cannot encode " + field.getType().getName() + " because no such writer exists");
                }
                try
                {
                    writeFunc.write(buf, field.get(t));
                } catch (IllegalAccessException e)
                {
                    throw new MalformedPacketException("Cannot encode " + field.getType().getName() + " because the access was denied");
                } finally
                {
                    if (wasPrivate)
                    {
                        field.setAccessible(false);
                    }
                }
            }
        };
    }

    /**
     * Intermediate function that instantiates a default packet used in {@link #generateDecoder(Class)}.
     * For this exact purpose, the packet class should either have one accessible, or define a proper decoder.
     * If no such constructor is present, the {@link MalformedPacketException} will be thrown
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return Instance of {@code T} packet
     */
    private static <T> @NotNull T instantiatePacket (Class<T> clazz)
    {
        final Constructor<?>[] constructors = clazz.getConstructors();
        if (Arrays.stream(constructors).noneMatch(constructor -> constructor.getParameterCount() == 0))
        {
            throw new MalformedPacketException("Cannot decode " + clazz.getName() + " because there is no accessible default no-args constructor");
        }
        Constructor<T> constructor;
        try
        {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        T instance;
        try
        {
            instance = constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            throw new MalformedPacketException("Cannot decode " + clazz.getName() + " because there is no accessible default no-args constructor");
        }
        return instance;
    }
}
