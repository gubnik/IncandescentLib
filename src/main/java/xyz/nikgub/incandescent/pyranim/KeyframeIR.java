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

package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

/**
 * Intermediate representation of {@link Keyframe} that is allowed to have undefined timestamp.
 *
 * <p>This class is used solely in {@link PyranimParser} to construct the {@link Keyframe}
 * with provided type of transform performed by it.</p>
 *
 * @author Nikolay Gubankov (aka nikgub)
 * @see AnimationIR
 * @see AnimationPartInfo
 */
@ApiStatus.Internal
public record KeyframeIR(PyranimLexer.Instruction instruction, float xValue, float yValue, float zValue,
                         AnimationChannel.Interpolation interpolation)
{
    public Keyframe toKeyframe (float time)
    {
        return new Keyframe(time, switch (instruction)
        {
            case MOVE -> new Vector3f(xValue, yValue, zValue);
            case ROTATE -> KeyframeAnimations.degreeVec(xValue, yValue, zValue);
            case SCALE -> KeyframeAnimations.scaleVec(xValue, yValue, zValue);
        }, interpolation);
    }
}
