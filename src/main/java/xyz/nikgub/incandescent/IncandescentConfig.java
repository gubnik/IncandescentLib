package xyz.nikgub.incandescent;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Left empty for future possible uses
 */
@Mod.EventBusSubscriber(modid = Incandescent.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IncandescentConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
    }
}
