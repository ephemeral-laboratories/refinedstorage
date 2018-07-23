package com.raoulvdberge.refinedstorage.network;

import com.raoulvdberge.refinedstorage.api.network.grid.GridType;
import com.raoulvdberge.refinedstorage.api.network.grid.IGrid;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeGrid;
import com.raoulvdberge.refinedstorage.container.ContainerGrid;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBase;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerFluid;
import com.raoulvdberge.refinedstorage.util.StackUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.Collection;

public class MessageGridProcessingTransfer extends MessageHandlerPlayerToServer<MessageGridProcessingTransfer> implements IMessage {
    private Collection<ItemStack> inputs;
    private Collection<ItemStack> outputs;

    private Collection<FluidStack> fluidInputs;
    private Collection<FluidStack> fluidOutputs;

    public MessageGridProcessingTransfer() {
    }

    public MessageGridProcessingTransfer(Collection<ItemStack> inputs, Collection<ItemStack> outputs, Collection<FluidStack> fluidInputs, Collection<FluidStack> fluidOutputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.fluidInputs = fluidInputs;
        this.fluidOutputs = fluidOutputs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();

        this.inputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            this.inputs.add(StackUtils.readItemStack(buf));
        }

        size = buf.readInt();

        this.outputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            this.outputs.add(StackUtils.readItemStack(buf));
        }

        size = buf.readInt();

        this.fluidInputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            buf.readInt();

            this.fluidInputs.add(StackUtils.readFluidStack(buf));
        }

        size = buf.readInt();

        this.fluidOutputs = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            buf.readInt();

            this.fluidOutputs.add(StackUtils.readFluidStack(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(inputs.size());

        for (ItemStack stack : inputs) {
            StackUtils.writeItemStack(buf, stack);
        }

        buf.writeInt(outputs.size());

        for (ItemStack stack : outputs) {
            StackUtils.writeItemStack(buf, stack);
        }

        buf.writeInt(fluidInputs.size());

        for (FluidStack stack : fluidInputs) {
            StackUtils.writeFluidStack(buf, stack);
        }

        buf.writeInt(fluidOutputs.size());

        for (FluidStack stack : fluidOutputs) {
            StackUtils.writeFluidStack(buf, stack);
        }
    }

    @Override
    public void handle(MessageGridProcessingTransfer message, EntityPlayerMP player) {
        if (player.openContainer instanceof ContainerGrid) {
            IGrid grid = ((ContainerGrid) player.openContainer).getGrid();

            if (grid.getGridType() == GridType.PATTERN) {
                ItemHandlerBase handler = ((NetworkNodeGrid) grid).getProcessingMatrix();
                ItemHandlerFluid handlerFluid = ((NetworkNodeGrid) grid).getMatrixProcessingFluids();

                clearInputsAndOutputs(handler);
                clearInputsAndOutputs(handlerFluid);

                setInputs(handler, message.inputs);
                setOutputs(handler, message.outputs);

                setFluidInputs(handlerFluid, message.fluidInputs);
                setFluidOutputs(handlerFluid, message.fluidOutputs);
            }
        }
    }

    private void clearInputsAndOutputs(ItemHandlerBase handler) {
        for (int i = 0; i < 9 * 2; ++i) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private void setInputs(ItemHandlerBase handler, Collection<ItemStack> stacks) {
        setSlots(handler, stacks, 0, 9);
    }

    private void setOutputs(ItemHandlerBase handler, Collection<ItemStack> stacks) {
        setSlots(handler, stacks, 9, 18);
    }

    private void setSlots(ItemHandlerBase handler, Collection<ItemStack> stacks, int begin, int end) {
        for (ItemStack stack : stacks) {
            handler.setStackInSlot(begin, stack);

            begin++;

            if (begin >= end) {
                break;
            }
        }
    }

    private void setFluidInputs(ItemHandlerBase handler, Collection<FluidStack> stacks) {
        setFluidSlots(handler, stacks, 0, 9);
    }

    private void setFluidOutputs(ItemHandlerBase handler, Collection<FluidStack> stacks) {
        setFluidSlots(handler, stacks, 9, 18);
    }

    private void setFluidSlots(ItemHandlerBase handler, Collection<FluidStack> stacks, int begin, int end) {
        for (FluidStack stack : stacks) {
            if (!StackUtils.hasFluidBucket(stack) || stack.amount > Fluid.BUCKET_VOLUME) {
                continue;
            }

            ItemStack filledContainer = new ItemStack(Items.BUCKET);

            IFluidHandlerItem fluidHandler = filledContainer.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

            fluidHandler.fill(StackUtils.copy(stack, Fluid.BUCKET_VOLUME), true);

            filledContainer = fluidHandler.getContainer();
            filledContainer.setCount(stack.amount);

            handler.setStackInSlot(begin, filledContainer);

            begin++;

            if (begin >= end) {
                break;
            }
        }
    }
}
