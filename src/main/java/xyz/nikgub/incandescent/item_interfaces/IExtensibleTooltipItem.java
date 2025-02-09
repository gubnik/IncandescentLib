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

package xyz.nikgub.incandescent.item_interfaces;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import xyz.nikgub.incandescent.Incandescent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface providing methods to dynamically collect {@link Item} tooltip lines
 * from a translation.
 */
public interface IExtensibleTooltipItem
{
    /**
     * Collects tooltip lines for this item.
     *
     * @param list       {@link List} of all tooltip lines rendered
     * @param hiddenKey  Translation key shown while the lines are hidden
     * @param subscriber Translation key subscriber
     * @param key        {@link xyz.nikgub.incandescent.Incandescent.Key} to show the collected lines
     * @see Item#appendHoverText
     */
    default void gatherTooltipLines (@NotNull List<Component> list, String hiddenKey, String subscriber, Incandescent.Key key)
    {
        this.gatherTooltipLines((Item) this, list, hiddenKey, subscriber, key);
    }

    /**
     * Collects tooltip lines for any item.
     *
     * @param list       {@link List} of all tooltip lines rendered
     * @param hiddenKey  Translation key shown while the lines are hidden
     * @param subscriber Translation key subscriber
     * @param key        {@link xyz.nikgub.incandescent.Incandescent.Key} to show the collected lines
     * @see Item#appendHoverText
     */
    default void gatherTooltipLines (Item item, @NotNull List<Component> list, String hiddenKey, String subscriber, Incandescent.Key key)
    {
        Optional<ResourceKey<Item>> optKey = ForgeRegistries.ITEMS.getResourceKey(item);
        if (optKey.isEmpty()) return;
        List<Component> fetchedLines = new ArrayList<>();
        String locName = optKey.get().location().getNamespace();
        String locPath = optKey.get().location().getPath();
        int it = 0;
        String lineName = "item." + locName + "." + locPath + "." + subscriber + "." + it;
        Component tComponent = Component.translatable(lineName);
        while (!tComponent.getString().equals(lineName))
        {
            fetchedLines.add(tComponent.copy().withStyle(ChatFormatting.GRAY));
            lineName = "item." + locName + "." + locPath + "." + subscriber + "." + ++it;
            tComponent = Component.translatable(lineName);

        }
        if (fetchedLines.isEmpty()) return;
        if (key.getSupplier().get()) list.addAll(fetchedLines);
        else
        {
            String rawText = Component.translatable(hiddenKey).getString();
            list.add(Component.literal(rawText + key.name()).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.BOLD));
        }
    }
}

