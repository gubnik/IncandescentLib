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

package xyz.nikgub.incandescent.autogen_network.core;

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
import xyz.nikgub.incandescent.autogen_network.interfaces.PacketReadFunc;
import xyz.nikgub.incandescent.autogen_network.interfaces.PacketWriteFunc;

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
public class PacketIOMapping
{
    /**
     * Fetches the write function for a class
     *
     * @param clazz Class for which to fetch the function or {@code null} is it doesn't exist
     * @return {@link PacketWriteFunc} for the {@code clazz}
     */
    public static @Nullable PacketWriteFunc<?> bufWrite (Class<?> clazz)
    {
        return WRITE_FUNC_MAP.get(clazz);
    }

    /**
     * Fetches the read function for a class
     *
     * @param clazz Class for which to fetch the function or {@code null} is it doesn't exist
     * @return {@link PacketReadFunc} for the {@code clazz}
     */
    public static @Nullable PacketReadFunc<?> bufRead (Class<?> clazz)
    {
        return READ_FUNC_MAP.get(clazz);
    }

    /**
     * Mapping of write functions to the classes they return
     *
     * @apiNote This mapping omits generic methods, and methods that require more than one argument
     */
    private static final Map<Class<?>, PacketWriteFunc<?>> WRITE_FUNC_MAP = Map.ofEntries(
        Map.entry(Boolean.class, (PacketWriteFunc<Boolean>) FriendlyByteBuf::writeBoolean),
        Map.entry(Integer.class, (PacketWriteFunc<Integer>) FriendlyByteBuf::writeInt),
        Map.entry(Double.class, (PacketWriteFunc<Double>) FriendlyByteBuf::writeDouble),
        Map.entry(Float.class, (PacketWriteFunc<Float>) FriendlyByteBuf::writeFloat),
        Map.entry(Character.class, (PacketWriteFunc<Character>) FriendlyByteBuf::writeChar),
        Map.entry(Byte.class, (PacketWriteFunc<Byte>) FriendlyByteBuf::writeByte),
        Map.entry(CompoundTag.class, (PacketWriteFunc<CompoundTag>) FriendlyByteBuf::writeNbt),
        Map.entry(UUID.class, (PacketWriteFunc<UUID>) FriendlyByteBuf::writeUUID),
        Map.entry(Component.class, (PacketWriteFunc<Component>) FriendlyByteBuf::writeComponent),
        Map.entry(ItemStack.class, (PacketWriteFunc<ItemStack>) FriendlyByteBuf::writeItem),
        Map.entry(BlockHitResult.class, (PacketWriteFunc<BlockHitResult>) FriendlyByteBuf::writeBlockHitResult),
        Map.entry(Vector3f.class, (PacketWriteFunc<Vector3f>) FriendlyByteBuf::writeVector3f),
        Map.entry(IntList.class, (PacketWriteFunc<IntList>) FriendlyByteBuf::writeIntIdList),
        Map.entry(BitSet.class, (PacketWriteFunc<BitSet>) FriendlyByteBuf::writeBitSet),
        Map.entry(BlockPos.class, (PacketWriteFunc<BlockPos>) FriendlyByteBuf::writeBlockPos),
        Map.entry(Date.class, (PacketWriteFunc<Date>) FriendlyByteBuf::writeDate),
        Map.entry(GameProfile.class, (PacketWriteFunc<GameProfile>) FriendlyByteBuf::writeGameProfile),
        Map.entry(PropertyMap.class, (PacketWriteFunc<PropertyMap>) FriendlyByteBuf::writeGameProfileProperties),
        Map.entry(Instant.class, (PacketWriteFunc<Instant>) FriendlyByteBuf::writeInstant),
        Map.entry(Long.class, (PacketWriteFunc<Long>) FriendlyByteBuf::writeLong),
        Map.entry(Short.class, (PacketWriteFunc<Short>) FriendlyByteBuf::writeShort),
        Map.entry(GlobalPos.class, (PacketWriteFunc<GlobalPos>) FriendlyByteBuf::writeGlobalPos),
        Map.entry(ResourceLocation.class, (PacketWriteFunc<ResourceLocation>) FriendlyByteBuf::writeResourceLocation),
        Map.entry(Quaternionf.class, (PacketWriteFunc<Quaternionf>) FriendlyByteBuf::writeQuaternion),
        Map.entry(Property.class, (PacketWriteFunc<Property>) FriendlyByteBuf::writeProperty),
        Map.entry(PublicKey.class, (PacketWriteFunc<PublicKey>) FriendlyByteBuf::writePublicKey),
        Map.entry(SectionPos.class, (PacketWriteFunc<SectionPos>) FriendlyByteBuf::writeSectionPos),
        Map.entry(FluidStack.class, (PacketWriteFunc<FluidStack>) FriendlyByteBuf::writeFluidStack)
        // to be extended, probably
    );

    /**
     * Mapping of read functions to the classes they return
     *
     * @apiNote This mapping omits generic methods, and methods that require more than one argument
     */
    private static final Map<Class<?>, PacketReadFunc<?>> READ_FUNC_MAP = Map.ofEntries(
        Map.entry(Boolean.class, (PacketReadFunc<Boolean>) FriendlyByteBuf::readBoolean),
        Map.entry(Integer.class, (PacketReadFunc<Integer>) FriendlyByteBuf::readInt),
        Map.entry(Double.class, (PacketReadFunc<Double>) FriendlyByteBuf::readDouble),
        Map.entry(Float.class, (PacketReadFunc<Float>) FriendlyByteBuf::readFloat),
        Map.entry(Character.class, (PacketReadFunc<Character>) FriendlyByteBuf::readChar),
        Map.entry(Byte.class, (PacketReadFunc<Byte>) FriendlyByteBuf::readByte),
        Map.entry(CompoundTag.class, (PacketReadFunc<CompoundTag>) FriendlyByteBuf::readNbt),
        Map.entry(UUID.class, (PacketReadFunc<UUID>) FriendlyByteBuf::readUUID),
        Map.entry(Component.class, (PacketReadFunc<Component>) FriendlyByteBuf::readComponent),
        Map.entry(ItemStack.class, (PacketReadFunc<ItemStack>) FriendlyByteBuf::readItem),
        Map.entry(BlockHitResult.class, (PacketReadFunc<BlockHitResult>) FriendlyByteBuf::readBlockHitResult),
        Map.entry(Vector3f.class, (PacketReadFunc<Vector3f>) FriendlyByteBuf::readVector3f),
        Map.entry(IntList.class, (PacketReadFunc<IntList>) FriendlyByteBuf::readIntIdList),
        Map.entry(BitSet.class, (PacketReadFunc<BitSet>) FriendlyByteBuf::readBitSet),
        Map.entry(BlockPos.class, (PacketReadFunc<BlockPos>) FriendlyByteBuf::readBlockPos),
        Map.entry(Date.class, (PacketReadFunc<Date>) FriendlyByteBuf::readDate),
        Map.entry(GameProfile.class, (PacketReadFunc<GameProfile>) FriendlyByteBuf::readGameProfile),
        Map.entry(PropertyMap.class, (PacketReadFunc<PropertyMap>) FriendlyByteBuf::readGameProfileProperties),
        Map.entry(Instant.class, (PacketReadFunc<Instant>) FriendlyByteBuf::readInstant),
        Map.entry(Long.class, (PacketReadFunc<Long>) FriendlyByteBuf::readLong),
        Map.entry(Short.class, (PacketReadFunc<Short>) FriendlyByteBuf::readShort),
        Map.entry(GlobalPos.class, (PacketReadFunc<GlobalPos>) FriendlyByteBuf::readGlobalPos),
        Map.entry(ResourceLocation.class, (PacketReadFunc<ResourceLocation>) FriendlyByteBuf::readResourceLocation),
        Map.entry(Quaternionf.class, (PacketReadFunc<Quaternionf>) FriendlyByteBuf::readQuaternion),
        Map.entry(Property.class, (PacketReadFunc<Property>) FriendlyByteBuf::readProperty),
        Map.entry(PublicKey.class, (PacketReadFunc<PublicKey>) FriendlyByteBuf::readPublicKey),
        Map.entry(SectionPos.class, (PacketReadFunc<SectionPos>) FriendlyByteBuf::readSectionPos),
        Map.entry(FluidStack.class, (PacketReadFunc<FluidStack>) FriendlyByteBuf::readFluidStack)
        // to be extended, probably
    );
}
