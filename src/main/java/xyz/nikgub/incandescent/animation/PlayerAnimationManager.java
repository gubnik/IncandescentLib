package xyz.nikgub.incandescent.animation;

import io.netty.util.internal.UnstableApi;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.world.entity.player.Player;
import xyz.nikgub.incandescent.pyranim.LegacyPyranim;

import java.util.HashMap;
import java.util.Map;

/**
 * <h2>How does it work?</h2>
 * <p>When you launch an animation via a method, AnimationInstance containing info about launched animation will be put into a hash map.</p>
 * <p>Then player's model will try to consume the animation for its player, making it null if successful.</p>
 *
 * @author nikgub_
 * @apiNote Animations break if started in first person and then switched to third person
 */
@SuppressWarnings("unused")
@UnstableApi
public class PlayerAnimationManager
{

    private static final AnimationDefinition EMPTY = LegacyPyranim.ofPlayer("empty.pyranim");

    private static final Map<Player, AnimationInstance> runningAnimations = new HashMap<>();

    public static void launchAnimation (Player player, AnimationDefinition animationDefinition, boolean override)
    {
        PlayerAnimationManager.runningAnimations.put(player, new AnimationInstance(animationDefinition, override));
    }

    public static void endAnimation (Player player)
    {
        PlayerAnimationManager.runningAnimations.put(player, new AnimationInstance(EMPTY, true));
    }

    public static AnimationInstance consumeAnimationFor (Player player)
    {
        AnimationInstance instance = runningAnimations.get(player);
        runningAnimations.put(player, null);
        return instance;
    }

    public record AnimationInstance(AnimationDefinition animation, boolean override)
    {
    }
}
