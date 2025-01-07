package xyz.nikgub.incandescent.common.item;

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

public interface IExtensibleTooltipItem
{
    default void gatherTooltipLines (@NotNull List<Component> list, String hiddenKey, String subscriber, Incandescent.Key key)
    {
        this.gatherTooltipLines((Item) this, list, hiddenKey, subscriber, key);
    }

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

