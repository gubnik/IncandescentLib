package xyz.nikgub.incandescent.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.common.util.CacheMap;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Abstraction over SimpleChannel to automate packet registering process
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
public class IncandescentNetworkCore
{
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

    /**
     * ID of the last packet signed. Doubles as total packet count.
     */
    private int lastPacket = 0;

    /**
     * Actual channel through which the packets are sent;
     */
    private final SimpleChannel channelInstance;

    /**
     * Cache for a decoding function of a packet class.
     * Is being collected in {@link #getDecoder(Class)}
     */
    private static final Map<Class<?>, DecoderFunc<?>> DECODER_CACHE = new CacheMap<>(32);

    /**
     * Cache for an encoding function of a packet class.
     * Is being collected in {@link #getEncoder(Class)}
     */
    private static final Map<Class<?>, EncoderFunc<?>> ENCODER_CACHE = new CacheMap<>(32);

    /**
     * Cache for a handling function of a packet class.
     * Is being collected in {@link #getHandler(Class)}
     */
    private static final Map<Class<?>, HandlerFunc<?>> HANDLER_CACHE = new CacheMap<>(32);

    public IncandescentNetworkCore (String modId)
    {
        this.channelInstance = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(modId, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();
    }

    /**
     * Signs the packet to the channel.
     * From here, other functions are invoked
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     */
    public <T> void sign (Class<T> clazz)
    {
        IncandescentPacket packet = clazz.getAnnotation(IncandescentPacket.class);
        this.channelInstance.messageBuilder(clazz, lastPacket++, packet.direction())
            .decoder(getDecoder(clazz)::decode)
            .encoder(getEncoder(clazz)::encode)
            .consumerMainThread(getHandler(clazz)::handle)
            .add();
    }

    /**
     * Getter for {@link #channelInstance}
     *
     * @return {@link #channelInstance}
     */
    public SimpleChannel getChannelInstance ()
    {
        return channelInstance;
    }

    /**
     * Function that searches for a decoder in a packet class.
     * A decoder is assumed to be a constructor taking {@link FriendlyByteBuf}
     * as an argument.
     * If not found, the function will attempt to run a generator
     * for a general reflection-powered decoder.
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link DecoderFunc} of a packet class
     * @see NetworkFunctionGenerators#generateDecoder(Class)
     */
    public static <T> DecoderFunc<T> getDecoder (Class<T> clazz)
    {
        if (DECODER_CACHE.get(clazz) != null)
        {
            return (DecoderFunc<T>) DECODER_CACHE.get(clazz);
        }
        Constructor<T> constructor;
        try
        {
            constructor = clazz.getConstructor(FriendlyByteBuf.class);
            DecoderFunc<T> decoderFunc = (buf) ->
            {
                try
                {
                    return constructor.newInstance(buf);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    throw new IllformedPacketException("FriendlyByteBuffer constructor is not accessible within packet class " + clazz);
                }
            };
            DECODER_CACHE.putIfAbsent(clazz, decoderFunc);
            return decoderFunc;
        } catch (NoSuchMethodException e)
        {
            Incandescent.LOGGER.warn("[{}] DECODER NOT PROVIDED, FALLBACK TO DEFAULT", clazz.getName());
            DecoderFunc<T> decoderFunc = NetworkFunctionGenerators.generateDecoder(clazz);
            DECODER_CACHE.putIfAbsent(clazz, decoderFunc);
            return decoderFunc;
        }
    }

    /**
     * Function that searches for an encoder in a packet class.
     * An encoder is to be marked with {@link xyz.nikgub.incandescent.network.IncandescentPacket.Encoder}
     * , otherwise it will not be found.
     * If not found, the function will attempt to run a generator
     * for a general reflection-powered encoder.
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link EncoderFunc} of a packet class
     * @see NetworkFunctionGenerators#getEncoderMethod(Class)
     * @see NetworkFunctionGenerators#generateEncoder(Class)
     */
    public static <T> EncoderFunc<T> getEncoder (Class<T> clazz)
    {
        if (ENCODER_CACHE.get(clazz) != null)
        {
            return (EncoderFunc<T>) ENCODER_CACHE.get(clazz);
        }
        final Method encoderMethod = getEncoderMethod(clazz);
        if (encoderMethod == null)
        {
            Incandescent.LOGGER.warn("[{}] ENCODER NOT PROVIDED, FALLBACK TO DEFAULT", clazz.getName());
            EncoderFunc<T> encoderFunc = NetworkFunctionGenerators.generateEncoder(clazz);
            ENCODER_CACHE.putIfAbsent(clazz, encoderFunc);
            return encoderFunc;
        }
        EncoderFunc<T> encoderFunc = (t, buf) ->
        {
            try
            {
                encoderMethod.invoke(t, buf);
            } catch (IllegalAccessException e)
            {
                throw new IllformedPacketException("Encoder method is not accessible within packet class " + clazz);
            } catch (InvocationTargetException e)
            {
                throw new IllformedPacketException("Encoder method is not invocable within packet class " + clazz);
            }
        };
        ENCODER_CACHE.putIfAbsent(clazz, encoderFunc);
        return encoderFunc;
    }

    /**
     * Function that searches for a handler in a packet class.
     * An encoder is to be marked with {@link xyz.nikgub.incandescent.network.IncandescentPacket.Handler}
     * , otherwise it will not be found.
     * If not found, the function will throw an error with no fallback provided,
     * since the handler function is irreplaceable
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link HandlerFunc} of a packet class
     * @see NetworkFunctionGenerators#getHandlerMethod(Class)
     */
    public static <T> HandlerFunc<T> getHandler (Class<T> clazz)
    {
        if (HANDLER_CACHE.get(clazz) != null)
        {
            return (HandlerFunc<T>) HANDLER_CACHE.get(clazz);
        }
        final Method handlerMethod = getHandlerMethod(clazz);
        HandlerFunc<T> handlerFunc = (t, sup) ->
        {
            try
            {
                handlerMethod.invoke(t, sup);
            } catch (IllegalAccessException e)
            {
                throw new IllformedPacketException("Handler method is not accessible within packet class " + clazz);
            } catch (InvocationTargetException e)
            {
                throw new IllformedPacketException("Handler method is not invocable within packet class " + clazz);
            }
        };
        HANDLER_CACHE.putIfAbsent(clazz, handlerFunc);
        return handlerFunc;
    }

    /**
     * Gathers an encoder method from packet {@code clazz} using reflection.
     * If no such method is present, the {@link NetworkFunctionGenerators#generateEncoder(Class)} will be used instead.
     * If multiple of such method are present, the {@link IllformedPacketException} will be thrown.
     * If such a method is present but is illformed, the {@link IllformedPacketException} will be thrown.
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link Method} that is the encoder of {@code T} packet, or {@code null} if it does not exist
     */
    private static <T> @Nullable Method getEncoderMethod (Class<T> clazz)
    {
        Method[] methods = clazz.getMethods();
        Method encoder = null;
        for (var method : methods)
        {
            if (!method.isAnnotationPresent(IncandescentPacket.Encoder.class))
            {
                continue;
            }
            if (encoder != null)
            {
                throw new IllformedPacketException("Encoder method is not unique within packet class " + clazz);
            }
            encoder = method;
        }
        if (encoder == null)
        {
            return null;
        }
        if (encoder.getReturnType() != void.class || encoder.getParameterCount() != 1
            || encoder.getParameters()[0].getType() != FriendlyByteBuf.class)
        {
            throw new IllformedPacketException("Encoder method is present within packet class " + clazz + " but is illformed");
        }
        return encoder;
    }

    /**
     * Gathers a handler method from packet {@code clazz} using reflection.
     * If no such method is present, the {@link IllformedPacketException} will be thrown.
     * If multiple of such method are present, the {@link IllformedPacketException} will be thrown.
     * If such a method is present but is illformed, the {@link IllformedPacketException} will be thrown.
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link Method} that is the handler of {@code T} packet
     */
    private static <T> @NotNull Method getHandlerMethod (Class<T> clazz)
    {
        Method[] methods = clazz.getMethods();
        Method handler = null;
        for (var method : methods)
        {
            if (!method.isAnnotationPresent(IncandescentPacket.Handler.class))
            {
                continue;
            }
            if (handler != null)
            {
                throw new IllformedPacketException("Handler method is not unique within packet class " + clazz);
            }
            handler = method;
        }
        if (handler == null)
        {
            throw new IllformedPacketException("Handler method is not present within packet class " + clazz);
        }
        if (handler.getReturnType() != boolean.class || handler.getParameterCount() != 1
            || !(handler.getParameters()[0].getParameterizedType() instanceof ParameterizedType type && type.getRawType() == Supplier.class
            && type.getActualTypeArguments().length == 1 && type.getActualTypeArguments()[0] == NetworkEvent.Context.class))
        {
            throw new IllformedPacketException("Handler method is present within packet class " + clazz + " but is illformed");
        }
        return handler;
    }

    /**
     * Collects class' fields marked with {@link xyz.nikgub.incandescent.network.IncandescentPacket.Value}
     * and sorts them according to {@link IncandescentPacket.Value#value()}
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return List of fields marked with {@link xyz.nikgub.incandescent.network.IncandescentPacket.Value}
     * @see xyz.nikgub.incandescent.network.IncandescentPacket.Value
     */
    private static <T> List<Field> getAnnotatedMethods (Class<T> clazz)
    {
        final Field[] fields = clazz.getDeclaredFields();
        final List<Field> retVal = new ArrayList<>();
        for (Field field : fields)
        {
            if (!field.isAnnotationPresent(IncandescentPacket.Value.class))
            {
                continue;
            }
            retVal.add(field);
        }
        retVal.sort(Comparator.comparingInt(field ->
        {
            IncandescentPacket.Value value = field.getAnnotation(IncandescentPacket.Value.class);
            if (value == null)
            {
                return Integer.MAX_VALUE;
            }
            return value.value();
        }));
        return retVal;
    }

    /**
     * Static class that houses generator functions and their caches.
     */
    private static class NetworkFunctionGenerators
    {
        /**
         * Cache for a reading function appropriate for a field.
         * Is being collected in {@link #generateDecoder(Class)}
         */
        private static final Map<Field, PacketIO.ReadFunc<?>> READER_CACHE = new CacheMap<>(128);

        /**
         * Cache for a writing function appropriate for a field.
         * Is being collected in {@link #generateEncoder(Class)}
         */
        private static final Map<Field, PacketIO.WriteFunc<?>> WRITER_CACHE = new CacheMap<>(128);

        /**
         * Generator for a reflection-based decoder acting as a fallback.
         * It runs on {@link xyz.nikgub.incandescent.network.IncandescentPacket.Value} fields
         * that are being used for automated reader matching.
         * Readers are gathered from {@link PacketIO#bufRead(Class)},
         * and should it not contain a reader for a field's class, this
         * function will throw a {@link IllformedPacketException}.
         * Intermediate mappings of reader functions are stored in {@link NetworkFunctionGenerators#READER_CACHE}
         *
         * @param clazz {@link IncandescentPacket} class
         * @param <T>   Packet type
         * @return Generated {@link DecoderFunc}
         * @see PacketIO#bufRead(Class)
         */
        private static <T> DecoderFunc<T> generateDecoder (Class<T> clazz)
        {
            if (DECODER_CACHE.get(clazz) != null)
            {
                return (DecoderFunc<T>) DECODER_CACHE.get(clazz);
            }
            final List<Field> fields = getAnnotatedMethods(clazz);
            for (var field : fields)
            {
                if (READER_CACHE.get(field) != null)
                {
                    continue;
                }
                PacketIO.ReadFunc<?> readFunc = PacketIO.bufRead(field.getType());
                if (readFunc == null)
                {
                    throw new IllformedPacketException("Cannot decode " + field.getType().getName() + " because no such reader exists");
                }
                READER_CACHE.put(field, readFunc);
            }
            DecoderFunc<T> decoder = (buf) ->
            {
                T instance = instantiatePacket(clazz);
                for (var field : fields)
                {
                    boolean wasPrivate = Modifier.isPrivate(field.getModifiers());
                    if (wasPrivate)
                    {
                        field.setAccessible(true);
                    }
                    PacketIO.ReadFunc<Object> readFunc = (PacketIO.ReadFunc<Object>) READER_CACHE.get(field);
                    if (readFunc == null)
                    {
                        throw new IllformedPacketException("Cannot decode " + field.getType().getName() + " because no such reader exists");
                    }
                    try
                    {
                        field.set(instance, readFunc.read(buf));
                    } catch (IllegalAccessException e)
                    {
                        throw new IllformedPacketException("Cannot decode " + field.getType().getName() + " because the access was denied");
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
            DECODER_CACHE.putIfAbsent(clazz, decoder);
            return decoder;
        }

        /**
         * Generator for a reflection-based encoder acting as a fallback.
         * It runs on {@link xyz.nikgub.incandescent.network.IncandescentPacket.Value} fields
         * that are being used for automated writer matching.
         * Readers are gathered from {@link PacketIO#bufWrite(Class)},
         * and should it not contain a writer for a field's class, this
         * function will throw a {@link IllformedPacketException}.
         * Intermediate mappings of writer functions are stored in {@link NetworkFunctionGenerators#WRITER_CACHE}
         *
         * @param clazz {@link IncandescentPacket} class
         * @param <T>   Packet type
         * @return Generated {@link EncoderFunc}
         * @see PacketIO#bufWrite(Class)
         */
        private static <T> EncoderFunc<T> generateEncoder (Class<T> clazz)
        {
            if (ENCODER_CACHE.get(clazz) != null)
            {
                return (EncoderFunc<T>) ENCODER_CACHE.get(clazz);
            }
            final List<Field> fields = getAnnotatedMethods(clazz);
            for (var field : fields)
            {
                if (WRITER_CACHE.get(field) != null)
                {
                    continue;
                }
                PacketIO.WriteFunc<?> writeFunc = PacketIO.bufWrite(field.getType());
                if (writeFunc == null)
                {
                    throw new IllformedPacketException("Cannot encode " + field.getType().getName() + " because no such writer exists");
                }
                WRITER_CACHE.put(field, writeFunc);
            }
            EncoderFunc<T> encoder = (t, buf) ->
            {
                for (var field : fields)
                {
                    boolean wasPrivate = Modifier.isPrivate(field.getModifiers());
                    if (wasPrivate)
                    {
                        field.setAccessible(true);
                    }
                    PacketIO.WriteFunc<Object> writeFunc = (PacketIO.WriteFunc<Object>) WRITER_CACHE.get(field);
                    if (writeFunc == null)
                    {
                        throw new IllformedPacketException("Cannot encode " + field.getType().getName() + " because no such writer exists");
                    }
                    try
                    {
                        writeFunc.write(buf, field.get(t));
                    } catch (IllegalAccessException e)
                    {
                        throw new IllformedPacketException("Cannot encode " + field.getType().getName() + " because the access was denied");
                    } finally
                    {
                        if (wasPrivate)
                        {
                            field.setAccessible(false);
                        }
                    }
                }
            };
            ENCODER_CACHE.putIfAbsent(clazz, encoder);
            return encoder;
        }

        /**
         * Intermediate function that instantiates a default packet used in {@link #generateDecoder(Class)}.
         * For this exact purpose, the packet class should either have one accessible, or define a proper decoder.
         * If no such constructor is present, the {@link IllformedPacketException} will be thrown
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
                throw new IllformedPacketException("Cannot decode " + clazz.getName() + " because there is no accessible default no-args constructor");
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
                throw new IllformedPacketException("Cannot decode " + clazz.getName() + " because there is no accessible default no-args constructor");
            }
            return instance;
        }
    }
}
