package xyz.nikgub.incandescent.client.animations;

import net.minecraft.world.entity.AnimationState;

/**
 * Record used by {@link IAnimationPurposeEntity}
 * @param animationState            AnimationState of an entity
 * @param animationPurpose          Type of animation, must be declared properly for generalized behaviour
 * @param signal                    Byte value of a signal used by Entity.handleEntityEvent(byte)
 * @param localPriority             Animations with higher priority are prioritized, also acts as an id.<p>
 *
 * @since 1.2.0                     byte and localPriority params are deprecated and will be removed in 1.3.0
 */
public record DeterminedAnimation(AnimationState animationState, AnimationPurpose animationPurpose, byte signal, int localPriority)
{
    public enum AnimationPurpose
    {
        MAIN_ATTACK,
        SPECIAL_ATTACK,
        HURT,
        SPECIAL_HURT,
        SPAWN,
        DEATH,
        MISC,
        IDLE,
        STOMP,
        DIG,
        CONJURE,
        TRANSFORM
    }
}
