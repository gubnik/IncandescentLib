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

package xyz.nikgub.incandescent;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(Incandescent.MOD_ID)
public class Incandescent
{
    public static final String MOD_ID = "incandescent_lib";

    private static final Logger LOGGER = LogUtils.getLogger();
    public static int clientTick;

    private static final Map<LocalPlayer, Double> screenShakeMap = new HashMap<>();

    public Incandescent()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, IncandescentConfig.SPEC);
    }

    public static void runShakeFor(double amount)
    {
        screenShakeMap.put(Minecraft.getInstance().player, amount);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    @SuppressWarnings("unused")
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void clientTick(final TickEvent.ClientTickEvent event) {
            clientTick++;
        }

        @SubscribeEvent
        public static void cameraSetupEvent(final ViewportEvent.ComputeCameraAngles event)
        {

            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;

            if (player == null) return;
            float delta = Minecraft.getInstance().getFrameTime();
            float ticksExistedDelta = player.tickCount + delta;
            double intensity = IncandescentConfig.screen_shake_intensity;
            if (!Minecraft.getInstance().isPaused() && player.level().isClientSide()
                    && screenShakeMap.containsKey(player)
            ) {
                event.setPitch((float) (event.getPitch() + intensity * Math.cos(ticksExistedDelta * screenShakeMap.get(player)) * 25));
                event.setYaw((float) (event.getYaw() + intensity * Math.cos(ticksExistedDelta * screenShakeMap.get(player)) * 25));
                event.setRoll((float) (event.getRoll() + intensity * Math.cos(ticksExistedDelta * screenShakeMap.get(player)) * 25));
            }
        }
    }
}
