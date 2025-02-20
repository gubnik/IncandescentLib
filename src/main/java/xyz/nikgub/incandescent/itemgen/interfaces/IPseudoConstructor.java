package xyz.nikgub.incandescent.itemgen.interfaces;

import net.minecraft.world.item.Item;


public interface IPseudoConstructor<I extends Item>
{
    I create (Object... args);
}
