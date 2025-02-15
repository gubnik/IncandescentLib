# Autogen Networking

Incandescent Lib provides an API to reduce boilerplate of registering Minecraft's networking utilizing Java's reflection
capabilities, specifically by the use of `@IncandescentPacket` annotation to automatically register and generate
packets.

## How to use

```java
/**
 * Registers a packet for a mod "modid" automatically, with autogenerated encoder and decoder.
 * Fields are made non-final to provide a less strict requirement for a factory method or a constructor,
 * since the autogen decoder <b>REQUIRES</b> a default constructor to be available.
 */
@IncandescentPacket(value = "modid", direction = NetworkDirection.PLAY_TO_CLIENT)
public class ExamplePacket
{
    @IncandescentPacket.Value // Registers the field "numberOfPotatoes" to be automatically encoded/decoded
    private Integer numberOfPotatoes;

    @IncandescentPacket.Value  // Registers the field "essenceOfPotatoes" to be automatically encoded/decoded
    private CompoundTag essenceOfPotatoes;

    /**
     * Factory method replacing a constructor solely for styling purposes.
     * A regular constructor would work as well, but you will have to make sure that the default constructor,
     * e.g. {@code new ExamplePacket()} is available. 
     */
    public ExamplePacket create (Integer numberOfPotatoes, CompoundTag essenceOfPotatoes)
    {
        ExamplePacket packet = new ExamplePacket();
        packet.numberOfPotatoes = numberOfPotatoes;
        packet.essenceOfPotatoes = essenceOfPotatoes;
        return packet;
    }

    @IncandescentPacket.Handler // Registers this method to be recognized as a handler
    public boolean handler (Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() ->
        {
            // does work on the client side
            Incandescent.LOGGER.info("Received {} potatoes, with essence {}", numberOfPotatoes, essenceOfPotatoes);
        });
    }
}
```

## Packet specification

A *packet*, as per Minecraft's own `Packet` interface, must contain:

- Encoder method, to encode the fields of the packet into a `FriendlyByteBuf`:
   ```java
   void encoder (FriendlyByteBuf buffer) { /* ... */ }
   ```
- Decoder method, to decode the fields of the packet from a `FriendlyByteBuf`, this is commonly a constructor:
   ```java
   PacketClass decoder (FriendlyByteBuf buffer) { /* ... */ }
   ```
- Handler method, that executes code on the packet-receiving side:
   ```java
   boolean handler (Supplier<NetworkEvent.Context> contextSupplier) { /* ... */ }
   ```

## Packet collection

During Forge Mod Loader common setup stage before anything else, `IncandescentNetwork.registerCores()` fetches the
information about classes present within every loaded mod, collecting the ones that are annotated with
`@IncandescentPacket`. These classes are recognized as *packets*, and will later be processed by
`IncandescentNetworkCore` internal logic.

## Network Cores

Network core represents a channel used in a conventional way of registering the packets, but with a provided layer
of abstraction to reduce the boilerplate code.
Once the packets were collected, they are assigned to their respective mod IDs in a network core.
The core then will attempt to search for methods fulfilling packet specification,
these methods should be annotated with:

- `@IncandescentPacket.Value` for a packet data field
- `@IncandescentPacket.Encoder` for the encoder
- `@IncandescentPacket.Decoder` for the decoder
- `@IncandescentPacket.Handler` for the handler

If any of the annotated methods are malformed or declared not public, an `IllformedPacketException` will be thrown,
stopping the loading process entirely with a descriptive error message.
If either the encoder or decoder is missing, they will be *autogenerated*.

## Automatic packet generation

Once an encoder/decoder is recognized as missing, the core will fallback to the autogenerated methods, generated within
`IncandescentNetworkCore.NetworkFunctionGenerators`.
The fields to be encoded are collected from the fields annotated by `@IncandescentPacket.Value`, and the generation
process follows these steps:

1. Check the cache for an existing encoder/decoder. To avoid repeated encoder creation calls for the same packet,
   the encoder is being cached once it was first created or found, allowing to subvert the majority of Java reflection
   overhead in the process.

2. Collect the annotated fields. Fields are being collected by the property of being annotated with
   `@IncandescentPacket.Value`; such fields are known to carry data that must be transferred via the packet, and will
   be provided encoding/decoding into the buffer.

3. Check the reader/writer function cache for each collected field. Similar to entire encoder/decoder functions,
   reader/writer functions are cached by their fields to avoid a substantial par of reflection's overhead. This,
   however,
   is less of a benefit, since the generator will ideally only run once.

4. Create the lambda of the encoder/decoder:
    - Unlock the fields to be temporary public.
    - Read/write the data from/to the buffer using reader/writer function.
    - Return the fields into the original condition

5. Return the lambda as the encoder/decoder to be cached and used for the packet.

## Drawbacks

1. Due to the nature of Java reflection, the entire autogen is a rather slow and heavy process. For larger packets
   it's generally recommended to manually provide encoder and decoder instead of relying on autogen.

2. It is likely, but **NOT** guaranteed for the collected fields to maintain original declaration order.
   If the order is broken asymmetrically between encoder and decoder, this will lead
   to broken data being extracted from the buffer. However, this **WILL NOT** cause the game to crash, since the total
   size of the data written/read remains the same.

3. Network cores are stored statically in a hash map; while this is highly optimal for access operations,
   if anything were to happen to the map, network cores will be lost and likely removed from the runtime. This will
   most likely **NOT HAPPEN**, but may happen, so keep this in mind.

4. Reader/writer functions are declared in `PacketIO`, being mapped to the types they read/write. This mapping was
   done manually, and only concerns types that have defined reader and writer methods of `FriendlyByteBuffer` that
   adhere
   to the following signatures:
   ```java
   @FunctionalInterface
   public interface WriteFunc<T>
   {
       void write (FriendlyByteBuf buf, T obj);
   }
   
   @FunctionalInterface
   public interface ReadFunc<T>
   {
       T read (FriendlyByteBuf buf);
   }
   ```
   For the majority of types this is enough but there are use cases that are not covered by such implementation,
   such as types for which the reader or writer has non-standard parameters or a non-standard return type;
   for the types that have more than one variant of being encoded, like `Double` and `Float`, the most primitive
   and likely-to-be-used version was chosen.

## Exceptions

- `FaultyPacketLoadException` - unchecked, thrown by `IncandescentNetwork.registerCores()` either when the class was
  failed
  to be accessed by the collector method, or the class cannot be read, or the packet mysteriously cannot be interpreted
  as such via casting.

- `IllformedPacketException` - unchecked, thrown by methods of `IncandescentNetworkCore`. Represent general errors in
  packet's
  structure, such as wrong method signatures, missing handler method or aforementioned cases otherwise covered by
  `FaultyPacketLoadException`.