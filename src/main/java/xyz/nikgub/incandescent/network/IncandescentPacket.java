package xyz.nikgub.incandescent.network;

import net.minecraftforge.network.NetworkDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically register a network packet
 * <p>
 * Classes marked with this annotation are not needed to be registered explicitly,
 * and their methods are automatically collected.
 * <p>
 * Decoder method must be a constructor that takes in {@link net.minecraft.network.FriendlyByteBuf}
 * <p>
 * If no decoder or encoder is present, they will be created from fields marked with
 * {@link Value}. However, if you intend on falling back on autogenerated decoder,
 * you must ensure that there is an available default constructor.
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IncandescentPacket
{
    /**
     * Mod ID of the mod this packet is attached to
     * @return Mod ID
     */
    String value ();

    NetworkDirection direction ();

    /**
     * Annotation to mark fields that contain data being sent
     * <p>
     * Fields marked with this annotation are used to automatically
     * build decoder and encoder, should there be none predefined
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Value
    {
        int value() default Integer.MAX_VALUE;
    }

    /**
     * Annotation to mark packet's encoder method
     * <p>
     * Methods marked with this annotation are not used to process
     * encode data into packets to be sent
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Encoder
    {
    }

    /**
     * Annotation to mark packet's handler method
     * <p>
     * Methods marked with this annotation are not used to process
     * their packets once received by the other side
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Handler
    {
    }
}
