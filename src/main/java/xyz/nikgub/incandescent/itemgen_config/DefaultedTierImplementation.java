package xyz.nikgub.incandescent.itemgen_config;

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
 * @see xyz.nikgub.incandescent.itemgen_config.interfaces.IConverter
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
