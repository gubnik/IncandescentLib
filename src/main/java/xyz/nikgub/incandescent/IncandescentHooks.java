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
