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

package xyz.nikgub.incandescent.common.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Interface for altering attribute tooltip's color <p>
 * Used in {@link xyz.nikgub.incandescent.mixin.ItemStackMixin}
 * @author nikgub_
 */
@SuppressWarnings("unused")
public interface INotStupidTooltipItem {
    /**
     * Determines format transformations of attribute's tooltip <p>
     * New format overrides the default one
     * @return      Map of attributes and pairs of attribute's UUID and its new format
     */
    Map<Attribute, Pair<UUID, ChatFormatting>> specialColoredUUID(ItemStack itemStack);

    /**
     * Determines additional bonus in attribute's value <p>
     * This value should be gathered from player and be solely visual
     * @return      Function that consumes player and returns double value
     */
    BiFunction<Player, Attribute, Double> getAdditionalPlayerBonus(ItemStack itemStack);
}
