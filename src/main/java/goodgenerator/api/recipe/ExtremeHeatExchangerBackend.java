package goodgenerator.api.recipe;

import javax.annotation.ParametersAreNonnullByDefault;

import goodgenerator.util.MyRecipeAdder;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBackendPropertiesBuilder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtremeHeatExchangerBackend extends RecipeMapBackend {

    public ExtremeHeatExchangerBackend(RecipeMapBackendPropertiesBuilder propertiesBuilder) {
        super(propertiesBuilder);
    }

    @Override
    public GT_Recipe compileRecipe(GT_Recipe recipe) {
        if (!(recipe instanceof MyRecipeAdder.ExtremeHeatExchangerRecipe eheRecipe)) {
            throw new RuntimeException("Recipe must be instance of ExtremeHeatExchangerRecipe");
        }
        MyRecipeAdder.mXHeatExchangerFuelMap.put(recipe.mFluidInputs[0].getFluid(), eheRecipe);
        return super.compileRecipe(recipe);
    }
}
