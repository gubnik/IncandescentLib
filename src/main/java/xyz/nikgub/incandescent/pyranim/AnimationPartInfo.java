package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.Keyframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPartInfo
{
    private final Map<Float, KeyframeTriplet> animations = new HashMap<>();

    public void addAnimation (float pointInTime, AnimationIR animationIR)
    {
        if (animations.get(pointInTime) == null)
        {
            animations.put(pointInTime, new KeyframeTriplet());
        }
        var triplet = animations.get(pointInTime);
        switch (animationIR.instruction())
        {
            case MOVE ->
            {
                if (triplet.movKeyframe != null)
                {
                    throw new RuntimeException("Redefinition of movement keyframe at the moment " + pointInTime);
                }
                triplet.movKeyframe = animationIR;
            }
            case ROTATE ->
            {
                if (triplet.rotKeyframe != null)
                {
                    throw new RuntimeException("Redefinition of rotation keyframe at the moment " + pointInTime);
                }
                triplet.rotKeyframe = animationIR;
            }
            case SCALE ->
            {
                if (triplet.sclKeyframe != null)
                {
                    throw new RuntimeException("Redefinition of scale keyframe at the moment " + pointInTime);
                }
                triplet.sclKeyframe = animationIR;
            }
        }
        animations.put(pointInTime, triplet);
    }

    public List<Keyframe> getMov ()
    {
        return new ArrayList<>(animations.entrySet().stream().map(entry -> entry.getValue().movKeyframe.toKeyframe(entry.getKey())).toList());
    }

    public List<Keyframe> getRot ()
    {
        return new ArrayList<>(animations.entrySet().stream().map(entry -> entry.getValue().rotKeyframe.toKeyframe(entry.getKey())).toList());
    }

    public List<Keyframe> getScl ()
    {
        return new ArrayList<>(animations.entrySet().stream().map(entry -> entry.getValue().sclKeyframe.toKeyframe(entry.getKey())).toList());
    }

    private static class KeyframeTriplet
    {
        private AnimationIR movKeyframe = null;
        private AnimationIR rotKeyframe = null;
        private AnimationIR sclKeyframe = null;
    }
}
