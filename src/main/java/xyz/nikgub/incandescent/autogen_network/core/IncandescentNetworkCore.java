/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2025, nikgub_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.nikgub.incandescent.autogen_network.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.autogen_network.IncandescentPacket;
import xyz.nikgub.incandescent.autogen_network.exception.MalformedPacketException;
import xyz.nikgub.incandescent.autogen_network.interfaces.DecoderFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.EncoderFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.HandlerFunc;
import xyz.nikgub.incandescent.util.CacheMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Abstraction over SimpleChannel to automate packet registering process
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
public class IncandescentNetworkCore
{
    /**
     * Actual channel through which the packets are sent;
     */
    private final SimpleChannel channelInstance;

    /**
     * Function generator used for this core instance.
     */
    private final NetworkFunctionGenerator generator;

    /**
     * Cache for a decoding function of a packet class.
     * Is being collected in {@link #getDecoder(Class)}
     */
    private final Map<Class<?>, DecoderFunc<?>> DECODER_CACHE;

    /**
     * Cache for an encoding function of a packet class.
     * Is being collected in {@link #getEncoder(Class)}
     */
    private final Map<Class<?>, EncoderFunc<?>> ENCODER_CACHE;

    /**
     * Cache for a handling function of a packet class.
     * Is being collected in {@link #getHandler(Class)}
     */
    private final Map<Class<?>, HandlerFunc<?>> HANDLER_CACHE;

    /**
     * ID of the last packet signed. Doubles as total packet count.
     */
    private int lastPacket = 0;

    public static IncandescentNetworkCore simple (String modId)
    {
        return new IncandescentNetworkCore(modId, 8);
    }

    public static IncandescentNetworkCore withCacheSize (String modId, int maxCacheSize)
    {
        return new IncandescentNetworkCore(modId, maxCacheSize);
    }

    private IncandescentNetworkCore (String modId, int maxCacheSize)
    {
        this.channelInstance = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(modId, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();
        this.generator = new NetworkFunctionGenerator();
        this.DECODER_CACHE = new CacheMap<>(maxCacheSize);
        this.ENCODER_CACHE = new CacheMap<>(maxCacheSize);
        this.HANDLER_CACHE = new CacheMap<>(maxCacheSize);
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
            .decoder(this.getDecoder(clazz)::decode)
            .encoder(this.getEncoder(clazz)::encode)
            .consumerMainThread(this.getHandler(clazz)::handle)
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
     * @see NetworkFunctionGenerator#generateDecoder(Class)
     */
    public <T> DecoderFunc<T> getDecoder (Class<T> clazz)
    {
        if (this.DECODER_CACHE.get(clazz) != null)
        {
            return (DecoderFunc<T>) this.DECODER_CACHE.get(clazz);
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
                    throw new MalformedPacketException("FriendlyByteBuffer constructor is not accessible within packet class " + clazz);
                }
            };
            this.DECODER_CACHE.putIfAbsent(clazz, decoderFunc);
            return decoderFunc;
        } catch (NoSuchMethodException e)
        {
            Incandescent.LOGGER.warn("[{}] DECODER NOT PROVIDED, FALLBACK TO DEFAULT", clazz.getName());
            DecoderFunc<T> decoderFunc = this.generator.generateDecoder(clazz);
            this.DECODER_CACHE.putIfAbsent(clazz, decoderFunc);
            return decoderFunc;
        }
    }

    /**
     * Function that searches for an encoder in a packet class.
     * An encoder is to be marked with {@link IncandescentPacket.Encoder}
     * , otherwise it will not be found.
     * If not found, the function will attempt to run a generator
     * for a general reflection-powered encoder.
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link EncoderFunc} of a packet class
     * @see #getEncoderMethod(Class)
     * @see NetworkFunctionGenerator#generateEncoder(Class)
     */
    public <T> EncoderFunc<T> getEncoder (Class<T> clazz)
    {
        if (this.ENCODER_CACHE.get(clazz) != null)
        {
            return (EncoderFunc<T>) this.ENCODER_CACHE.get(clazz);
        }
        final Method encoderMethod = getEncoderMethod(clazz);
        if (encoderMethod == null)
        {
            Incandescent.LOGGER.warn("[{}] ENCODER NOT PROVIDED, FALLBACK TO DEFAULT", clazz.getName());
            EncoderFunc<T> encoderFunc = this.generator.generateEncoder(clazz);
            this.ENCODER_CACHE.putIfAbsent(clazz, encoderFunc);
            return encoderFunc;
        }
        EncoderFunc<T> encoderFunc = (t, buf) ->
        {
            try
            {
                encoderMethod.invoke(t, buf);
            } catch (IllegalAccessException e)
            {
                throw new MalformedPacketException("Encoder method is not accessible within packet class " + clazz);
            } catch (InvocationTargetException e)
            {
                throw new MalformedPacketException("Encoder method is not invocable within packet class " + clazz);
            }
        };
        this.ENCODER_CACHE.putIfAbsent(clazz, encoderFunc);
        return encoderFunc;
    }

    /**
     * Function that searches for a handler in a packet class.
     * An encoder is to be marked with {@link IncandescentPacket.Handler}
     * , otherwise it will not be found.
     * If not found, the function will throw an error with no fallback provided,
     * since the handler function is irreplaceable
     *
     * @param clazz {@link IncandescentPacket} class
     * @param <T>   Packet type
     * @return {@link HandlerFunc} of a packet class
     * @see #getHandlerMethod(Class)
     */
    public <T> HandlerFunc<T> getHandler (Class<T> clazz)
    {
        if (this.HANDLER_CACHE.get(clazz) != null)
        {
            return (HandlerFunc<T>) this.HANDLER_CACHE.get(clazz);
        }
        final Method handlerMethod = getHandlerMethod(clazz);
        HandlerFunc<T> handlerFunc = (t, sup) ->
        {
            try
            {
                handlerMethod.invoke(t, sup);
            } catch (IllegalAccessException e)
            {
                throw new MalformedPacketException("Handler method is not accessible within packet class " + clazz);
            } catch (InvocationTargetException e)
            {
                throw new MalformedPacketException("Handler method is not invocable within packet class " + clazz);
            }
        };
        this.HANDLER_CACHE.putIfAbsent(clazz, handlerFunc);
        return handlerFunc;
    }

    /**
     * Gathers an encoder method from packet {@code clazz} using reflection.
     * If no such method is present, the {@link NetworkFunctionGenerator#generateEncoder(Class)} will be used instead.
     * If multiple of such method are present, the {@link MalformedPacketException} will be thrown.
     * If such a method is present but is illformed, the {@link MalformedPacketException} will be thrown.
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
                throw new MalformedPacketException("Encoder method is not unique within packet class " + clazz);
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
            throw new MalformedPacketException("Encoder method is present within packet class " + clazz + " but is illformed");
        }
        return encoder;
    }

    /**
     * Gathers a handler method from packet {@code clazz} using reflection.
     * If no such method is present, the {@link MalformedPacketException} will be thrown.
     * If multiple of such method are present, the {@link MalformedPacketException} will be thrown.
     * If such a method is present but is illformed, the {@link MalformedPacketException} will be thrown.
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
                throw new MalformedPacketException("Handler method is not unique within packet class " + clazz);
            }
            handler = method;
        }
        if (handler == null)
        {
            throw new MalformedPacketException("Handler method is not present within packet class " + clazz);
        }
        if (handler.getReturnType() != boolean.class || handler.getParameterCount() != 1
            || !(handler.getParameters()[0].getParameterizedType() instanceof ParameterizedType type && type.getRawType() == Supplier.class
            && type.getActualTypeArguments().length == 1 && type.getActualTypeArguments()[0] == NetworkEvent.Context.class))
        {
            throw new MalformedPacketException("Handler method is present within packet class " + clazz + " but is illformed");
        }
        return handler;
    }
}
