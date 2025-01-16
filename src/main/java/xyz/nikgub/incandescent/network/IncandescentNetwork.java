package xyz.nikgub.incandescent.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import xyz.nikgub.incandescent.Incandescent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class responsible for networking API
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
@Mod.EventBusSubscriber(modid = Incandescent.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IncandescentNetwork
{
    /**
     * Mapping of {@link IncandescentNetworkCore} to mod IDs
     * It is populated at the end of {@link IncandescentNetwork#registerCores(FMLCommonSetupEvent)}
     */
    private static final Map<String, IncandescentNetworkCore> CORES = new HashMap<>();

    public static <T> void sendPacket (T packet)
    {
        IncandescentPacket incandescentPacket = packet.getClass().getAnnotation(IncandescentPacket.class);
        if (incandescentPacket == null)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " is not properly annotated");
        }
        IncandescentNetworkCore core = CORES.get(incandescentPacket.value());
        if (incandescentPacket.direction() == NetworkDirection.PLAY_TO_SERVER)
        {
            core.getChannelInstance().sendToServer(packet);
            return;
        }
        core.getChannelInstance().send(PacketDistributor.ALL.noArg(), packet);
    }

    public static <T> void sendToPlayer (T packet, ServerPlayer player)
    {
        IncandescentPacket incandescentPacket = packet.getClass().getAnnotation(IncandescentPacket.class);
        if (incandescentPacket == null)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " is not properly annotated");
        }
        IncandescentNetworkCore core = CORES.get(incandescentPacket.value());
        if (incandescentPacket.direction() == NetworkDirection.PLAY_TO_SERVER)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " cannot be sent to client because it is a server packet");
        }
        core.getChannelInstance().send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <T> void sendToPlayersNearby (T packet, ServerPlayer player)
    {
        IncandescentPacket incandescentPacket = packet.getClass().getAnnotation(IncandescentPacket.class);
        if (incandescentPacket == null)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " is not properly annotated");
        }
        IncandescentNetworkCore core = CORES.get(incandescentPacket.value());
        if (incandescentPacket.direction() == NetworkDirection.PLAY_TO_SERVER)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " cannot be sent to client because it is a server packet");
        }
        core.getChannelInstance().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), packet);
    }

    public static <T> void sendToPlayersNearbyAndSelf (T packet, ServerPlayer player)
    {
        IncandescentPacket incandescentPacket = packet.getClass().getAnnotation(IncandescentPacket.class);
        if (incandescentPacket == null)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " is not properly annotated");
        }
        IncandescentNetworkCore core = CORES.get(incandescentPacket.value());
        if (incandescentPacket.direction() == NetworkDirection.PLAY_TO_SERVER)
        {
            throw new IllformedPacketException("Packet " + packet.getClass().getName() + " cannot be sent to client because it is a server packet");
        }
        core.getChannelInstance().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    /**
     * Subscriber that collects and signs packets to {@link IncandescentNetworkCore}.
     * It is powered by Java's Reflection, and may or may not cause performance issues on
     * the startup.
     *
     * @param event FML setup event, running before everything else
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void registerCores (final FMLCommonSetupEvent event)
    {
        final Map<String, Set<@NotNull Class<?>>> loadPacketInfo = new HashMap<>();
        ModList.get().getAllScanData().forEach(modFileScanData ->
        {
            for (var annotationData : modFileScanData.getAnnotations())
            {
                final String id = annotationData.annotationType().getClassName();
                if (id.equals(Mixin.class.getName()))
                {
                    continue;
                }
                if (!id.equals(IncandescentPacket.class.getName()))
                {
                    continue;
                }
                try
                {
                    Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                    IncandescentPacket packet = clazz.getAnnotation(IncandescentPacket.class);
                    if (packet == null)
                    {
                        throw new FaultyPacketLoadException(String.format("Packet %s is not a packet", clazz.getName()));
                    }
                    loadPacketInfo.putIfAbsent(packet.value(), new HashSet<>());
                    loadPacketInfo.get(packet.value()).add(clazz);
                } catch (ClassNotFoundException e)
                {
                    throw new FaultyPacketLoadException(String.format("Class %s failed to load", annotationData.clazz().getClassName()), e);
                }
            }
        });
        for (var entry : loadPacketInfo.entrySet())
        {
            IncandescentNetworkCore core = new IncandescentNetworkCore(entry.getKey());
            for (var clazz : entry.getValue())
            {
                core.sign(clazz);
            }
            CORES.put(entry.getKey(), core);
        }
    }
}
