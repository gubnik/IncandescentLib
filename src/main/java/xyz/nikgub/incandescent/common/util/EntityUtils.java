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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class EntityUtils
{

    /**
     * Shoots a {@code projectile} from {@code shooter} with {@code speed} with an {@code inaccuracy}
     *
     * @param projectile {@link Projectile} to be shot
     * @param shooter    {@link LivingEntity} responsible for the act of shooting
     * @param speed      {@code float} speed factor applied to the projectile
     * @param inaccuracy {@code float} inaccuracy applied to the projectile
     */
    public static void shootProjectile (Projectile projectile, LivingEntity shooter, float speed, float inaccuracy)
    {
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.3, shooter.getZ());
        projectile.shoot(shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z, speed, inaccuracy);
        shooter.level().addFreshEntity(projectile);
    }

    /**
     * Collects all entities of class {@code tClass} into a list.
     *
     * @param tClass {@link Class} of the entities to be collected
     * @param center {@link Vec3} center point from which the {@code radius} will be expanded
     * @param radius {@code double} radius by which to expand the collector
     * @param level  {@link Level} on which to perform the collection
     * @param <T>    Type of entities to be collected
     * @return {@link List} of collected entities
     */
    public static <T extends Entity> List<T> simpleEntityCollector (Class<T> tClass, Vec3 center, double radius, Level level)
    {
        return level.getEntitiesOfClass(tClass, new AABB(center, center).inflate(radius), e -> true);
    }

    /**
     * Collects all {@link LivingEntity} within a radius into a list.
     *
     * @param center {@link Vec3} center point from which the {@code radius} will be expanded
     * @param radius {@code double} radius by which to expand the collector
     * @param level  {@link Level} on which to perform the collection
     * @return {@link List} of collected entities
     */
    public static List<? extends LivingEntity> simpleEntityCollector (Vec3 center, double radius, Level level)
    {
        return simpleEntityCollector(LivingEntity.class, center, radius, level);
    }

    /**
     * Collects all entities of class {@code tClass} into a list, sorting them by distance from {@code center}.
     * This should only be used if the sorting of the enemies is important, otherwise use {@link #simpleEntityCollector(Class, Vec3, double, Level)}
     * for performance reasons.
     *
     * @param tClass {@link Class} of the entities to be collected
     * @param center {@link Vec3} center point from which the {@code radius} will be expanded
     * @param radius {@code double} radius by which to expand the collector
     * @param level  {@link Level} on which to perform the collection
     * @param <T>    Type of entities to be collected
     * @return {@link List} of collected entities
     */
    public static <T extends Entity> List<T> entityCollector (Class<T> tClass, Vec3 center, double radius, Level level)
    {
        return level.getEntitiesOfClass(tClass, new AABB(center, center).inflate(radius), e -> true).stream().sorted(Comparator.comparingDouble(
            entityFound -> entityFound.distanceToSqr(center))).toList();
    }

    /**
     * Collects all {@link LivingEntity} within a radius into a list, sorting them by distance from {@code center}.
     * This should only be used if the sorting of the enemies is important, otherwise use {@link #simpleEntityCollector(Vec3, double, Level)}
     * for performance reasons.
     *
     * @param center {@link Vec3} center point from which the {@code radius} will be expanded
     * @param radius {@code double} radius by which to expand the collector
     * @param level  {@link Level} on which to perform the collection
     * @return {@link List} of collected entities
     */
    public static List<? extends LivingEntity> entityCollector (Vec3 center, double radius, Level level)
    {
        return entityCollector(LivingEntity.class, center, radius, level);
    }

    /**
     * Checks if all armor pieces equipped by {@code entity} share the material with {@code checkedArmorItem}
     *
     * @param entity           {@link LivingEntity} for which to check the armor pieces
     * @param checkedArmorItem {@link ArmorItem} from which to take the material to check for
     * @return {@code boolean} whether all armor items equipped by {@code entity} share the material with {@code checkedArmorItem}
     */
    public static boolean hasFullSetEquipped (LivingEntity entity, ArmorItem checkedArmorItem)
    {
        boolean b = true;
        final ArmorMaterial material = checkedArmorItem.getMaterial();
        for (final EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
        {
            if (!(entity.getItemBySlot(equipmentSlot).getItem() instanceof ArmorItem armorItem) || armorItem.getMaterial() != material)
            {
                b = false;
                break;
            }
        }
        return b;
    }

    /**
     * Shortens the duration of {@code effect} on the {@code entity}.
     *
     * @param entity    {@link Entity} for which to shorten the effect
     * @param effect    {@link MobEffect} that should be shortened
     * @param tickValue {@code int} time in ticks that will be subtracted from the duration
     */
    public static void shortenEffect (final LivingEntity entity, final MobEffect effect, final int tickValue)
    {
        MobEffectInstance instance = entity.getEffect(effect);
        assert instance != null;
        MobEffectInstance newInstance = new MobEffectInstance(instance.getEffect(), Mth.clamp(instance.getDuration() - tickValue, 0, instance.getDuration()), instance.getAmplifier(), instance.isAmbient(), instance.isVisible(), instance.showIcon());
        entity.removeEffect(effect);
        entity.addEffect(newInstance);
    }

    /**
     * Covers an entire collision box of {@code entity} in {@code particleType} particles
     *
     * @param entity        {@link Entity} that should be covered
     * @param particleType  {@link SimpleParticleType} with which to cover
     * @param particleSpeed {@code double} the speed coefficient applied to the particles
     */
    public static void coverInParticles (final LivingEntity entity, final SimpleParticleType particleType, final double particleSpeed)
    {
        if (!(entity.level() instanceof ServerLevel level)) return;
        float height = entity.getBbHeight();
        float width = entity.getBbWidth();
        level.sendParticles(particleType, entity.getX(), entity.getY() + height / 2, entity.getZ(), (int) (10 * width * height * width), width / 2, height / 2, width / 2, particleSpeed);
    }
}
