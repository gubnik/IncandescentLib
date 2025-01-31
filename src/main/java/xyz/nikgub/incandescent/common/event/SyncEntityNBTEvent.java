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
    /**
     * {@link CompoundTag} sent by server.
     * This is likely but not guaranteed to be equal to server-side call to {@link Entity#getPersistentData()}
     */
    private final CompoundTag serverNbt;

    /**
     * Whether the entity is already synced or not.
     * Under normal circumstances, it will return {@code false} until
     * explicitly changed by {@link #setDoSync(boolean)}
     */
    private boolean doSync = false;

    public SyncEntityNBTEvent (Entity entity, CompoundTag serverNbt)
    {
        super(entity);
        this.serverNbt = serverNbt;
    }

    /**
     * Getter for the data provided by server call
     *
     * @return {@link CompoundTag} sent by server
     */
    public CompoundTag getServerNbt ()
    {
        return serverNbt;
    }

    /**
     * Getter for whether the entity is already synced or not.
     * Under normal circumstances, it will return {@code false} until
     * explicitly changed by {@link #setDoSync(boolean)}
     *
     * @return {@code boolean} whether the NBT should be synced or not
     */
    public boolean doSync ()
    {
        return doSync;
    }

    /**
     * Sets whether the entity is already synced or not.
     *
     * @param v {@code boolean} whether the NBT should be synced or not
     */
    public void setDoSync (boolean v)
    {
        doSync = v;
    }
}
