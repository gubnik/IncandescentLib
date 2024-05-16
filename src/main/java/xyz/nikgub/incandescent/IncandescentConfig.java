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

    public static final ForgeConfigSpec.ConfigValue<Double> SCREEN_SHAKE_INTENSITY = BUILDER
            .comment("Defines how severe is screenshake")
            .defineInRange("screenshake_amount", 0.01d, 0, 1f);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static double screen_shake_intensity;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        screen_shake_intensity = SCREEN_SHAKE_INTENSITY.get();
    }
}
