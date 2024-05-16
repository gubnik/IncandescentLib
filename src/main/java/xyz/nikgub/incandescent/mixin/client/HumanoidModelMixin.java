package xyz.nikgub.incandescent.mixin.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nikgub.incandescent.animations.PlayerAnimationManager;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Mixin(value = HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel{

    @Unique
    private final AnimationState incandescentLib$STATE = new AnimationState();
    @Unique
    private AnimationDefinition RUNNING_ANIMATION_DEFINITION = null;
    @Unique
    private final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL"))
    public void setupAnimMixinTail(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo)
    {
        if(!(entity instanceof Player player)) return; // Asserts that only player model is animated
        this.incandescentLib$animate(incandescentLib$STATE, RUNNING_ANIMATION_DEFINITION, ageInTicks); // Tries to play RUNNING_ANIMATION_DEFINITION every tick, null handling included
        PlayerAnimationManager.AnimationInstance instance = PlayerAnimationManager.consumeAnimationFor(player); // Gets animation for player, otherwise gets null
        if(instance == null) return; // Quit if no animation was supplied
        if(!instance.override() && this.RUNNING_ANIMATION_DEFINITION != null) return; // Quit if animation doesn't override current animation
        RUNNING_ANIMATION_DEFINITION = instance.animation();
        incandescentLib$STATE.start(entity.tickCount);
    }

    @Unique
    void incandescentLib$keyframesAnimate(HumanoidModel<?> model, AnimationDefinition definition, long l, float v, Vector3f vector3f) {
        float f = incandescentLib$getElapsedSeconds(definition, l);

        for(Map.Entry<String, List<AnimationChannel>> entry : definition.boneAnimations().entrySet()) {
            ModelPart modelPart = incandescentLib$acceptablePart(model, entry.getKey());
            List<AnimationChannel> list = entry.getValue();
            if(modelPart != null)
            {
                for (AnimationChannel animationChannel : list)
                {
                    Keyframe[] akeyframe = animationChannel.keyframes();
                    int i = Math.max(0, Mth.binarySearch(0, akeyframe.length, (p_232315_) -> f <= akeyframe[p_232315_].timestamp()) - 1);
                    int j = Math.min(akeyframe.length - 1, i + 1);
                    Keyframe keyframe = akeyframe[i];
                    Keyframe keyframe1 = akeyframe[j];
                    float f1 = f - keyframe.timestamp();
                    float f2;
                    if (j != i) {
                        f2 = Mth.clamp(f1 / (keyframe1.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
                    } else {
                        f2 = 0.0F;
                    }
                    if(i == akeyframe.length - 1) this.RUNNING_ANIMATION_DEFINITION = null;
                    keyframe1.interpolation().apply(vector3f, f2, akeyframe, i, j, v);
                    modelPart.xRot = 0; modelPart.yRot = 0; modelPart.zRot = 0;
                    //modelPart.x = 0; modelPart.y = 0; modelPart.z = 0;
                    animationChannel.target().apply(modelPart, vector3f);
                }
            }
        }

    }

    @Unique
    private static ModelPart incandescentLib$acceptablePart(HumanoidModel<?> model, String key) {
        return switch (key)
                {
                    case ("body") -> model.body;
                    case ("head") -> model.head;
                    case ("hat") -> model.hat;
                    case ("right_arm") -> model.rightArm;
                    case ("left_arm") -> model.leftArm;
                    case ("right_leg") -> model.rightLeg;
                    case ("left_leg") -> model.leftLeg;
                    default -> null;
                };
    }

    @Unique
    private static float incandescentLib$getElapsedSeconds(AnimationDefinition definition, long l) {
        float f = (float)l / 1000.0F;
        return definition.looping() ? f % definition.lengthInSeconds() : f;
    }

    @Unique
    private void incandescentLib$animate(AnimationState state, AnimationDefinition definition, float v) {
        this.incandescentLib$animate(state, definition, v, 1.0F);
    }

    @Unique
    private void incandescentLib$animate(AnimationState state, AnimationDefinition definition, float v, float v1) {
        if(definition == null)
        {
            state.stop();
            this.ANIMATION_VECTOR_CACHE.set(0);
            return;
        }
        state.updateTime(v, v1);
        state.ifStarted((animationState) -> this.incandescentLib$keyframesAnimate((HumanoidModel<?>) (Object) this, definition, animationState.getAccumulatedTime(), 1.0F, this.ANIMATION_VECTOR_CACHE));
    }
}
