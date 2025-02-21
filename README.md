# Incandescent Lib
A light-weight library providing vast API for reducing boilerplate of common Forge code, along with
providing general utility, convenient data animation format, numerous interfaces, etc., etc.

## How to use
To use Incandescent Lib for your project, include the following line in your `build.gradle`:
```groovy
repositories {
    maven {
        url "https://cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}
dependancies {
    implementation fg.deobf("curse.maven:incandescent_lib-971520:${incandescent_lib_artifact_version}")
}
```

# Features

## [Autogen Networking](src/main/java/xyz/nikgub/incandescent/autogen_network/README.md)
Allows automated registration and assembly of packets from but a few annotations, using Java's Reflection API
### Example:
```java
@IncandescentPacket(value = "modid", direction = NetworkDirection.PLAY_TO_CLIENT)
public class ExamplePacket
{
    @IncandescentPacket.Value
    private Integer numberOfPotatoes;

    @IncandescentPacket.Value
    private CompoundTag essenceOfPotatoes;

    @IncandescentPacket.Handler
    public boolean handler (Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() ->
        {
            Incandescent.LOGGER.info("Received {} potatoes, with essence {}", numberOfPotatoes, essenceOfPotatoes);
        });
    }
}
```

## [Pyranim format](src/main/java/xyz/nikgub/incandescent/pyranim/README.md)
File format to easily write and parse entity animations in a data-driven manner. This format uses a simple
Assembly-style syntax and allows for user-defined interpolations.
### Example:
```pyranim
.drtion 1.0  
.doloop

left_arm:
    >attime 0
    >intrpl "catmullrom"
    mov 0, 1, 0
    rot 2, 3, 4
    scl 5, 6, 7
    >attime 0.4
    >intrpl "linear"
    mov 1, 2, 3
    rot 5, 6, 7
    scl 6, 5, 4
head:
    >attime 0
    >intrpl "linear"
    mov 0, 1, 0
    rot 45, 45, 45
    scl 1, 2, 1
```

## [Item interfaces](src/main/java/xyz/nikgub/incandescent/item_interfaces/README.md)
Incandescent Lib provides a set of interfaces for Minecraft's items, allowing to extend
their functionality beyond regular Forge capabilities without falling back on Mixins.