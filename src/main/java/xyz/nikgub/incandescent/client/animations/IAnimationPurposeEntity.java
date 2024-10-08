package xyz.nikgub.incandescent.client.animations;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.Incandescent;

import java.util.Comparator;
import java.util.List;

/**
 * <p>Interface that allows for safe interjection into entity's animation behaviour
 * by introducing generalized behaviour.</p>
 * <h3>Possibilities</h3>
 * <p>Starting and stopping animations solely by {@link AnimationState} or {@link DeterminedAnimation.AnimationPurpose}.</p>
 * <p>Generalized byte event handling via default method.</p>
 * <h3>Limitations</h3>
 * <p>Determined animations must have 70 as the smallest byte signal and must not exceed 153 animations total.</p>
 * <p>Generalized behaviour has no control over animation states that were not introduced by getAllAnimations().</p>
 *
 * @author nikgub_
 */
public interface IAnimationPurposeEntity {

    byte OVERRIDE_DELTA = 32;

    private Entity asEntity()
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
     * @return      List of all entity animations
     */
    @NotNull List<DeterminedAnimation> getAllAnimations();

    /**
     * Default method that provides all currently running animations
     * @return      List of all currently running animations
     */
    default List<DeterminedAnimation> getRunningAnimations()
    {
        return getAllAnimations().stream().filter(determinedAnimation -> determinedAnimation.animationState().isStarted()).toList();
    }

    /**
     * Method that finds a corresponding byte of an AnimationState
     * @param animationState        AnimationState acting as a search key
     * @return                      Byte, associated with AnimationState via DeterminedAnimation
     */
    default byte byteOfAnimationState(AnimationState animationState)
    {
        return getAllAnimations().stream().filter(determinedAnimation -> determinedAnimation.animationState().equals(animationState)).map(DeterminedAnimation::signal).findFirst().orElseThrow();
    }

    /**
     * Method that creates a list of AnimationState for all animations of certain purpose
     * @param animationPurpose      AnimationPurpose acting as a search key
     * @return                      List of AnimationState
     */
    default List<AnimationState> ofPurpose(DeterminedAnimation.AnimationPurpose animationPurpose)
    {
        return getAllAnimations().stream()
                .filter(determinedAnimation -> determinedAnimation.animationPurpose() == animationPurpose)
                .map(DeterminedAnimation::animationState).toList();
    }

    /**
     * Default method that stops all currently running animations
     */
    default void stopAllAnimations()
    {
        for(DeterminedAnimation determinedAnimation : getAllAnimations())
        {
            if(determinedAnimation.animationState().isStarted()) determinedAnimation.animationState().stop();
        }
    }

    default AnimationState getAnimationOf(DeterminedAnimation.AnimationPurpose animationPurpose)
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

    /**
     * Method that sends a corresponding byte as an entity event to entity's level
     * @param animationState        AnimationState serving as a search key
     * @param override              True to override currently running animation
     *
     * @deprecated                  Use {@link #runAnimationOf(DeterminedAnimation.AnimationPurpose)} instead
     */
    @Deprecated(forRemoval = true)
    default void runAnimationByState(AnimationState animationState, boolean override) {
        byte msg = byteOfAnimationState(animationState);
        if(override) msg += OVERRIDE_DELTA;
        asEntity().level().broadcastEntityEvent(asEntity(), msg);
    }

    /**
     * Method that sends a corresponding byte as an entity event to entity's level
     * @param animationPurpose      AnimationPurpose serving as a search key
     * @param override              True to override currently running animation
     *
     * @deprecated                  Use {@link #runAnimationOf(DeterminedAnimation.AnimationPurpose)} instead
     */
    @Deprecated(forRemoval = true)
    default void runAnimationByPurpose(DeterminedAnimation.AnimationPurpose animationPurpose, boolean override)
    {
        AnimationState animationState = ofPurpose(animationPurpose).stream().findFirst().orElseThrow(
                () -> new RuntimeException("[" + Incandescent.MOD_ID + "] Unable to find animation of purpose " + animationPurpose + " for entity " + asEntity()));
        runAnimationByState(animationState, override);
    }

    /**
     * Default method that handles byte events
     * @param bt                    Byte event handled by Level.handleEntityEvent(Entity, byte)
     *
     * @deprecated                  Use {@link #animationSyncedDataHandler(EntityDataAccessor)} instead
     */
    @Deprecated(forRemoval = true)
    default void safelyHandleAnimations(final byte bt)
    {
        // Biggest byte in declared animations
        final byte maxMessage = getAllAnimations().stream().max(Comparator.comparing(DeterminedAnimation::signal)).orElseThrow(() -> handlerException(bt)).signal();
        // Determine if handler should override default behaviour
        boolean override = bt > maxMessage;
        final byte msg = (byte) (override ? bt -OVERRIDE_DELTA : bt);
        // Check every determined animation
        for(DeterminedAnimation determinedAnimation : getAllAnimations())
        {
            if(getRunningAnimations().isEmpty())
            {
                if(determinedAnimation.signal() != msg) return;
                // Stop the animation just in case
                determinedAnimation.animationState().stop();
                // Run the animation
                determinedAnimation.animationState().start(asEntity().tickCount);
                return;
            }
            // For each running animation...
            getRunningAnimations().forEach(runningAnimation ->
            {
                // Stop if byte doesn't align
                if (determinedAnimation.signal() != msg) return;
                // Stop if running animation's priority is greater than that of the animation with corresponding signal
                if (runningAnimation.localPriority() > determinedAnimation.localPriority() && !override) return;
                // Stop the animation just in case
                determinedAnimation.animationState().stop();
                // Run the animation
                determinedAnimation.animationState().start(asEntity().tickCount);
            });
        }
    }

    @Deprecated(forRemoval = true)
    private RuntimeException handlerException(byte bt)
    {
        return new RuntimeException("Cannot handle the byte " + bt + " supplied by " + asEntity().getDisplayName().getString());
    }
}
