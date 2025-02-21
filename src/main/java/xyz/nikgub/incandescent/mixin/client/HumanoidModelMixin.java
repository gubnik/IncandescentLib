package xyz.nikgub.incandescent.mixin.client;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nikgub.incandescent.common.item_interfaces.ICustomSwingItem;

@SuppressWarnings("unused")
@Mixin(value = HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel
{

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    protected void setupAttackAnimationHead (T pLivingEntity, float pAgeInTicks, CallbackInfo callbackInfo)
    {
        HumanoidModel<T> self = (HumanoidModel<T>) (Object) this;
        if (!(pLivingEntity.getMainHandItem().getItem() instanceof ICustomSwingItem item)) return;
        item.thirdPersonTransform(pLivingEntity.getMainHandItem(), self, pLivingEntity, pAgeInTicks);
        callbackInfo.cancel();
    }
}
