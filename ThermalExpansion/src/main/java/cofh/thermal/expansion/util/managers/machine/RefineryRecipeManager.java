package cofh.thermal.expansion.util.managers.machine;

import cofh.core.fluid.FluidStackHolder;
import cofh.core.fluid.IFluidStackAccess;
import cofh.core.inventory.FalseIInventory;
import cofh.core.inventory.IItemStackAccess;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.core.util.IThermalInventory;
import cofh.thermal.core.util.managers.AbstractManager;
import cofh.thermal.core.util.managers.IRecipeManager;
import cofh.thermal.core.util.recipes.ThermalRecipe;
import cofh.thermal.core.util.recipes.internal.IMachineRecipe;
import cofh.thermal.core.util.recipes.internal.SimpleMachineRecipe;
import cofh.thermal.expansion.init.TExpRecipeTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RefineryRecipeManager extends AbstractManager implements IRecipeManager {

    private static final RefineryRecipeManager INSTANCE = new RefineryRecipeManager();
    protected static final int DEFAULT_ENERGY = 8000;

    protected Map<Integer, IMachineRecipe> recipeMap = new Object2ObjectOpenHashMap<>();

    protected int maxOutputItems;
    protected int maxOutputFluids;

    public static RefineryRecipeManager instance() {

        return INSTANCE;
    }

    private RefineryRecipeManager() {

        super(DEFAULT_ENERGY);
        this.maxOutputItems = 1;
        this.maxOutputFluids = 2;
    }

    public void addRecipe(ThermalRecipe recipe) {

        addRecipe(recipe.getEnergy(), recipe.getExperience(), recipe.getMinPower(), recipe.getMaxPower(), Collections.emptyList(), recipe.getInputFluids(), recipe.getOutputItems(), recipe.getOutputItemChances(), recipe.getOutputFluids());
    }

    public boolean validRecipe(FluidStack input) {

        return getRecipe(input) != null;
    }

    protected void clear() {

        recipeMap.clear();
    }

    protected IMachineRecipe getRecipe(FluidStack input) {

        return getRecipe(Collections.emptyList(), Collections.singletonList(new FluidStackHolder(input)));
    }

    protected IMachineRecipe getRecipe(List<? extends IItemStackAccess> inputSlots, List<? extends IFluidStackAccess> inputTanks) {

        if (inputTanks.isEmpty() || inputTanks.get(0).isEmpty()) {
            return null;
        }
        FluidStack inputFluid = inputTanks.get(0).getFluidStack();
        return recipeMap.get(FluidHelper.fluidHashcode(inputFluid));
    }

    protected IMachineRecipe addRecipe(int energy, float experience, int minPower, int maxPower, List<ItemStack> inputItems, List<FluidStack> inputFluids, List<ItemStack> outputItems, List<Float> chance, List<FluidStack> outputFluids) {

        if (inputFluids.isEmpty() || outputItems.isEmpty() && outputFluids.isEmpty() || outputItems.size() > maxOutputItems || outputFluids.size() > maxOutputFluids || energy <= 0) {
            return null;
        }
        FluidStack input = inputFluids.get(0);
        if (input.isEmpty()) {
            return null;
        }
        for (ItemStack stack : outputItems) {
            if (stack.isEmpty()) {
                return null;
            }
        }
        for (FluidStack stack : outputFluids) {
            if (stack.isEmpty()) {
                return null;
            }
        }
        energy = (int) (energy * getDefaultScale());

        SimpleMachineRecipe recipe = new SimpleMachineRecipe(energy, experience, minPower, maxPower, inputItems, inputFluids, outputItems, chance, outputFluids);
        recipeMap.put(FluidHelper.fluidHashcode(input), recipe);
        return recipe;
    }

    // region IRecipeManager
    @Override
    public IMachineRecipe getRecipe(IThermalInventory inventory) {

        return getRecipe(inventory.inputSlots(), inventory.inputTanks());
    }

    @Override
    public List<IMachineRecipe> getRecipeList() {

        return new ArrayList<>(recipeMap.values());
    }
    // endregion

    // region IManager
    @Override
    public void config() {

    }

    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        Map<ResourceLocation, IRecipe<FalseIInventory>> recipes = recipeManager.getRecipes(TExpRecipeTypes.RECIPE_REFINERY);
        for (Map.Entry<ResourceLocation, IRecipe<FalseIInventory>> entry : recipes.entrySet()) {
            addRecipe((ThermalRecipe) entry.getValue());
        }
    }
    // endregion
}
