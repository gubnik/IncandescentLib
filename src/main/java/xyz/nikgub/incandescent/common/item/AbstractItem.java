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

import net.minecraft.world.item.Item;

import java.util.UUID;

/**
 * Filler class to avoid using accesstransformer.cfg
 */
public abstract class AbstractItem extends Item
{

    public AbstractItem (Properties p_41383_)
    {
        super(p_41383_);
    }

    public static UUID BASE_DAMAGE = Item.BASE_ATTACK_DAMAGE_UUID;
    public static UUID BASE_SPEED = Item.BASE_ATTACK_SPEED_UUID;
}
