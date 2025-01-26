package xyz.nikgub.incandescent.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nikgub.incandescent.IncandescentHooks;
import xyz.nikgub.incandescent.event.SyncEntityNBTEvent;
import xyz.nikgub.incandescent.network.IncandescentNetwork;
import xyz.nikgub.incandescent.network.s2c.SyncEntityNBTPacket;

@Mixin(Entity.class)
public class EntityMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    public void tickMixin (CallbackInfo callbackInfo)
    {
        final Entity self = (Entity) (Object) this;
        if (!(self.level() instanceof ServerLevel))
        {
            return;
        }
        final CompoundTag tag = self.getPersistentData();
        final SyncEntityNBTEvent event = IncandescentHooks.syncEntityNbtEvent(self, tag);
        if (event.doSync())
        {
            IncandescentNetwork.sendPacket(SyncEntityNBTPacket.create(self.getId(), tag));
        }
    }
}
