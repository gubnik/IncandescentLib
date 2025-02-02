package xyz.nikgub.incandescent.item_interfaces;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * Provides easy definition of item's default attribute modifiers.
 * Default modifiers are styled similarly to how default attack damage and speed are displayed
 * on tools and weapons.
 *
 * @author Nikolay Gubankov (aka nikgub)
 */
public interface IDefaultAttributesItem
{
    /**
     * Defines mapping of attribute's UUID to the attribute.
     * Values from this method are used to recognize default attributes.
     *
     * @param itemStack {@link ItemStack} of the item.
     * @return {@link Map} of attribute's UUID to the attributes.
     */
    Map<Attribute, UUID> getDefaultAttributeModifiersUUID (ItemStack itemStack);

    /**
     * Provides an additional bonus gathered from the player.
     * This bonus only affects what number is displayed in the tooltip,
     * and is not added to the actual attribute value.
     *
     * @param itemStack {@link ItemStack} of the item.
     * @param player {@link Player} from whom to gather a bonus.
     * @param attribute {@link Attribute} for which to gather the bonus.
     * @param uuid {@link UUID} of the specific modifier for which to gather the bonus.
     * @return {@code double} bonus number
     */
    double getAdditionalPlayerBonus (ItemStack itemStack, Player player, Attribute attribute, UUID uuid);

    /**
     * Defines a style applied to the default attributes instead of the default dark green.
     * The overriding happens as the post-processing of the component list,
     * so no context of the exact attribute-UUID pair can be preserved.
     *
     * @param itemStack {@link ItemStack} of the item.
     * @param componentCopy {@link MutableComponent} of the component being modified
     * @return {@link Style} applied to the default attributes.
     */
    Style getOverridingStyle (ItemStack itemStack, MutableComponent componentCopy);
}
