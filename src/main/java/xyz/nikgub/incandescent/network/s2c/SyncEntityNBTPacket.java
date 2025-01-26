package xyz.nikgub.incandescent.network.s2c;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.network.IncandescentPacket;

import java.util.function.Supplier;

/**
 * Packet class to sync entity's NBT between server and client
 * Sent from {@link xyz.nikgub.incandescent.mixin.EntityMixin}
 *
 * @see xyz.nikgub.incandescent.event.SyncEntityNBTEvent
 */
@IncandescentPacket(value = Incandescent.MOD_ID, direction = NetworkDirection.PLAY_TO_CLIENT)
public class SyncEntityNBTPacket
{
    @IncandescentPacket.Value
    private CompoundTag serverNbt;

    @IncandescentPacket.Value
    private Integer entityId;

    public static SyncEntityNBTPacket create (Integer entityId, CompoundTag serverNbt)
    {
        SyncEntityNBTPacket packet = new SyncEntityNBTPacket();
        packet.entityId = entityId;
        packet.serverNbt = serverNbt;
        return packet;
    }

    @IncandescentPacket.Handler
    public boolean handler (Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            final Minecraft instance = Minecraft.getInstance();
            assert instance.level != null;
            Entity entity = instance.level.getEntity(entityId);
            assert entity != null;
            entity.getPersistentData().merge(serverNbt);
        });
        return true;
    }
}
