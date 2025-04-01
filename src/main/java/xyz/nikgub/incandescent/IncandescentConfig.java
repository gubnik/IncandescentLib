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

package xyz.nikgub.incandescent;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Incandescent.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IncandescentConfig
{
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Double> SCREEN_SHAKE_INTENSITY = CLIENT_BUILDER
        .comment("Defines how severe is the screenshake")
        .defineInRange("screenshake_amount", 0.01d, 0, 1f);

    public static final ForgeConfigSpec.ConfigValue<Boolean> COMMON_ALLOW_FORCED_ENTITY_NBT_SYNC = COMMON_BUILDER
        .comment("Set true if you want to allow to sync entity NBTs via SyncEntityNBTEvent. This will not override server config.")
        .define("allow_forced_entity_nbt_sync", true);

    public static final ForgeConfigSpec.ConfigValue<Boolean> SERVER_ALLOW_FORCED_ENTITY_NBT_SYNC = SERVER_BUILDER
        .comment("Set true if you want to allow to sync entity NBTs via SyncEntityNBTEvent on the server")
        .define("allow_forced_entity_nbt_sync", true);

    static final ForgeConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    static final ForgeConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
    static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    public static double screen_shake_intensity;

    public static boolean common_allow_forced_entity_nbt_sync;

    public static boolean server_allow_forced_entity_nbt_sync;

    @SubscribeEvent
    static void onLoad (final ModConfigEvent event)
    {
        switch (event.getConfig().getType())
        {
            case CLIENT ->
            {
                screen_shake_intensity = SCREEN_SHAKE_INTENSITY.get();
            }
            case SERVER ->
            {
                server_allow_forced_entity_nbt_sync = SERVER_ALLOW_FORCED_ENTITY_NBT_SYNC.get();
            }
            case COMMON ->
            {
                common_allow_forced_entity_nbt_sync = COMMON_ALLOW_FORCED_ENTITY_NBT_SYNC.get();
            }
        }
    }
}
