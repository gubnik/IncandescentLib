package xyz.nikgub.incandescent.common.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;

public class GeneralUtils
{

    /**
     * Turns the explicit RGB params into an integer.
     *
     * @param red   {@code int} value of red channel
     * @param green {@code int} value of green channel
     * @param blue  {@code int} value of blue channel
     * @return {@code int} representing the color
     */
    public static int rgbToColorInteger (int red, int green, int blue)
    {
        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Turns the explicit RGBA params into an integer.
     * Alpha channel is assumed to be MSB.
     *
     * @param red   {@code int} value of red channel
     * @param green {@code int} value of green channel
     * @param blue  {@code int} value of blue channel
     * @param alpha {@code int} value of alpha channel
     * @return {@code int} representing the color
     */
    public static int rgbaToColorInteger (int red, int green, int blue, int alpha)
    {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Checks whether the {@code serverPlayer} has completed the {@code advancement}.
     * This only works on the server-side, since {@code serverPlayer} cannot be normally obtained
     * on the client.
     *
     * @param serverPlayer {@link ServerPlayer} for which to check the {@code advancement}
     * @param advancement  {@link Advancement} that needs to be checked
     * @return {@code true} if {@code serverPlayer} has completed the {@code advancement}, {@code false} otherwise
     */
    public static boolean hasCompletedTheAdvancement (@NotNull ServerPlayer serverPlayer, @NotNull Advancement advancement)
    {
        return serverPlayer.getAdvancements()
            .getOrStartProgress(advancement)
            .isDone();
    }

    /**
     * Completes the {@code advancement} for {@code serverPlayer}.
     * This only works on the server-side, since {@code serverPlayer} cannot be normally obtained
     * on the client.
     *
     * @param serverPlayer{@link ServerPlayer} for which to complete the {@code advancement}
     * @param advancement {@link Advancement} that needs to be completed
     */
    public static void addAdvancement (@NotNull ServerPlayer serverPlayer, @NotNull Advancement advancement)
    {
        if (!hasCompletedTheAdvancement(serverPlayer, advancement))
        {
            AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
            for (String s : advancementProgress.getRemainingCriteria())
            {
                serverPlayer.getAdvancements().award(advancement, s);
            }
        }
    }

    /**
     * Completes the {@link Advancement} at {@code resourceLocation} for {@code serverPlayer}.
     * This only works on the server-side, since {@code serverPlayer} cannot be normally obtained
     * on the client.
     *
     * @param serverPlayer{@link ServerPlayer} for which to complete the {@code advancement}
     * @param resourceLocation {@link ResourceLocation} of the {@link Advancement} that needs to be completed
     */
    public static void addAdvancement (@NotNull ServerPlayer serverPlayer, @NotNull ResourceLocation resourceLocation)
    {
        Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(resourceLocation);
        if (advancement == null) return;
        addAdvancement(serverPlayer, advancement);
    }

    /**
     * Checks whether the damage was inflicted by a direct source of damage.
     * What classifies for 'direct damage' is a completely arbitrary defined characteristic,
     * you should not use this method if you disagree with this classification.
     *
     * @param damageSource {@link DamageSource} to be checked
     * @return {@code true} if {@code damageSource} is direct, {@code false} otherwise
     */
    public static boolean isDirectDamage (@NotNull final DamageSource damageSource)
    {
        return !damageSource.is(DamageTypeTags.IS_EXPLOSION) && !damageSource.is(DamageTypeTags.IS_PROJECTILE);
    }

    /**
     * Checks whether the damage was inflicted by the same entity that authored the damage.
     * An example of such damage is a melee attack.
     *
     * <p>If the damage was not inflicted by an entity, this will return false</p>
     *
     * @param damageSource {@link DamageSource} to be checked
     * @return {@code true} if {@code damageSource} is direct, {@code false} otherwise
     */
    public static boolean isSelfServedDamage (@NotNull final DamageSource damageSource)
    {
        return damageSource.getEntity() != null && damageSource.getEntity().equals(damageSource.getDirectEntity());
    }

    public static void playSound (Level level, double x, double y, double z, SoundEvent soundEvent, SoundSource source, float volume, float pitch)
    {
        if (!level.isClientSide())
            level.playSound(null, BlockPos.containing(x, y, z), soundEvent, source, volume, pitch);
        else level.playLocalSound(x, y, z, soundEvent, source, volume, pitch, false);
    }

    public static DamageSource makeDamageSource (ResourceKey<DamageType> damageType, @NotNull Level level, @Nullable Entity trueSource, @Nullable Entity proxy)
    {
        Optional<Registry<DamageType>> registry = level.registryAccess().registry(Registries.DAMAGE_TYPE);
        if (registry.isPresent())
            try
            {
                return new DamageSource(registry.get().getHolderOrThrow(damageType), proxy, trueSource);
            } catch (IllegalStateException stateException)
            {
                return new DamageSource(registry.get().getHolderOrThrow(DamageTypes.GENERIC), proxy, trueSource);
            }
        else
            throw new RuntimeException("Unable to locate damage type registry. How?");
    }

    public static Vec3 findGround (Vec3 pos, Level level)
    {
        while (pos.y > -64 && !level.getBlockState(BlockPos.containing(pos)).canOcclude())
        {
            pos = new Vec3(pos.x, pos.y - 1, pos.z);
        }
        return pos;
    }

    public static Queue<Vec3> launchRay (final Vec3 pos, final Vec3 rotations, int iterations, double step)
    {
        Queue<Vec3> ret = new LinkedList<>();
        for (int i = 0; i < iterations; i++)
        {
            ret.add(new Vec3(pos.x + rotations.x * i * step, pos.y + rotations.y * i * step, pos.z + rotations.z * i * step));
        }
        return ret;
    }

    public static Vec3 traceUntil (final LivingEntity entity, final BiConsumer<Vec3, Level> action, final double limit)
    {
        if (!(entity.level() instanceof ServerLevel level)) return Vec3.ZERO;
        final Vec3 angles = entity.getLookAngle();
        final double x = entity.getX();
        final double y = entity.getY() + entity.getEyeHeight() + 0.1;
        final double z = entity.getZ();
        double i = 1.2;
        Vec3 lookPos;
        ClipContext clip;
        while (EntityUtils.entityCollector(lookPos = new Vec3(x + angles.x * i, y + angles.y * i, z + angles.z * i), 0.25, entity.level()).isEmpty() &&
            !level.getBlockState(new BlockPos(level.clip((clip = new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(i)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))).getBlockPos().getX(), level.clip(clip).getBlockPos().getY(), level.clip(clip).getBlockPos().getZ())
            ).canOcclude() && i < limit)
        {
            action.accept(lookPos, level);
            i += 0.2;
        }
        return lookPos;
    }
}
