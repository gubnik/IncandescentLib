package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import org.joml.Vector3f;

public record KeyframeIR(PyranimLexer.Instruction instruction, float xValue, float yValue, float zValue,
                         AnimationChannel.Interpolation interpolation)
{
    public Keyframe toKeyframe (float time)
    {
        return new Keyframe(time, new Vector3f(xValue, yValue, zValue), interpolation);
    }
}
