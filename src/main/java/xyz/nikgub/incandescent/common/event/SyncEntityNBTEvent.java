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
