package xyz.nikgub.incandescent.client.animations;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.AnimationState;

/**
 * Record used by {@link IAnimationPurposeEntity}
 *
 * @param animationState   AnimationState of an entity
 * @param animationPurpose Type of animation, must be declared properly for generalized behaviour
 */
public record DeterminedAnimation(AnimationState animationState, AnimationPurpose animationPurpose)
{
    @SuppressWarnings("unused")
    public enum AnimationPurpose
    {
        IDLE,
        MAIN_ATTACK,
        SPECIAL_ATTACK,
        ULTIMATE_ATTACK,
        HURT,
        SPECIAL_HURT,
        SPAWN,
        DEATH,
        SPECIAL_DEATH,
        STOMP,
        DIG,
        CONJURE,
        TRANSFORM,
        MISC_1,
        MISC_2,
        MISC_3,
        MISC_4,
    }

    public static final EntityDataSerializer<AnimationPurpose> ANIMATION_SERIALIZER = EntityDataSerializer.simpleEnum(DeterminedAnimation.AnimationPurpose.class);

    static
    {
        EntityDataSerializers.registerSerializer(ANIMATION_SERIALIZER);
    }
}
