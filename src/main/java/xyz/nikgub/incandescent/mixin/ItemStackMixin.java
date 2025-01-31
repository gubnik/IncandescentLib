/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2024, nikgub_

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

package xyz.nikgub.incandescent.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.item_interfaces.IGradientNameItem;
import xyz.nikgub.incandescent.item_interfaces.INotStupidTooltipItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;
import static net.minecraft.world.item.ItemStack.appendEnchantmentNames;

@SuppressWarnings("all")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements net.minecraftforge.common.extensions.IForgeItemStack
{

    @Shadow
    private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
    @Shadow
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);

    @Shadow
    public abstract CompoundTag getTagElement (String string);

    @Shadow
    public abstract Item getItem ();

    @Shadow public abstract boolean is (Item pItem);

    @Inject(method = "getHoverName", at = @At("HEAD"), cancellable = true)
    public void getHoverNameMixinHead (CallbackInfoReturnable<Component> retVal)
    {
        ItemStack self = (ItemStack) (Object) this;
        Function<Integer, Integer> colorFunction;
        if (self.getItem() instanceof IGradientNameItem iGradientNameItem)
        {
            if (!(iGradientNameItem.getGradientCondition(self))) return;
            colorFunction = iGradientNameItem.getGradientFunction(self);
        } else return;
        CompoundTag compoundtag = this.getTagElement("display");
        if (compoundtag != null && compoundtag.contains("Name", 8))
        {
            try
            {
                MutableComponent component = Component.Serializer.fromJson(compoundtag.getString("Name"));
                if (component != null)
                {
                    component = component.withStyle(component.getStyle().withColor(colorFunction.apply((Incandescent.clientTick))));
                    retVal.setReturnValue(component);
                }
                compoundtag.remove("Name");
            } catch (Exception exception)
            {
                compoundtag.remove("Name");
            }
        }
        MutableComponent defaultComponent = this.getItem().getName(self).copy();
        defaultComponent = defaultComponent.withStyle(defaultComponent.getStyle().withColor(colorFunction.apply(Incandescent.clientTick)));
        retVal.setReturnValue(defaultComponent);
    }

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lcom/google/common/collect/Multimap;"), cancellable = true)
    public void getTooltipLinesMixinInvoke (@Nullable Player player, TooltipFlag flag1, CallbackInfoReturnable<List<Component>> retVal)
    {

    }

    /**
     * <h1>What the fuck is this?</h1>
     * <p>This is a <b>VERY</b> bad solution for a very stupid problem.</p>
     * <p>Generally, this injected method is duplicating the functionality of the original method but changes
     * the part about enchantment tooltips so that {@link INotStupidTooltipItem} functionality could be implemented</p>
     * <p>If you have a better idea on how to make this work - please tell me, for now this should do because of conditional
     * restrictions I put in it, casting objects C-style is generally one of faster operations so this condition shouldn't
     * be too heavy on client</p>
     * TODO: a better code
     *
     * @param player See original method's parameters
     * @param flag1  See original method's parameters
     * @param retVal Mixin callback information
     */
    @Inject(method = "getTooltipLines", at = @At("HEAD"), cancellable = true)
    public void getTooltipLinesMixinHead (@Nullable Player player, TooltipFlag flag1, CallbackInfoReturnable<List<Component>> retVal)
    {
        if (!(((ItemStack) (Object) this).getItem() instanceof INotStupidTooltipItem notStupidTooltipItem)) return;
        ItemStack self = (ItemStack) (Object) this;
        List<Component> list = Lists.newArrayList();
        MutableComponent mutablecomponent = Component.empty().append(self.getHoverName()).withStyle(self.getRarity().getStyleModifier());
        if (self.hasCustomHoverName()) mutablecomponent.withStyle(ChatFormatting.ITALIC);

        list.add(mutablecomponent);
        if (!flag1.isAdvanced() && !self.hasCustomHoverName() && self.is(Items.FILLED_MAP))
        {
            Integer integer = MapItem.getMapId(self);
            if (integer != null) list.add(Component.literal("#" + integer).withStyle(ChatFormatting.GRAY));
        }

        int j = this.getHideFlags();
        if (shouldShowInTooltip(j, ItemStack.TooltipPart.ADDITIONAL))
            this.getItem().appendHoverText(self, player == null ? null : player.level(), list, flag1);

        if (self.hasTag())
        {
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.UPGRADES) && player != null)
                ArmorTrim.appendUpgradeHoverText(self, player.level().registryAccess(), list);
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.ENCHANTMENTS))
                appendEnchantmentNames(list, self.getEnchantmentTags());

            if (self.getOrCreateTag().contains("display", 10))
            {
                CompoundTag compoundtag = self.getOrCreateTag().getCompound("display");
                if (shouldShowInTooltip(j, ItemStack.TooltipPart.DYE) && compoundtag.contains("color", 99))
                {
                    if (flag1.isAdvanced())
                        list.add(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", compoundtag.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    else
                        list.add(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }

                if (compoundtag.getTagType("Lore") == 9)
                {
                    ListTag listtag = compoundtag.getList("Lore", 8);
                    for (int i = 0; i < listtag.size(); ++i)
                    {
                        String s = listtag.getString(i);
                        try
                        {
                            MutableComponent mutablecomponent1 = Component.Serializer.fromJson(s);
                            if (mutablecomponent1 != null)
                                list.add(ComponentUtils.mergeStyles(mutablecomponent1, LORE_STYLE));
                        } catch (Exception exception)
                        {
                            compoundtag.remove("Lore");
                        }
                    }
                }
            }
        }
        // the important part
        if (shouldShowInTooltip(j, ItemStack.TooltipPart.MODIFIERS))
        {
            for (EquipmentSlot equipmentslot : EquipmentSlot.values())
            {
                Multimap<Attribute, AttributeModifier> multimap = self.getAttributeModifiers(equipmentslot);
                if (!multimap.isEmpty())
                {
                    list.add(CommonComponents.EMPTY);
                    list.add(Component.translatable("item.modifiers." + equipmentslot.getName()).withStyle(ChatFormatting.GRAY));

                    for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries())
                    {
                        AttributeModifier attributemodifier = entry.getValue();
                        double d0 = attributemodifier.getAmount();
                        boolean flag = false;
                        Style style = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
                        if (player != null)
                        {
                            // vvv Custom behaviour vvv
                            Map<Attribute, Pair<UUID, Style>> special = notStupidTooltipItem.specialColoredUUID(self);
                            for (Attribute attribute : special.keySet())
                            {
                                if (attributemodifier.getId() == special.get(attribute).getFirst())
                                {
                                    d0 += player.getAttributeValue(attribute);
                                    d0 += notStupidTooltipItem.getAdditionalPlayerBonus(self).apply(player, attribute);
                                    style = special.get(attribute).getSecond();
                                    flag = true;
                                }
                            }
                            // ^^^ Custom behaviour ^^^
                            // vvv Default behaviour vvv
                            if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID)
                            {
                                d0 += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                                d0 += EnchantmentHelper.getDamageBonus(self, MobType.UNDEFINED);
                                flag = true;
                            } else if (attributemodifier.getId() == Item.BASE_ATTACK_SPEED_UUID)
                            {
                                d0 += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                                flag = true;
                            }
                            // ^^^ Default behaviour ^^^
                        }
                        double d1;
                        if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
                        {
                            if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) d1 = d0 * 10.0D;
                            else d1 = d0;
                        } else d1 = d0 * 100.0D;
                        if (flag)
                            list.add(CommonComponents.space().append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().getDescriptionId()))).withStyle(style));
                        else if (d0 > 0.0D)
                            list.add(Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        else if (d0 < 0.0D)
                        {
                            d1 *= -1.0D;
                            list.add(Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }
        // the important part ends there
        if (self.hasTag())
        {
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.UNBREAKABLE) && self.getOrCreateTag().getBoolean("Unbreakable"))
                list.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.CAN_DESTROY) && self.getOrCreateTag().contains("CanDestroy", 9))
            {
                ListTag listtag1 = self.getOrCreateTag().getList("CanDestroy", 8);
                if (!listtag1.isEmpty())
                {
                    list.add(CommonComponents.EMPTY);
                    list.add(Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY));
                    for (int k = 0; k < listtag1.size(); ++k)
                    {
                        list.addAll(expandBlockState(listtag1.getString(k)));
                    }
                }
            }
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.CAN_PLACE) && self.getOrCreateTag().contains("CanPlaceOn", 9))
            {
                ListTag listtag2 = self.getOrCreateTag().getList("CanPlaceOn", 8);
                if (!listtag2.isEmpty())
                {
                    list.add(CommonComponents.EMPTY);
                    list.add(Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY));
                    for (int l = 0; l < listtag2.size(); ++l)
                    {
                        list.addAll(expandBlockState(listtag2.getString(l)));
                    }
                }
            }
        }
        if (flag1.isAdvanced())
        {
            if (self.isDamaged())
                list.add(Component.translatable("item.durability", self.getMaxDamage() - self.getDamageValue(), self.getMaxDamage()));
            list.add(Component.literal(ForgeRegistries.ITEMS.getKey(self.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (self.hasTag())
                list.add(Component.translatable("item.nbt_tags", self.getOrCreateTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (player != null && !this.getItem().isEnabled(player.level().enabledFeatures()))
            list.add(DISABLED_ITEM_TOOLTIP);

        net.minecraftforge.event.ForgeEventFactory.onItemTooltip(self, player, list, flag1);
        retVal.setReturnValue(list);
        retVal.cancel();
    }

    @Shadow
    private static boolean shouldShowInTooltip (int p_41627_, ItemStack.TooltipPart p_41628_)
    {
        return (p_41627_ & p_41628_.getMask()) == 0;
    }

    @Shadow
    public abstract int getHideFlags ();

    @Shadow
    private static Collection<Component> expandBlockState (String p_41762_)
    {
        try
        {
            return BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), p_41762_, true).map(
                (p_220162_) -> Lists.newArrayList(p_220162_.blockState()
                    .getBlock()
                    .getName()
                    .withStyle(ChatFormatting.DARK_GRAY)), (p_220164_) -> p_220164_.tag().stream().map((p_220172_) -> p_220172_.value().getName().withStyle(ChatFormatting.DARK_GRAY)).collect(Collectors.toList()));
        } catch (CommandSyntaxException commandsyntaxexception)
        {
            return Lists.newArrayList(Component.literal("missingno").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
