package cofh.thermal.core.util.managers.device;

import cofh.core.inventory.FalseIInventory;
import cofh.thermal.core.init.TCoreRecipeTypes;
import cofh.thermal.core.util.managers.AbstractManager;
import cofh.thermal.core.util.recipes.device.RockGenMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;

public class RockGenManager extends AbstractManager {

    private static final RockGenManager INSTANCE = new RockGenManager();
    protected static final RockGenRecipe DEFAULT_RECIPE = new RockGenRecipe(instance().getDefaultEnergy(), ItemStack.EMPTY);

    protected Map<Block, Map<Block, RockGenRecipe>> recipeMap = new IdentityHashMap<>();
    protected Map<Block, RockGenRecipe> defaultRecipeMap = new IdentityHashMap<>();

    protected RockGenManager() {

        super(20);
    }

    public static RockGenManager instance() {

        return INSTANCE;
    }

    protected void clear() {

        recipeMap.clear();
        defaultRecipeMap.clear();
    }

    // region MAPPINGS
    public RockGenRecipe getResult(Block below, Block[] adjacent) {

        Map<Block, RockGenRecipe> baseMap = recipeMap.get(below);

        if (baseMap != null) {
            for (Block adj : adjacent) {
                if (baseMap.containsKey(adj)) {
                    return baseMap.getOrDefault(adj, DEFAULT_RECIPE);
                }
            }
        }
        baseMap = defaultRecipeMap;
        for (Block adj : adjacent) {
            if (baseMap.containsKey(adj)) {
                return baseMap.getOrDefault(adj, DEFAULT_RECIPE);
            }
        }
        return DEFAULT_RECIPE;
    }

    public boolean addMapping(int time, Block below, Block adjacent, ItemStack stack) {

        if (adjacent == Blocks.AIR || stack.isEmpty()) {
            return false;
        }
        if (below == Blocks.AIR) {
            defaultRecipeMap.put(adjacent, new RockGenRecipe(time, stack));
            return true;
        }
        if (!recipeMap.containsKey(below)) {
            recipeMap.put(below, new Object2ObjectOpenHashMap<>());
        }
        recipeMap.get(below).put(adjacent, new RockGenRecipe(time, stack));
        return true;
    }

    public void addMapping(RockGenMapping mapping) {

        addMapping(mapping.getTime(), mapping.getBelow(), mapping.getAdjacent(), mapping.getResult());
    }
    // endregion

    // region IManager
    @Override
    public void config() {

    }

    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        Map<ResourceLocation, IRecipe<FalseIInventory>> mappings = recipeManager.getRecipes(TCoreRecipeTypes.MAPPING_ROCK_GEN);
        for (Map.Entry<ResourceLocation, IRecipe<FalseIInventory>> entry : mappings.entrySet()) {
            addMapping((RockGenMapping) entry.getValue());
        }
    }
    // endregion

    // region RECIPE
    public static class RockGenRecipe {

        protected final int time;
        protected final ItemStack result;

        public RockGenRecipe(int time, ItemStack result) {

            this.time = time;
            this.result = result;
        }

        public int getTime() {

            return time;

        }

        public ItemStack getResult() {

            return result;
        }

    }
    // endregion
}
