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

package xyz.nikgub.incandescent.itemgen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Implementation of {@link Tier} able to be constructed from a map of its fields,
 * with a default fallback in the case of failure.
 * <p>
 *     This class and its {@link #fromObjectMap(Map)} method should be used for
 *     the purposes of {@link ItemGenDefinition} shall the definition of tiers
 *     be required.
 * </p>
 *
 * @see xyz.nikgub.incandescent.itemgen.interfaces.IConverter
 * @see ItemGenDefinition
 */
public class DefaultedTierImplementation implements Tier
{
    private final int uses;
    private final float diggingSpeed;
    private final float attackDamageBonus;
    private final int harvestLevel;
    private final int enchantmentValue;
    private final String ingredientId;

    private Ingredient cachedIngredient = null;

    public DefaultedTierImplementation (int uses, float diggingSpeed, float attackDamageBonus, int harvestLevel, int enchantmentValue, String ingredientId)
    {
        this.uses = uses;
        this.diggingSpeed = diggingSpeed;
        this.attackDamageBonus = attackDamageBonus;
        this.harvestLevel = harvestLevel;
        this.enchantmentValue = enchantmentValue;
        this.ingredientId = ingredientId;
    }

    public static DefaultedTierImplementation fromObjectMap (Map<String, Object> map)
    {
        int uses = 0;
        float diggingSpeed = 0;
        float attackDamageBonus = 0;
        int harvestLevel = 0;
        int enchantmentValue = 0;
        String ingredientId = "";
        if (map.get("uses") instanceof Number number)
        {
            uses = number.intValue();
        }
        if (map.get("digging_speed") instanceof Number number)
        {
            diggingSpeed = number.floatValue();
        }
        if (map.get("attack_damage_bonus") instanceof Number number)
        {
            attackDamageBonus = number.floatValue();
        }
        if (map.get("harvest_level") instanceof Number number)
        {
            harvestLevel = number.intValue();
        }
        if (map.get("enchantment_value") instanceof Number number)
        {
            enchantmentValue = number.intValue();
        }
        if (map.get("ingredient_id") instanceof String sIngredientId)
        {
            ingredientId = sIngredientId;
        }
        return new DefaultedTierImplementation(
            uses,
            diggingSpeed,
            attackDamageBonus,
            harvestLevel,
            enchantmentValue,
            ingredientId
        );
    }

    @Override
    public int getUses ()
    {
        return uses;
    }

    @Override
    public float getSpeed ()
    {
        return diggingSpeed;
    }

    @Override
    public float getAttackDamageBonus ()
    {
        return attackDamageBonus;
    }

    @Override
    public int getLevel ()
    {
        return harvestLevel;
    }

    @Override
    public int getEnchantmentValue ()
    {
        return enchantmentValue;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient ()
    {
        if (cachedIngredient != null && !cachedIngredient.checkInvalidation())
        {
            return cachedIngredient;
        }
        final ResourceLocation ingredientLocation = ResourceLocation.of(ingredientId, ':');
        if (!ForgeRegistries.ITEMS.containsKey(ingredientLocation))
        {
            cachedIngredient = Ingredient.EMPTY;
            return cachedIngredient;
        }
        cachedIngredient = Ingredient.of(ForgeRegistries.ITEMS.getValue(ingredientLocation));
        return cachedIngredient;
    }
}
