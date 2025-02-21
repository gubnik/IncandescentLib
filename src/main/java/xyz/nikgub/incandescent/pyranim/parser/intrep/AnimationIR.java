/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2025, nikgub_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.nikgub.incandescent.pyranim.parser.intrep;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Intermediate representation of {@link AnimationDefinition} that is allowed to have
 * undefined fields be extended in more open way than {@link AnimationDefinition.Builder}.
 *
 * <p>This class is used solely in {@link PyranimParser} to construct the {@link AnimationDefinition.Builder}
 * without having to hardcode the order of directives in {@link PyranimLexer}.</p>
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
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

    public void addKeyframe (@NotNull KeyframeIR keyframeIR)
    {
        this.mappedBoneAnimations.putIfAbsent(currentPart, new AnimationPartInfo());
        AnimationPartInfo partInfo = mappedBoneAnimations.get(currentPart);
        partInfo.addKeyframe(currentTime, keyframeIR);
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
