/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2024, nikgub_

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

package xyz.nikgub.incandescent.item_interfaces;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import xyz.nikgub.incandescent.common.util.GeneralUtils;
import xyz.nikgub.incandescent.mixin.ItemStackMixin;

import java.util.function.Function;

/**
 * Interface for {@link ItemStackMixin}'s getHoverNameMixinHead()
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
public interface IGradientNameItem
{

    /**
     * Method that provides an additional condition to display gradient name
     *
     * @param itemStack ItemStack of this item
     * @return boolean
     */

    boolean getGradientCondition (ItemStack itemStack);

    /**
     * Method that provides a pair of colors to switch between
     *
     * @return Pair of integer RGB color values
     */
    Pair<Integer, Integer> getGradientColors (ItemStack itemStack);

    /**
     * Method that provides time in ticks in which full color change happens
     *
     * @return Integer time in ticks
     */
    int getGradientTickTime (ItemStack itemStack);

    /**
     * Method that provides a function that defines how does the color change depending on tick
     *
     * @return Function that consumes an integer tick and returns an integer color code
     */
    default Function<Integer, Integer> getGradientFunction (ItemStack itemStack)
    {
        final int redFirst = getGradientColors(itemStack).getFirst() / 65536, greenFirst = (getGradientColors(itemStack).getFirst() % 65536) / 256, blueFirst = getGradientColors(itemStack).getFirst() % 256;
        final int redSecond = getGradientColors(itemStack).getSecond() / 65536, greenSecond = (getGradientColors(itemStack).getSecond() % 65536) / 256, blueSecond = getGradientColors(itemStack).getSecond() % 256;
        return (tick) ->
        {
            final int cT = Math.abs(getGradientTickTime(itemStack) - tick % (getGradientTickTime(itemStack) * 2));
            return GeneralUtils.rgbToColorInteger(
                redFirst + ((redSecond - redFirst) / getGradientTickTime(itemStack)) * cT,
                greenFirst + ((greenSecond - greenFirst) / getGradientTickTime(itemStack)) * cT,
                blueFirst + ((blueSecond - blueFirst) / getGradientTickTime(itemStack)) * cT
            );
        };
    }
}
