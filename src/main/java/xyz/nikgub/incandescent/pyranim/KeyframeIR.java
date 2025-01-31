package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

/**
 * Intermediate representation of {@link Keyframe} that is allowed to have undefined timestamp.
 *
 * <p>This class is used solely in {@link PyranimParser} to construct the {@link Keyframe}
 * with provided type of transform performed by it.</p>
 *
 * @see AnimationIR
 * @see AnimationPartInfo
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
@ApiStatus.Internal
public record KeyframeIR(PyranimLexer.Instruction instruction, float xValue, float yValue, float zValue,
                         AnimationChannel.Interpolation interpolation)
{
    public Keyframe toKeyframe (float time)
    {
        return new Keyframe(time, new Vector3f(xValue, yValue, zValue), interpolation);
    }
}
