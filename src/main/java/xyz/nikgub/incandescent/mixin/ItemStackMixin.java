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

package xyz.nikgub.incandescent.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikgub.incandescent.Incandescent;
import xyz.nikgub.incandescent.common.item_interfaces.IBetterAttributeTooltipItem;
import xyz.nikgub.incandescent.common.item_interfaces.IGradientNameItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

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
    public abstract int getHideFlags ();

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    public void modifyReturnResult (CallbackInfoReturnable<Component> retVal)
    {
        final ItemStack self = (ItemStack) (Object) this;
        if (!(self.getItem() instanceof IGradientNameItem iGradientNameItem))
        {
            return;
        }
        if (!(iGradientNameItem.getGradientCondition(self))) return;
        final Function<Integer, Integer> colorFunction = iGradientNameItem.getGradientFunction(self);
        Component component = retVal.getReturnValue();
        retVal.setReturnValue(component.copy().withStyle(component.getStyle().withColor(colorFunction.apply((Incandescent.clientTick)))));
    }

    @Inject(method = "getTooltipLines", at = @At("HEAD"), cancellable = true)
    public void getTooltipLinesMixinHead (@Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir)
    {
        final ItemStack self = (ItemStack) (Object) this;
        if (!(((ItemStack) (Object) this).getItem() instanceof IBetterAttributeTooltipItem tooltipHandlingItem))
        {
            return;
        }
        final List<Component> list = new ArrayList<>();
        final MutableComponent itemName = Component.empty().append(self.getHoverName()).withStyle(self.getRarity().getStyleModifier());
        if (self.hasCustomHoverName())
        {
            itemName.withStyle(ChatFormatting.ITALIC);
        }
        list.add(itemName);
        final int hideBitMask = this.getHideFlags();
        if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.ADDITIONAL))
        {
            self.getItem().appendHoverText(self, player == null ? null : player.level(), list, tooltipFlag);
        }

        if (self.hasTag())
        {
            if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.UPGRADES) && player != null)
            {
                ArmorTrim.appendUpgradeHoverText(self, player.level().registryAccess(), list);
            }
            if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.ENCHANTMENTS))
            {
                appendEnchantmentNames(list, self.getEnchantmentTags());
            }
            if (self.getOrCreateTag().contains("display", 10))
            {
                CompoundTag compoundtag = self.getOrCreateTag().getCompound("display");
                if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.DYE) && compoundtag.contains("color", 99))
                {
                    if (tooltipFlag.isAdvanced())
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
        if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.MODIFIERS))
        {
            list.addAll(tooltipHandlingItem.composeAttributeModifierLines(self, player));
        }
        if (self.hasTag())
        {
            if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.UNBREAKABLE) && self.getOrCreateTag().getBoolean("Unbreakable"))
            {
                list.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
            }
            if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.CAN_DESTROY) && self.getOrCreateTag().contains("CanDestroy", 9))
            {
                ListTag listtag1 = self.getOrCreateTag().getList("CanDestroy", 8);
                if (!listtag1.isEmpty())
                {
                    list.add(CommonComponents.EMPTY);
                    list.add(Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY));
                    for (int k = 0; k < listtag1.size(); ++k)
                    {
                        list.addAll(ItemStack.expandBlockState(listtag1.getString(k)));
                    }
                }
            }
            if (ItemStack.shouldShowInTooltip(hideBitMask, ItemStack.TooltipPart.CAN_PLACE) && self.getOrCreateTag().contains("CanPlaceOn", 9))
            {
                ListTag listtag2 = self.getOrCreateTag().getList("CanPlaceOn", 8);
                if (!listtag2.isEmpty())
                {
                    list.add(CommonComponents.EMPTY);
                    list.add(Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY));
                    for (int l = 0; l < listtag2.size(); ++l)
                    {
                        list.addAll(ItemStack.expandBlockState(listtag2.getString(l)));
                    }
                }
            }
        }
        if (tooltipFlag.isAdvanced())
        {
            if (self.isDamaged())
                list.add(Component.translatable("item.durability", self.getMaxDamage() - self.getDamageValue(), self.getMaxDamage()));
            list.add(Component.literal(ForgeRegistries.ITEMS.getKey(self.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (self.hasTag())
                list.add(Component.translatable("item.nbt_tags", self.getOrCreateTag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (player != null && !self.getItem().isEnabled(player.level().enabledFeatures()))
        {
            list.add(DISABLED_ITEM_TOOLTIP);
        }
        net.minecraftforge.event.ForgeEventFactory.onItemTooltip(self, player, list, tooltipFlag);
        cir.setReturnValue(list);
        cir.cancel();
    }
}
