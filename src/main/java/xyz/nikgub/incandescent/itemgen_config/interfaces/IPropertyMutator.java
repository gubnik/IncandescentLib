package xyz.nikgub.incandescent.itemgen_config.interfaces;

import net.minecraft.world.item.Item;

@FunctionalInterface
public interface IPropertyMutator<T>
{
    Item.Properties set (Item.Properties properties, T val);
}
