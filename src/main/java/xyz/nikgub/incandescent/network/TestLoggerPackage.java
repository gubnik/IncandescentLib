package xyz.nikgub.incandescent.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;
import xyz.nikgub.incandescent.Incandescent;

import java.util.function.Supplier;

/**
 * Dummy packet class to showcase the abilities of {@link IncandescentNetwork}.
 * Decoder and encoder were removed, and so {@link xyz.nikgub.incandescent.network.IncandescentNetworkCore}
 * will generate general ones from reflection, using fields marked with {@link xyz.nikgub.incandescent.network.IncandescentPacket.Value}
 */
//@IncandescentPacket(value = Incandescent.MOD_ID, direction = NetworkDirection.PLAY_TO_CLIENT)
public class TestLoggerPackage
{
    @IncandescentPacket.Value
    private Integer fieldOne;

    @IncandescentPacket.Value
    private Double fieldTwo;

    @IncandescentPacket.Value
    private CompoundTag fieldThree;

    public static TestLoggerPackage create (int a, double b, CompoundTag nbt)
    {
        TestLoggerPackage n = new TestLoggerPackage();
        n.fieldOne = a;
        n.fieldTwo = b;
        n.fieldThree = nbt;
        return n;
    }

    @IncandescentPacket.Handler
    public boolean handler (Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            Incandescent.LOGGER.info("Hello from some thread {} {} {}", fieldOne, fieldTwo, fieldThree);
        });
        return true;
    }
}
