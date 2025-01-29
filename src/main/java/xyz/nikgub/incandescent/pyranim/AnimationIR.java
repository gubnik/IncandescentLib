package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class AnimationIR
{
    private Float length = null;

    @NotNull
    private PyranimLexer.State currentState = PyranimLexer.State.GLOBAL_HEADER;
    private boolean doLoop = false;

    private String currentPart = "";
    private AnimationChannel.Interpolation currentInterpolation = null;
    private float currentTime = 0;

    private final Map<String, AnimationPartInfo> mappedBoneAnimations = new HashMap<>();

    public AnimationIR ()
    {

    }

    public void addKeyframe (@NotNull KeyframeIR keyframeIR)
    {
        this.mappedBoneAnimations.putIfAbsent(currentPart, new AnimationPartInfo());
        AnimationPartInfo partInfo = mappedBoneAnimations.get(currentPart);
        partInfo.addKeyframe(currentTime, keyframeIR);
    }

    @NotNull
    public AnimationDefinition.Builder bakeIntoBuilder ()
    {
        if (length == null)
        {
            throw new IllegalStateException("Animation length unset");
        }
        if (length <= 0)
        {
            throw new IllegalStateException("Animation length cannot be negative");
        }
        AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(length);
        for (var unbaked : mappedBoneAnimations.entrySet())
        {
            for (AnimationChannel channel : unbaked.getValue().bakeIntoChannel())
            {
                builder.addAnimation(unbaked.getKey(), channel);
            }
        }
        if (doLoop)
        {
            builder.looping();
        }
        return builder;
    }

    public void setLength (float length)
    {
        if (this.length != null)
        {
            throw new IllegalArgumentException("Length of an AnimationIR cannot be redefined");
        }
        this.length = length;
    }

    public String getCurrentPart ()
    {
        return currentPart;
    }

    public void setCurrentPart (String currentPart)
    {
        this.currentPart = currentPart;
    }

    public AnimationChannel.Interpolation getCurrentInterpolation ()
    {
        return currentInterpolation;
    }

    public void setCurrentInterpolation (AnimationChannel.Interpolation currentInterpolation)
    {
        this.currentInterpolation = currentInterpolation;
    }

    public float getCurrentTime ()
    {
        return currentTime;
    }

    public void setCurrentTime (float currentTime)
    {
        this.currentTime = currentTime;
    }

    @NotNull
    public PyranimLexer.State getCurrentState ()
    {
        return currentState;
    }

    public void setCurrentState (@NotNull PyranimLexer.State currentState)
    {
        this.currentState = currentState;
    }

    public void setDoLoop (boolean doLoop)
    {
        this.doLoop = doLoop;
    }
}
