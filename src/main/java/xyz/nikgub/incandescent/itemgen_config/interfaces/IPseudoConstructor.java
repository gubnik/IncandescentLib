package xyz.nikgub.incandescent.itemgen_config.interfaces;

import net.minecraft.world.item.Item;


public interface IPseudoConstructor<I extends Item>
{
    I create (Object... args);
}
