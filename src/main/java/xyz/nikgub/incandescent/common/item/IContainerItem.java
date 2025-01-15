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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Interface for mod items that have associated inventory
 *
 * @author nikgub_
 */
@SuppressWarnings("unused")
public interface IContainerItem
{
    @SuppressWarnings("unused")
    ICapabilityProvider initCapabilities (ItemStack stack, @Nullable CompoundTag nbt);

    /**
     * Method that gets a copy of itemstack from a slot of an itemstack <p>
     * Made not-static to enforce readability
     *
     * @param target ItemStack with an associated item handling capability
     * @param id     ID of a slot
     * @return ItemStack from a slot
     */
    default ItemStack getItemFromItem (ItemStack target, int id)
    {
        AtomicReference<ItemStack> AR = new AtomicReference<>(ItemStack.EMPTY);
        target.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(capability -> AR.set(capability.getStackInSlot(id).copy()));
        return AR.get();
    }

    /**
     * Method that sets a copy of itemstack to a slot of an itemstack <p>
     * Made not-static to enforce readability
     *
     * @param target ItemStack with an associated item handling capability
     * @param value  ItemStack that will have its copy placed in a slot
     * @param id     ID of a slot
     */
    default void setItemInItem (ItemStack target, ItemStack value, int id)
    {
        target.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(capability ->
        {
            if (capability instanceof IItemHandlerModifiable handlerModifiable)
            {
                handlerModifiable.setStackInSlot(id, value);
            }
        });
    }
}
