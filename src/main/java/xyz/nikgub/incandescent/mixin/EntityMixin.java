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

package xyz.nikgub.incandescent.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nikgub.incandescent.IncandescentConfig;
import xyz.nikgub.incandescent.IncandescentHooks;
import xyz.nikgub.incandescent.autogen_network.IncandescentNetworkAPI;
import xyz.nikgub.incandescent.common.event.SyncEntityNBTEvent;
import xyz.nikgub.incandescent.network.s2c.SyncEntityNBTPacket;

@Mixin(Entity.class)
public abstract class EntityMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    public void tickMixin (CallbackInfo callbackInfo)
    {
        final Entity self = (Entity) (Object) this;
        if (!(self.level() instanceof ServerLevel))
        {
            return;
        }
        if (!IncandescentConfig.server_allow_forced_entity_nbt_sync || !IncandescentConfig.common_allow_forced_entity_nbt_sync)
        {
            // Quit processing if the configuration disallows NBT sync
            return;
        }
        final CompoundTag tag = self.getPersistentData();
        final SyncEntityNBTEvent event = IncandescentHooks.syncEntityNbtEvent(self, tag);
        if (event.doSync())
        {
            IncandescentNetworkAPI.sendPacket(SyncEntityNBTPacket.create(self.getId(), tag));
        }
    }
}
