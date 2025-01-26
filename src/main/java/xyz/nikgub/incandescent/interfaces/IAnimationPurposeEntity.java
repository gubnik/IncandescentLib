package xyz.nikgub.incandescent.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.animation.DeterminedAnimation;

import java.util.List;

/**
 * Interface that allows for safe interjection into entity's animation behaviour
 * by introducing generalized behaviour.
 * Generalized behaviour has no control over animation states that were not introduced by getAllAnimations().
 *
 * @author nikgub_
 */
public interface IAnimationPurposeEntity
{

    private Entity asEntity ()
    {
        return (Entity) this;
    }

    /**
     * @return entity's data accessor responsible for storing animation state
     */
    EntityDataAccessor<DeterminedAnimation.AnimationPurpose> getAnimationStateDataAccessor ();

    default void setState (DeterminedAnimation.AnimationPurpose state)
    {
        this.asEntity().getEntityData().set(getAnimationStateDataAccessor(), state);
    }

    default DeterminedAnimation.AnimationPurpose getState ()
    {
        return this.asEntity().getEntityData().get(getAnimationStateDataAccessor());
    }

    /**
     * Method that determines animations <p>
     * If an AnimationState exists for an entity but is not provided via this method it is ignored for all methods of this interface
     *
     * @return List of all entity animations
     */
    @NotNull
    List<DeterminedAnimation> getAllAnimations ();

    /**
     * Default method that provides all currently running animations
     *
     * @return List of all currently running animations
     */
    default List<DeterminedAnimation> getRunningAnimations ()
    {
        return getAllAnimations().stream().filter(determinedAnimation -> determinedAnimation.animationState().isStarted()).toList();
    }

    /**
     * Method that creates a list of AnimationState for all animations of certain purpose
     *
     * @param animationPurpose AnimationPurpose acting as a search key
     * @return List of AnimationState
     */
    default List<AnimationState> ofPurpose (DeterminedAnimation.AnimationPurpose animationPurpose)
    {
        return getAllAnimations().stream()
            .filter(determinedAnimation -> determinedAnimation.animationPurpose() == animationPurpose)
            .map(DeterminedAnimation::animationState).toList();
    }

    /**
     * Default method that stops all currently running animations
     */
    default void stopAllAnimations ()
    {
        for (DeterminedAnimation determinedAnimation : getAllAnimations())
        {
            if (determinedAnimation.animationState().isStarted()) determinedAnimation.animationState().stop();
        }
    }

    default AnimationState getAnimationOf (DeterminedAnimation.AnimationPurpose animationPurpose)
    {
        return getAllAnimations().stream().filter((determinedAnimation -> determinedAnimation.animationPurpose() == animationPurpose)).findFirst().orElseThrow().animationState();
    }

    default void runAnimationOf (DeterminedAnimation.AnimationPurpose animationPurpose)
    {
        EntityDataAccessor<DeterminedAnimation.AnimationPurpose> dataAccessor = this.getAnimationStateDataAccessor();
        this.asEntity().getEntityData().set(dataAccessor, animationPurpose);
    }

    default void animationSyncedDataHandler (EntityDataAccessor<?> dataAccessor)
    {
        if (!(getAnimationStateDataAccessor().equals(dataAccessor))) return;
        this.stopAllAnimations();
        getAnimationOf(this.getState()).startIfStopped(this.asEntity().tickCount);
    }
}
