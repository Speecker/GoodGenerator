package goodgenerator.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import goodgenerator.api.recipe.ComponentAssemblyLineFrontend;
import goodgenerator.api.recipe.ExtremeHeatExchangerBackend;
import goodgenerator.api.recipe.ExtremeHeatExchangerFrontend;
import goodgenerator.api.recipe.NaquadahReactorFrontend;
import goodgenerator.api.recipe.NeutronActivatorFrontend;
import goodgenerator.api.recipe.PreciseAssemblerFrontend;
import gregtech.api.enums.GT_Values;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.nei.formatter.SimpleSpecialValueFormatter;

public class MyRecipeAdder {

    public static final MyRecipeAdder instance = new MyRecipeAdder();

    public final RecipeMap<RecipeMapBackend> NqGFuels = RecipeMapBuilder.of("gg.recipe.naquadah_reactor")
            .maxIO(0, 0, 1, 1).minInputs(0, 1)
            .neiSpecialInfoFormatter(new SimpleSpecialValueFormatter("value.naquadah_reactor"))
            .neiRecipeComparator(Comparator.comparing(recipe -> recipe.mSpecialValue))
            .frontend(NaquadahReactorFrontend::new).build();
    public final RecipeMap<RecipeMapBackend> FRF = RecipeMapBuilder.of("gg.recipe.naquadah_fuel_refine_factory")
            .maxIO(6, 0, 2, 1).minInputs(0, 1)
            .neiSpecialInfoFormatter(new SimpleSpecialValueFormatter("value.naquadah_fuel_refine_factory")).build();
    public final RecipeMap<?> NA = RecipeMapBuilder.of("gg.recipe.neutron_activator").maxIO(9, 9, 1, 1)
            .neiSpecialInfoFormatter(recipeInfo -> {
                int minNKE = recipeInfo.recipe.mSpecialValue % 10000;
                int maxNKE = recipeInfo.recipe.mSpecialValue / 10000;
                return Arrays.asList(
                        StatCollector.translateToLocal("value.neutron_activator.0"),
                        GT_Utility.formatNumbers(minNKE) + StatCollector.translateToLocal("value.neutron_activator.2"),
                        StatCollector.translateToLocal("value.neutron_activator.1"),
                        GT_Utility.formatNumbers(maxNKE) + StatCollector.translateToLocal("value.neutron_activator.2"));
            }).frontend(NeutronActivatorFrontend::new).build();
    public final RecipeMap<ExtremeHeatExchangerBackend> XHE = RecipeMapBuilder
            .of("gg.recipe.extreme_heat_exchanger", ExtremeHeatExchangerBackend::new).maxIO(0, 0, 2, 3)
            .frontend(ExtremeHeatExchangerFrontend::new).build();
    public final RecipeMap<RecipeMapBackend> PA = RecipeMapBuilder.of("gg.recipe.precise_assembler").maxIO(4, 1, 4, 0)
            .progressBar(GT_UITextures.PROGRESSBAR_ARROW_MULTIPLE).progressBarPos(85, 30)
            .neiTransferRect(80, 30, 35, 18)
            .neiSpecialInfoFormatter(new SimpleSpecialValueFormatter("value.precise_assembler"))
            .frontend(PreciseAssemblerFrontend::new).build();
    public final RecipeMap<RecipeMapBackend> COMPASSLINE_RECIPES = RecipeMapBuilder
            .of("gg.recipe.componentassemblyline").maxIO(12, 1, 12, 0).neiTransferRect(70, 15, 18, 54)
            .neiSpecialInfoFormatter(
                    recipeInfo -> Collections.singletonList(
                            StatCollector.translateToLocalFormatted(
                                    "value.component_assembly_line",
                                    GT_Values.VN[recipeInfo.recipe.mSpecialValue])))
            .frontend(ComponentAssemblyLineFrontend::new).build();

    public void addLiquidMentalFuel(FluidStack input, FluidStack output, int EUt, int ticks) {
        NqGFuels.addRecipe(
                true,
                null,
                null,
                null,
                new FluidStack[] { input },
                new FluidStack[] { output },
                ticks,
                0,
                EUt);
    }

    public void addNaquadahFuelRefineRecipe(FluidStack[] input1, ItemStack[] input2, FluidStack output, int EUt,
            int ticks, int tier) {
        FRF.addRecipe(false, input2, null, null, input1, new FluidStack[] { output }, ticks, EUt, tier);
    }

    public void addNeutronActivatorRecipe(FluidStack[] input1, ItemStack[] input2, FluidStack[] output1,
            ItemStack[] output2, int ticks, int maxNKE, int minNKE) {
        if (maxNKE <= 0) maxNKE = 1;
        if (maxNKE >= 1100) maxNKE = 1100;
        if (minNKE < 0) minNKE = 0;
        if (minNKE >= maxNKE) minNKE = maxNKE - 1;
        NA.addRecipe(false, input2, output2, null, input1, output1, ticks, 0, maxNKE * 10000 + minNKE);
    }

    public static HashMap<Fluid, ExtremeHeatExchangerRecipe> mXHeatExchangerFuelMap = new HashMap<>();

    public static class ExtremeHeatExchangerRecipe extends GT_Recipe {

        public ExtremeHeatExchangerRecipe(FluidStack[] input, FluidStack[] output, int special) {
            super(false, null, null, null, null, input, output, 0, 0, special);
        }

        public int getMaxHotFluidConsume() {
            if (this.mFluidInputs != null) {
                return this.mFluidInputs[0].amount;
            }
            return 0;
        }

        public Fluid getNormalSteam() {
            if (this.mFluidOutputs != null) {
                return this.mFluidOutputs[0].getFluid();
            }
            return null;
        }

        public Fluid getHeatedSteam() {
            if (this.mFluidOutputs != null) {
                return this.mFluidOutputs[1].getFluid();
            }
            return null;
        }

        public Fluid getCooledFluid() {
            if (this.mFluidOutputs != null) {
                return this.mFluidOutputs[2].getFluid();
            }
            return null;
        }

        public int getEUt() {
            if (getNormalSteam() != null) {
                switch (getNormalSteam().getName()) {
                    case "steam": {
                        int tVal = this.mFluidInputs[1].amount * 4;
                        if (tVal < 0) tVal = -tVal;
                        return tVal;
                    }
                    case "ic2superheatedsteam": {
                        int tVal = this.mFluidInputs[1].amount * 8;
                        if (tVal < 0) tVal = -tVal;
                        return tVal;
                    }
                    case "supercriticalsteam": {
                        int tVal = this.mFluidInputs[1].amount * 800;
                        if (tVal < 0) tVal = -tVal;
                        return tVal;
                    }
                    default:
                        return 0;
                }
            }
            return 0;
        }
    }

    public void addExtremeHeatExchangerRecipe(FluidStack HotFluid, FluidStack ColdFluid, FluidStack WorkFluid,
            FluidStack HeatedWorkFluid, FluidStack OverHeatedWorkFluid, int Threshold) {
        XHE.addRecipe(
                new ExtremeHeatExchangerRecipe(
                        new FluidStack[] { HotFluid, WorkFluid },
                        new FluidStack[] { HeatedWorkFluid, OverHeatedWorkFluid, ColdFluid },
                        Threshold));
    }

    public void addPreciseAssemblerRecipe(ItemStack[] aItemInputs, FluidStack[] aFluidInputs, ItemStack aOutput,
            int aEUt, int aDuration, int aTier) {
        if (aOutput == null) return;
        PA.addRecipe(
                false,
                aItemInputs,
                new ItemStack[] { aOutput },
                null,
                null,
                aFluidInputs,
                null,
                aDuration,
                aEUt,
                aTier);
    }

    public GT_Recipe addComponentAssemblyLineRecipe(ItemStack[] ItemInputArray, FluidStack[] FluidInputArray,
            ItemStack OutputItem, int aDuration, int aEUt, int casingLevel) {
        return COMPASSLINE_RECIPES.addRecipe(
                false,
                ItemInputArray,
                new ItemStack[] { OutputItem },
                null,
                FluidInputArray,
                null,
                aDuration,
                aEUt,
                casingLevel);
    }
}
