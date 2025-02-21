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

package xyz.nikgub.incandescent.pyranim.parser;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.pyranim.lexer.impl.Instruction;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Wrapper class that provides controlled access to {@link EnumMap} containing the mappings
 * for keyframes to their transform type.
 *
 * @see AnimationIR
 */
@ApiStatus.Internal
public class AnimationPartInfo
{
    private final EnumMap<Instruction, Queue<Keyframe>> keyframes = new EnumMap<>(Instruction.class);

    public void addKeyframe (float timestamp, KeyframeIR keyframeIR)
    {
        this.keyframes.putIfAbsent(keyframeIR.instruction(), new LinkedList<>());
        Queue<Keyframe> keyframes = this.keyframes.get(keyframeIR.instruction());
        keyframes.add(keyframeIR.toKeyframe(timestamp));
    }

    /**
     * Bakes the {@link #keyframes} into an {@link AnimationChannel}
     *
     * @return Queue of {@link AnimationChannel} to be used for the creation of {@link net.minecraft.client.animation.AnimationDefinition.Builder}
     */
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
