package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.container.BaseContainer;
import com.raoulvdberge.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.raoulvdberge.refinedstorage.screen.BaseScreen;
import com.raoulvdberge.refinedstorage.screen.grid.GridScreen;
import com.raoulvdberge.refinedstorage.util.RenderUtils;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class GuiContainerHandler implements IGuiContainerHandler<BaseScreen> {
    @Nullable
    @Override
    public Object getIngredientUnderMouse(BaseScreen screen, double mouseX, double mouseY) {
        mouseX -= screen.getGuiLeft();
        mouseY -= screen.getGuiTop();

        if (screen instanceof GridScreen) {
            GridScreen grid = (GridScreen) screen;

            if (!grid.getSearchField().isFocused() && grid.isOverSlotArea(mouseX, mouseY)) {
                return grid.getSlotNumber() >= 0 && grid.getSlotNumber() < grid.getView().getStacks().size() ? grid.getView().getStacks().get(grid.getSlotNumber()).getIngredient() : null;
            }
        }

        if (screen.getContainer() instanceof BaseContainer) {
            for (FluidFilterSlot slot : ((BaseContainer) screen.getContainer()).getFluidSlots()) {
                FluidStack fluidInSlot = slot.getFluidInventory().getFluid(slot.getSlotIndex());

                if (!fluidInSlot.isEmpty() && RenderUtils.inBounds(slot.xPos, slot.yPos, 18, 18, mouseX, mouseY)) {
                    return fluidInSlot;
                }
            }
        }

        return null;
    }
}
