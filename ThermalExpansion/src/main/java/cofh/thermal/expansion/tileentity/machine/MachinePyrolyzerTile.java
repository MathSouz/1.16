package cofh.thermal.expansion.tileentity.machine;

import cofh.core.fluid.FluidStorageCoFH;
import cofh.core.inventory.ItemStorageCoFH;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.core.tileentity.MachineTileProcess;
import cofh.thermal.expansion.inventory.container.machine.MachinePyrolyzerContainer;
import cofh.thermal.expansion.util.managers.machine.PyrolyzerRecipeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.StorageGroup.*;
import static cofh.core.util.constants.Constants.BUCKET_VOLUME;
import static cofh.core.util.constants.Constants.TANK_SMALL;
import static cofh.thermal.core.common.ThermalConfig.machineAugments;
import static cofh.thermal.expansion.init.TExpReferences.MACHINE_PYROLYZER_TILE;

public class MachinePyrolyzerTile extends MachineTileProcess {

    protected ItemStorageCoFH inputSlot = new ItemStorageCoFH(PyrolyzerRecipeManager.instance()::validRecipe);
    protected FluidStorageCoFH outputTank = new FluidStorageCoFH(TANK_SMALL);

    public MachinePyrolyzerTile() {

        super(MACHINE_PYROLYZER_TILE);

        inventory.addSlot(inputSlot, INPUT);
        inventory.addSlots(OUTPUT, 4);
        inventory.addSlot(chargeSlot, INTERNAL);

        tankInv.addTank(outputTank, OUTPUT);

        addAugmentSlots(machineAugments);
        initHandlers();
    }

    // TODO: Adjust when recipes have min/max power.
    @Override
    protected int getBaseProcessTick() {

        return BASE_PROCESS_TICK / 4;
    }

    @Override
    protected boolean cacheRecipe() {

        curRecipe = PyrolyzerRecipeManager.instance().getRecipe(this);
        if (curRecipe != null) {
            itemInputCounts = curRecipe.getInputItemCounts(this);
        }
        return curRecipe != null;
    }

    @Override
    protected boolean cacheRenderFluid() {

        if (curRecipe == null) {
            return false;
        }
        FluidStack prevFluid = renderFluid;
        List<FluidStack> recipeOutputFluids = curRecipe.getOutputFluids(this);
        renderFluid = recipeOutputFluids.isEmpty() ? FluidStack.EMPTY : new FluidStack(recipeOutputFluids.get(0), BUCKET_VOLUME);
        return !FluidHelper.fluidsEqual(renderFluid, prevFluid);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new MachinePyrolyzerContainer(i, world, pos, inventory, player);
    }

    // region OPTIMIZATION
    @Override
    protected boolean validateInputs() {

        if (!cacheRecipe()) {
            return false;
        }
        return inputSlot.getCount() >= itemInputCounts.get(0);
    }
    // endregion
}
