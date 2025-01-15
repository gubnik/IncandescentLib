package xyz.nikgub.incandescent.common.util;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class EntityUtils
{
    public static void shootProjectile (Projectile projectile, LivingEntity shooter, float speed, float inaccuracy)
    {
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.3, shooter.getZ());
        projectile.shoot(shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z, speed, inaccuracy);
        shooter.level().addFreshEntity(projectile);
    }

    public static List<? extends LivingEntity> entityCollector (Vec3 center, double radius, Level level)
    {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius), e -> true).stream().sorted(Comparator.comparingDouble(
            entityFound -> entityFound.distanceToSqr(center))).toList();
    }

    public static <T extends Entity> List<T> entityCollector (Class<T> tClass, Vec3 center, double radius, Level level)
    {
        return level.getEntitiesOfClass(tClass, new AABB(center, center).inflate(radius), e -> true).stream().sorted(Comparator.comparingDouble(
            entityFound -> entityFound.distanceToSqr(center))).toList();
    }

    public static boolean hasFullSetEquipped (LivingEntity entity, ArmorItem checkedArmorItem)
    {
        boolean b = true;
        for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
        {
            if (!(entity.getItemBySlot(equipmentSlot).getItem() instanceof ArmorItem armorItem) || !armorItem.getMaterial().equals(checkedArmorItem.getMaterial()))
            {
                b = false;
                break;
            }
        }
        return b;
    }

    public static void shortenEffect (final LivingEntity entity, final MobEffect effect, final int tick)
    {
        MobEffectInstance instance = entity.getEffect(effect);
        assert instance != null;
        MobEffectInstance newInstance = new MobEffectInstance(instance.getEffect(), Mth.clamp(instance.getDuration() - tick, 0, instance.getDuration()), instance.getAmplifier(), instance.isAmbient(), instance.isVisible(), instance.showIcon());
        entity.removeEffect(effect);
        entity.addEffect(newInstance);
    }

    public static void coverInParticles (final LivingEntity entity, final SimpleParticleType particleType, final double particleSpeed)
    {
        if (!(entity.level() instanceof ServerLevel level)) return;
        float height = entity.getBbHeight();
        float width = entity.getBbWidth();
        level.sendParticles(particleType, entity.getX(), entity.getY() + height / 2, entity.getZ(), (int) (10 * width * height * width), width / 2, height / 2, width / 2, particleSpeed);
    }
}
