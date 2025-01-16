package xyz.nikgub.incandescent.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.security.PublicKey;
import java.time.Instant;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Class containing read/write function handling for
 * later use in {@link IncandescentNetworkCore}
 *
 * @see IncandescentNetworkCore
 */
public class PacketIO
{
    /**
     * Functional interface representing a write
     * function of a {@link FriendlyByteBuf}. Return type
     * is set a {@code void} to account for potential
     * {@link FriendlyByteBuf}/{@link io.netty.buffer.ByteBuf}/{@code void} return types,
     * since we do not care for their return values
     *
     * @param <T> Type of the object to be written into {@link FriendlyByteBuf}
     * @see #WRITE_FUNC_MAP
     */
    @FunctionalInterface
    public interface WriteFunc<T>
    {
        /**
         * Abstract function to write to a {@link FriendlyByteBuf}
         *
         * @param buf A {@link FriendlyByteBuf} to be written to
         * @param obj A {@code T} to be written into a {@code buf}
         */
        void write (FriendlyByteBuf buf, T obj);
    }

    /**
     * Functional interface representing a read
     * function of a {@link FriendlyByteBuf}. Return type
     * is set a {@code T} to account for different types of values
     *
     * @param <T> Type of the object to be read from {@link FriendlyByteBuf}
     * @see #READ_FUNC_MAP
     */
    @FunctionalInterface
    public interface ReadFunc<T>
    {
        /**
         * Abstract function to read from a {@link FriendlyByteBuf}
         *
         * @param buf A {@link FriendlyByteBuf} to be read from
         * @return Value read from {@link FriendlyByteBuf}
         */
        T read (FriendlyByteBuf buf);
    }

    /**
     * Fetches the write function for a class
     *
     * @param clazz Class for which to fetch the function or {@code null} is it doesn't exist
     * @return {@link WriteFunc} for the {@code clazz}
     */
    public static @Nullable PacketIO.WriteFunc<?> bufWrite (Class<?> clazz)
    {
        return WRITE_FUNC_MAP.get(clazz);
    }

    /**
     * Fetches the read function for a class
     *
     * @param clazz Class for which to fetch the function or {@code null} is it doesn't exist
     * @return {@link ReadFunc} for the {@code clazz}
     */
    public static @Nullable PacketIO.ReadFunc<?> bufRead (Class<?> clazz)
    {
        return READ_FUNC_MAP.get(clazz);
    }

    /**
     * Mapping of write functions to the classes they return
     *
     * @apiNote This mapping omits generic methods, and methods that require more than one argument
     */
    private static final Map<Class<?>, WriteFunc<?>> WRITE_FUNC_MAP = Map.ofEntries(
        Map.entry(Boolean.class, (PacketIO.WriteFunc<Boolean>) FriendlyByteBuf::writeBoolean),
        Map.entry(Integer.class, (PacketIO.WriteFunc<Integer>) FriendlyByteBuf::writeInt),
        Map.entry(Double.class, (PacketIO.WriteFunc<Double>) FriendlyByteBuf::writeDouble),
        Map.entry(Float.class, (PacketIO.WriteFunc<Float>) FriendlyByteBuf::writeFloat),
        Map.entry(Character.class, (PacketIO.WriteFunc<Character>) FriendlyByteBuf::writeChar),
        Map.entry(Byte.class, (PacketIO.WriteFunc<Byte>) FriendlyByteBuf::writeByte),
        Map.entry(CompoundTag.class, (PacketIO.WriteFunc<CompoundTag>) FriendlyByteBuf::writeNbt),
        Map.entry(UUID.class, (PacketIO.WriteFunc<UUID>) FriendlyByteBuf::writeUUID),
        Map.entry(Component.class, (PacketIO.WriteFunc<Component>) FriendlyByteBuf::writeComponent),
        Map.entry(ItemStack.class, (PacketIO.WriteFunc<ItemStack>) FriendlyByteBuf::writeItem),
        Map.entry(BlockHitResult.class, (PacketIO.WriteFunc<BlockHitResult>) FriendlyByteBuf::writeBlockHitResult),
        Map.entry(Vector3f.class, (PacketIO.WriteFunc<Vector3f>) FriendlyByteBuf::writeVector3f),
        Map.entry(IntList.class, (PacketIO.WriteFunc<IntList>) FriendlyByteBuf::writeIntIdList),
        Map.entry(BitSet.class, (PacketIO.WriteFunc<BitSet>) FriendlyByteBuf::writeBitSet),
        Map.entry(BlockPos.class, (PacketIO.WriteFunc<BlockPos>) FriendlyByteBuf::writeBlockPos),
        Map.entry(Date.class, (PacketIO.WriteFunc<Date>) FriendlyByteBuf::writeDate),
        Map.entry(GameProfile.class, (PacketIO.WriteFunc<GameProfile>) FriendlyByteBuf::writeGameProfile),
        Map.entry(PropertyMap.class, (PacketIO.WriteFunc<PropertyMap>) FriendlyByteBuf::writeGameProfileProperties),
        Map.entry(Instant.class, (PacketIO.WriteFunc<Instant>) FriendlyByteBuf::writeInstant),
        Map.entry(Long.class, (PacketIO.WriteFunc<Long>) FriendlyByteBuf::writeLong),
        Map.entry(Short.class, (PacketIO.WriteFunc<Short>) FriendlyByteBuf::writeShort),
        Map.entry(GlobalPos.class, (PacketIO.WriteFunc<GlobalPos>) FriendlyByteBuf::writeGlobalPos),
        Map.entry(ResourceLocation.class, (PacketIO.WriteFunc<ResourceLocation>) FriendlyByteBuf::writeResourceLocation),
        Map.entry(Quaternionf.class, (PacketIO.WriteFunc<Quaternionf>) FriendlyByteBuf::writeQuaternion),
        Map.entry(Property.class, (PacketIO.WriteFunc<Property>) FriendlyByteBuf::writeProperty),
        Map.entry(PublicKey.class, (PacketIO.WriteFunc<PublicKey>) FriendlyByteBuf::writePublicKey),
        Map.entry(SectionPos.class, (PacketIO.WriteFunc<SectionPos>) FriendlyByteBuf::writeSectionPos),
        Map.entry(FluidStack.class, (PacketIO.WriteFunc<FluidStack>) FriendlyByteBuf::writeFluidStack)
        // to be extended, probably
    );

    /**
     * Mapping of read functions to the classes they return
     *
     * @apiNote This mapping omits generic methods, and methods that require more than one argument
     */
    private static final Map<Class<?>, ReadFunc<?>> READ_FUNC_MAP = Map.ofEntries(
        Map.entry(Boolean.class, (PacketIO.ReadFunc<Boolean>) FriendlyByteBuf::readBoolean),
        Map.entry(Integer.class, (PacketIO.ReadFunc<Integer>) FriendlyByteBuf::readInt),
        Map.entry(Double.class, (PacketIO.ReadFunc<Double>) FriendlyByteBuf::readDouble),
        Map.entry(Float.class, (PacketIO.ReadFunc<Float>) FriendlyByteBuf::readFloat),
        Map.entry(Character.class, (PacketIO.ReadFunc<Character>) FriendlyByteBuf::readChar),
        Map.entry(Byte.class, (PacketIO.ReadFunc<Byte>) FriendlyByteBuf::readByte),
        Map.entry(CompoundTag.class, (PacketIO.ReadFunc<CompoundTag>) FriendlyByteBuf::readNbt),
        Map.entry(UUID.class, (PacketIO.ReadFunc<UUID>) FriendlyByteBuf::readUUID),
        Map.entry(Component.class, (PacketIO.ReadFunc<Component>) FriendlyByteBuf::readComponent),
        Map.entry(ItemStack.class, (PacketIO.ReadFunc<ItemStack>) FriendlyByteBuf::readItem),
        Map.entry(BlockHitResult.class, (PacketIO.ReadFunc<BlockHitResult>) FriendlyByteBuf::readBlockHitResult),
        Map.entry(Vector3f.class, (PacketIO.ReadFunc<Vector3f>) FriendlyByteBuf::readVector3f),
        Map.entry(IntList.class, (PacketIO.ReadFunc<IntList>) FriendlyByteBuf::readIntIdList),
        Map.entry(BitSet.class, (PacketIO.ReadFunc<BitSet>) FriendlyByteBuf::readBitSet),
        Map.entry(BlockPos.class, (PacketIO.ReadFunc<BlockPos>) FriendlyByteBuf::readBlockPos),
        Map.entry(Date.class, (PacketIO.ReadFunc<Date>) FriendlyByteBuf::readDate),
        Map.entry(GameProfile.class, (PacketIO.ReadFunc<GameProfile>) FriendlyByteBuf::readGameProfile),
        Map.entry(PropertyMap.class, (PacketIO.ReadFunc<PropertyMap>) FriendlyByteBuf::readGameProfileProperties),
        Map.entry(Instant.class, (PacketIO.ReadFunc<Instant>) FriendlyByteBuf::readInstant),
        Map.entry(Long.class, (PacketIO.ReadFunc<Long>) FriendlyByteBuf::readLong),
        Map.entry(Short.class, (PacketIO.ReadFunc<Short>) FriendlyByteBuf::readShort),
        Map.entry(GlobalPos.class, (PacketIO.ReadFunc<GlobalPos>) FriendlyByteBuf::readGlobalPos),
        Map.entry(ResourceLocation.class, (PacketIO.ReadFunc<ResourceLocation>) FriendlyByteBuf::readResourceLocation),
        Map.entry(Quaternionf.class, (PacketIO.ReadFunc<Quaternionf>) FriendlyByteBuf::readQuaternion),
        Map.entry(Property.class, (PacketIO.ReadFunc<Property>) FriendlyByteBuf::readProperty),
        Map.entry(PublicKey.class, (PacketIO.ReadFunc<PublicKey>) FriendlyByteBuf::readPublicKey),
        Map.entry(SectionPos.class, (PacketIO.ReadFunc<SectionPos>) FriendlyByteBuf::readSectionPos),
        Map.entry(FluidStack.class, (PacketIO.ReadFunc<FluidStack>) FriendlyByteBuf::readFluidStack)
        // to be extended, probably
    );
}
