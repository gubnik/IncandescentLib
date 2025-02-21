package xyz.nikgub.incandescent.common.animation;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.AnimationState;
import net.minecraftforge.common.IExtensibleEnum;

/**
 * Record used by {@link IAnimationPurposeEntity}
 *
 * @param animationState   AnimationState of an entity
 * @param animationPurpose Type of animation, must be declared properly for generalized behaviour
 * @see IAnimationPurposeEntity
 */
public record DeterminedAnimation(AnimationState animationState, AnimationPurpose animationPurpose)
{
    /**
     * Enumeration of possible purposes of {@link DeterminedAnimation}.
     *
     * <p>This enumeration is extensible via Forge's API to provide a reliable
     * way to extend the list shall the entity be animated so much the possible purposes are
     * not sufficient to describe all possible states.</p>
     *
     * @see IAnimationPurposeEntity
     */
    @SuppressWarnings("unused")
    public enum AnimationPurpose implements IExtensibleEnum
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
        MISC_4;

        /**
         * Extension method for the {@link AnimationPurpose}.
         * Its contents are replaced at runtime by ASM, as specified in Forge's docs.
         *
         * @param name {@code String} representation of the created enum entry
         * @return Newly created instance of {@link AnimationPurpose}
         * @see IExtensibleEnum
         */
        public static AnimationPurpose create (String name)
        {
            throw new IllegalStateException("Enum not extended");
        }
    }

    public static final EntityDataSerializer<AnimationPurpose> ANIMATION_SERIALIZER = EntityDataSerializer.simpleEnum(DeterminedAnimation.AnimationPurpose.class);

    static
    {
        EntityDataSerializers.registerSerializer(ANIMATION_SERIALIZER);
    }
}
