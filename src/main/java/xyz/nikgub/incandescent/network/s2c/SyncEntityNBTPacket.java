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

package xyz.nikgub.incandescent.network.s2c;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.autogen_network.IncandescentPacket;
import xyz.nikgub.incandescent.autogen_network.core.IncandescentNetworkCore;
import xyz.nikgub.incandescent.common.event.SyncEntityNBTEvent;

import java.util.function.Supplier;

/**
 * Packet class to sync entity's NBT between server and client.
 * Sent from {@link xyz.nikgub.incandescent.mixin.EntityMixin} if the {@code doSync}
 * value of the event was set to {@code true}.
 * Encoder and decoder are automatically generated by {@link IncandescentNetworkCore}.
 *
 * @see SyncEntityNBTEvent
 * @see xyz.nikgub.incandescent.mixin.EntityMixin
 * @see IncandescentNetworkCore
 */
@IncandescentPacket(value = Incandescent.MOD_ID, direction = NetworkDirection.PLAY_TO_CLIENT)
public class SyncEntityNBTPacket
{
    @IncandescentPacket.Value
    private CompoundTag serverNbt;

    @IncandescentPacket.Value
    private Integer entityId;

    /**
     * Factory method to construct a packet.
     *
     * @param entityId  {@code int} ID of the entity
     * @param serverNbt {@link CompoundTag} NBT collected at the moment of packet being sent,
     *                  under normal circumstances it is equal to {@link Entity#getPersistentData()}
     *                  on the server side.
     * @return Constructed instance of {@link SyncEntityNBTPacket}
     */
    public static SyncEntityNBTPacket create (Integer entityId, CompoundTag serverNbt)
    {
        SyncEntityNBTPacket packet = new SyncEntityNBTPacket();
        packet.entityId = entityId;
        packet.serverNbt = serverNbt;
        return packet;
    }

    /**
     * Handler method of the packet.
     *
     * <p>Merges the server-side NBT of the entity into its client-side counterpart.</p>
     *
     * @param contextSupplier {@link Supplier} providing client-side context for which to schedule work.
     * @return {@code true}, since the handler cannot fail.
     */
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
