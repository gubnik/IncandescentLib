package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;

public class AnimationPartInfo
{
    private final EnumMap<PyranimLexer.Instruction, Queue<Keyframe>> keyframes = new EnumMap<>(PyranimLexer.Instruction.class);

    public void addKeyframe (float timestamp, KeyframeIR keyframeIR)
    {
        this.keyframes.putIfAbsent(keyframeIR.instruction(), new LinkedList<>());
        Queue<Keyframe> keyframes = this.keyframes.get(keyframeIR.instruction());
        keyframes.add(keyframeIR.toKeyframe(timestamp));
    }

    @NotNull
    public Queue<AnimationChannel> bakeIntoChannel ()
    {
        Queue<AnimationChannel> retVal = new LinkedList<>();
        for (var unbaked : keyframes.entrySet())
        {
            retVal.add(new AnimationChannel(unbaked.getKey().getAnimationTarget(), unbaked.getValue().toArray(new Keyframe[0])));
        }
        return retVal;
    }
}
