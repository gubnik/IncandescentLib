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

package xyz.nikgub.incandescent.common.item_interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Interface that hooks the {@link net.minecraft.world.item.Item} into
 * {@link xyz.nikgub.incandescent.mixin.client.ItemInHandRendererMixin} and {@link xyz.nikgub.incandescent.mixin.client.HumanoidModelMixin}
 * for further processing of swing animations
 */
public interface ICustomSwingItem
{

    /**
     * Method providing transformations of the third person swing animation.
     * Default value provides vanilla MC animation
     *
     * @param itemStack  {@link ItemStack} that performed the swing
     * @param model      {@link HumanoidModel} that is animated
     * @param entity     {@link net.minecraft.world.entity.Entity} for which the animation plays
     * @param ageInTicks {@link Float} representing how many ticks is the model alive
     * @param <T>        Generic type extending {@link LivingEntity}
     * @see xyz.nikgub.incandescent.mixin.client.HumanoidModelMixin
     */
    default <T extends LivingEntity> void thirdPersonTransform (ItemStack itemStack, HumanoidModel<T> model, T entity, float ageInTicks)
    {
        if (!(model.attackTime <= 0.0F))
        {
            HumanoidArm arm = entity.swingingArm == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
            ModelPart modelpart = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
            float f = model.attackTime;
            model.body.yRot = Mth.sin(Mth.sqrt(f) * ((float) Math.PI * 2F)) * 0.2F;
            if (arm == HumanoidArm.LEFT)
            {
                model.body.yRot *= -1.0F;
            }

            model.rightArm.z = Mth.sin(model.body.yRot) * 5.0F;
            model.rightArm.x = -Mth.cos(model.body.yRot) * 5.0F;
            model.leftArm.z = -Mth.sin(model.body.yRot) * 5.0F;
            model.leftArm.x = Mth.cos(model.body.yRot) * 5.0F;
            model.rightArm.yRot += model.body.yRot;
            model.leftArm.yRot += model.body.yRot;
            model.leftArm.xRot += model.body.yRot;
            f = 1.0F - model.attackTime;
            f *= f;
            f *= f;
            f = 1.0F - f;
            float f1 = Mth.sin(f * (float) Math.PI);
            float f2 = Mth.sin(model.attackTime * (float) Math.PI) * -(model.head.xRot - 0.7F) * 0.75F;
            modelpart.xRot -= f1 * 1.2F + f2;
            modelpart.yRot += model.body.yRot * 2.0F;
            modelpart.zRot += Mth.sin(model.attackTime * (float) Math.PI) * -0.4F;
        }
    }

    /**
     * Method providing transformations of the first person swing animation.
     * Default value provides vanilla MC animation
     *
     * @param itemStack        {@link ItemStack} that performed the swing
     * @param poseStack        {@link PoseStack} of arm transformations
     * @param swingProgress    Swing progress of an item
     * @param equippedProgress Equip progress of an item
     * @param isRight          Whether the arm is the right one or not
     * @see xyz.nikgub.incandescent.mixin.client.ItemInHandRendererMixin
     */
    default void firstPersonTransform (ItemStack itemStack, PoseStack poseStack, float swingProgress, float equippedProgress, boolean isRight)
    {
        HumanoidArm arm = isRight ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) i * f1 * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) i * -45.0F));
    }
}
