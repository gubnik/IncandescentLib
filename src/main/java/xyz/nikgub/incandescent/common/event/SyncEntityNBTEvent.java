package xyz.nikgub.incandescent.common.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * This event is fired whenever an entity's NBT is being requested
 * via {@link Entity#getPersistentData()}
 */
public class SyncEntityNBTEvent extends EntityEvent
{
    private final CompoundTag serverNbt;
    private boolean doSync = false;

    public SyncEntityNBTEvent (Entity entity, CompoundTag serverNbt)
    {
        super(entity);
        this.serverNbt = serverNbt;
    }

    public CompoundTag getServerNbt ()
    {
        return serverNbt;
    }

    public boolean doSync ()
    {
        return doSync;
    }

    public void setDoSync (boolean v)
    {
        doSync = v;
    }
}
