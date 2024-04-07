package com.strangesmell.noguifd;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import vectorwing.farmersdelight.client.gui.CookingPotTooltip;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.Optional;

public class NGCookingPotItem extends BlockItem {
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public NGCookingPotItem(Block block, Properties properties) {
        super(block, properties);
    }

    public boolean isBarVisible(ItemStack stack) {
        return getServingCount(stack) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getServingCount(stack) / 64, 13);
    }

    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemStack mealStack = NGCookingPotBlockEntity.getMealFromItem(stack);
        return Optional.of(new CookingPotTooltip.CookingPotTooltipComponent(mealStack));
    }

    private static int getServingCount(ItemStack stack) {
        CompoundTag nbt = stack.getTagElement("BlockEntityTag");
        if (nbt == null) {
            return 0;
        } else {
            ItemStack mealStack = NGCookingPotBlockEntity.getMealFromItem(stack);
            return mealStack.getCount();
        }
    }
}
