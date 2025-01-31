package xyz.nikgub.incandescent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import xyz.nikgub.incandescent.common.event.DefineSyncedEntityDataEvent;
import xyz.nikgub.incandescent.common.event.SyncEntityNBTEvent;

public class IncandescentHooks
{
    public static SyncEntityNBTEvent syncEntityNbtEvent (Entity entity, CompoundTag serverNbt)
    {
        SyncEntityNBTEvent event = new SyncEntityNBTEvent(entity, serverNbt);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static DefineSyncedEntityDataEvent defineSyncedEntityDataEvent (Entity entity)
    {
        DefineSyncedEntityDataEvent event = new DefineSyncedEntityDataEvent(entity);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
