package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.jetbrains.annotations.ApiStatus;

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
        return new Keyframe(time, switch (instruction)
        {
            case MOVE -> KeyframeAnimations.posVec(xValue, yValue, zValue);
            case ROTATE -> KeyframeAnimations.degreeVec(xValue, yValue, zValue);
            case SCALE -> KeyframeAnimations.scaleVec(xValue, yValue, zValue);
        }, interpolation);
    }
}
