package xyz.nikgub.incandescent.common.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class GeneralUtils {

    public static int rgbToColorInteger(int red, int green, int blue){
        return 65536 * red + 256 * green + blue;
    }

    public static int rgbaToColorInteger(int red, int green, int blue, int alpha){
        return 16777216 * alpha + 65536 * red + 256 * green + blue;
    }

    public static boolean hasCompletedTheAdvancement(ServerPlayer serverPlayer, Advancement advancement){
        if(advancement == null) return false;
        return serverPlayer.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public static void addAdvancement(ServerPlayer serverPlayer, ResourceLocation resourceLocation)
    {
        Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(resourceLocation);
        if(advancement == null) return;
        if(!hasCompletedTheAdvancement(serverPlayer, advancement))
        {
            AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
            {
                for (String s : advancementProgress.getRemainingCriteria()) serverPlayer.getAdvancements().award(advancement, s);
            }
        }
    }

    public static boolean isDirectDamage (final DamageSource damageSource)
    {
        return !damageSource.is(DamageTypeTags.IS_EXPLOSION) && !damageSource.is(DamageTypeTags.IS_PROJECTILE);
    }

    public static void playSound (Level level, double x, double y, double z, SoundEvent soundEvent, SoundSource source, float volume, float pitch)
    {
        if (!level.isClientSide()) level.playSound(null, BlockPos.containing(x, y, z), soundEvent, source, volume, pitch);
        else level.playLocalSound(x, y, z, soundEvent, source, volume, pitch, false);
    }

    public static DamageSource makeDamageSource(ResourceKey<DamageType> damageType, @NotNull Level level, @Nullable Entity trueSource, @Nullable Entity proxy)
    {
        Optional<Registry<DamageType>> registry = level.registryAccess().registry(Registries.DAMAGE_TYPE);
        if (registry.isPresent())
            try {
                return new DamageSource(registry.get().getHolderOrThrow(damageType), proxy, trueSource);
            }
            catch (IllegalStateException stateException)
            {
                return new DamageSource(registry.get().getHolderOrThrow(DamageTypes.GENERIC), proxy, trueSource);
            }
        else
            throw new RuntimeException("Unable to locate damage type registry. How?");
    }

    public static Vec3 findGround(Vec3 pos, Level level){
        while (pos.y > -64 && !level.getBlockState(new BlockPos(new Vec3i((int) pos.x, (int) pos.y, (int) pos.z))).canOcclude()){
            pos = new Vec3(pos.x, pos.y - 1, pos.z);
        }
        return pos;
    }

    public static List<Vec3> launchRay (final Vec3 pos, final Vec3 rotations, int iterations, double step)
    {
        List<Vec3> ret = new ArrayList<>();
        for (int i = 0; i < iterations; i++)
        {
            ret.add(new Vec3(pos.x + rotations.x * i * step, pos.y + rotations.y * i * step, pos.z + rotations.z * i * step));
        }
        return ret;
    }

    public static Vec3 traceUntil(final LivingEntity entity, final BiConsumer<Vec3, Level> action, final double limit){
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
