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

package xyz.nikgub.incandescent.item_interfaces;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikgub.incandescent.util.Hypermap;

import java.util.*;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

/**
 * @author Nikolay Gubankov (aka nikgub)
 */
public interface IAdvancedAttributeTooltipHandlingItem
{

    /**
     * {@link Hypermap} of styles to modifier UUID to attribute of a modifier that determines
     * which attribute modifiers to consider the default one for the item stack.
     * Default attributes are displayed without sign prefix, with a special style applied.
     * <p>
     * By default, the default attributes are assumed to be item's base attack damage and speed for
     * vanilla Minecraft compatibility purposes.
     * </p>
     *
     * @param itemStack {@link ItemStack} stack of this item
     * @return {@link Hypermap} of {@link Style} to modifier {@link UUID} to {@link Attribute} of a modifier
     */
    default Hypermap<Attribute, UUID, Style> getDefaultAttributesStyles (final ItemStack itemStack)
    {
        return Hypermap.of(
            Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_UUID, this.defaultStyle(itemStack),
            Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_UUID, this.defaultStyle(itemStack)
        );
    }

    /**
     * Additional bonus that will be added to the display value of a default attribute.
     * This bonus is purely cosmetic, and does not affect the actual value of the modifier
     * applied to the item.
     *
     * @param itemStack {@link ItemStack} stack of this item
     * @param player    {@link Player} rendering the tooltip
     * @param attribute {@link Attribute} currently being processed
     * @return {@code double} bonus value for the attribute modifier.
     */
    default double getAdditionalPlayerBonus (final ItemStack itemStack, final Player player, final Attribute attribute)
    {
        if (attribute == Attributes.ATTACK_DAMAGE)
        {
            return EnchantmentHelper.getDamageBonus(itemStack, MobType.UNDEFINED);
        }
        return 0;
    }

    /**
     * Determines the default style of default attributes.
     * Dark green by default to align with vanilla Minecraft.
     *
     * @param itemStack {@link ItemStack} stack of this item
     * @return Default style of default attributes
     */
    default Style defaultStyle (final ItemStack itemStack)
    {
        return Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    }

    /**
     * Method that is injected into {@link xyz.nikgub.incandescent.mixin.ItemStackMixin#getTooltipLinesMixinHead(Player, TooltipFlag, CallbackInfoReturnable)}.
     *
     * <p>
     * This method is a reworked Mojang's code for gathering attribute tooltip lines
     * made to specifically reflect the behaviour of the items of this interface.
     * </p>
     * <p>
     * Handles custom default attribute modifiers, their styles, optimizes the traversal
     * of the modifier map.
     * </p>
     *
     * @param self   {@link ItemStack} provided from the {@link ItemStack#getTooltipLines(Player, TooltipFlag)} via
     *               {@link xyz.nikgub.incandescent.mixin.ItemStackMixin#getTooltipLinesMixinHead(Player, TooltipFlag, CallbackInfoReturnable)}
     * @param player {@link Player} rendering the tooltip
     * @return {@link List} of {@link Component} to be added into tooltip lines list.
     */
    default List<Component> composeAttributeModifierLines (@NotNull final ItemStack self, final Player player)
    {
        final List<Component> list = new ArrayList<>();
        for (EquipmentSlot equipmentslot : EquipmentSlot.values())
        {
            final Multimap<Attribute, AttributeModifier> modifiers = self.getAttributeModifiers(equipmentslot);
            if (modifiers.isEmpty())
            {
                continue;
            }
            list.add(CommonComponents.EMPTY);
            list.add(Component.translatable("item.modifiers." + equipmentslot.getName()).withStyle(ChatFormatting.GRAY));
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries())
            {
                final Attribute attribute = entry.getKey();
                final AttributeModifier attributeModifier = entry.getValue();
                double initialAmount = attributeModifier.getAmount();
                boolean isDefaultAttributeFlag = false;
                Style style = this.defaultStyle(self);
                final Hypermap<Attribute, UUID, Style> defaultAttributeStylesHypermap = this.getDefaultAttributesStyles(self);
                final Optional<Style> optionalStyle = defaultAttributeStylesHypermap.get(attribute, attributeModifier.getId());
                if (optionalStyle.isPresent() && player != null)
                {
                    initialAmount += player.getAttributeBaseValue(attribute);
                    initialAmount += this.getAdditionalPlayerBonus(self, player, attribute);
                    style = optionalStyle.get();
                    isDefaultAttributeFlag = true;
                }
                final double displayValue = getDisplayValue(initialAmount, attribute, attributeModifier);
                final String formatValueString = ATTRIBUTE_MODIFIER_FORMAT.format(displayValue);
                final int modifierValue = attributeModifier.getOperation().toValue();
                final String attributeDescriptionId = attribute.getDescriptionId();
                if (isDefaultAttributeFlag)
                {
                    list.add(CommonComponents.space().append(Component.translatable("attribute.modifier.equals." + modifierValue, formatValueString, Component.translatable(attributeDescriptionId))).withStyle(style));
                } else if (initialAmount > 0.0D)
                {
                    list.add(Component.translatable("attribute.modifier.plus." + modifierValue, formatValueString, Component.translatable(attributeDescriptionId)).withStyle(ChatFormatting.BLUE));
                } else if (initialAmount < 0.0D)
                {
                    list.add(Component.translatable("attribute.modifier.take." + modifierValue, formatValueString, Component.translatable(attributeDescriptionId)).withStyle(ChatFormatting.RED));
                }
            }
        }
        return list;
    }

    /**
     * Helper method to mutate modifier value that will be shown in the tooltip.
     * This is done right before displaying, and should be the final modification applied.
     *
     * @param initialAmount     {@code double} initial value of the attribute.
     * @param attribute         {@link Attribute} of the entry.
     * @param attributeModifier {@link AttributeModifier} of the entry.
     * @return {@code double} mutated display value.
     * @apiNote Kept private to reduce the room for erroneous modifications, use
     * {@link #getAdditionalPlayerBonus(ItemStack, Player, Attribute)} for general value mutation,
     * otherwise feel free to mixin if you need to.
     */
    private static double getDisplayValue (final double initialAmount, final Attribute attribute, final AttributeModifier attributeModifier)
    {
        if (attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
        {
            return initialAmount * 100.0D;
        }
        if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE))
        {
            return initialAmount * 10.0D;
        }
        if (initialAmount < 0.0D)
        {
            return initialAmount * -1.0D;
        }
        return initialAmount;
    }
}
