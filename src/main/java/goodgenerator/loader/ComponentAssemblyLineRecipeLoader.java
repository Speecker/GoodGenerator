package goodgenerator.loader;

import static goodgenerator.util.Log.LOGGER;

import goodgenerator.util.MyRecipeAdder;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.items.GT_IntegratedCircuit_Item;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

public class ComponentAssemblyLineRecipeLoader {
    private static final String[] compPrefixes = {
        "Electric_Motor_",
        "Electric_Piston_",
        "Electric_Pump_",
        "Robot_Arm_",
        "Conveyor_Module_",
        "Emitter_",
        "Sensor_",
        "Field_Generator_",
    };
    private static final String[] blacklistedDictPrefixes = {
        "Any", "crafting",
    };

    private static LinkedHashMap<List<GT_Recipe>, Pair<ItemList, Integer>> allAssemblerRecipes;
    private static LinkedHashMap<List<GT_Recipe.GT_Recipe_AssemblyLine>, Pair<ItemList, Integer>> allAsslineRecipes;

    public static void run() {
        findAllRecipes();
        generateAssemblerRecipes();
    }
    /** Normal assembler recipes (LV-IV) **/
    private static void generateAssemblerRecipes() {
        allAssemblerRecipes.forEach((recipeList, info) -> {
            for (GT_Recipe recipe : recipeList) {
                if (recipe != null) {
                    LOGGER.printf(Level.INFO, "RECIPE: %s", info.getLeft().name());
                    ArrayList<ItemStack> fixedInputs = new ArrayList<>();
                    ArrayList<FluidStack> fixedFluids = new ArrayList<>();
                    for (int j = 0; j < recipe.mInputs.length; j++) {
                        ItemStack input = recipe.mInputs[j];
                        int count = input.stackSize;
                        if (GT_Utility.isStackValid(input)) {
                            /*
                            if (count > 8 && OreDictionary.getOreIDs(input).length > 0) {
                                oreDictLoop:
                                for (int id : OreDictionary.getOreIDs(input)) {
                                    String dict = OreDictionary.getOreName(id);
                                    Materials mat = OrePrefixes.getMaterial(dict);
                                    if (!mat.equals(Materials._NULL)) {
                                        for (String blacklistedPrefix : blacklistedDictPrefixes) {
                                            if (dict.startsWith(blacklistedPrefix)) {
                                                LOGGER.printf(Level.INFO, "Blacklisted: %s", blacklistedPrefix);
                                                continue oreDictLoop;
                                            }
                                        }
                                        OrePrefixes prefix = null;
                                        try {
                                            prefix =
                                                OrePrefixes.valueOf(
                                                    dict.substring(0, dict.indexOf(mat.mName)));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (prefix != null) {
                                            // i can finally play the game

                                            LOGGER.printf(Level.INFO, "Input calculated! Prefix: %s, Material: %s, Full: %s", prefix, mat, dict);
                                            fixedFluids.add(mat.getMolten((prefix.mMaterialAmount / (GT_Values.M / 144)) * count));
                                        }
                                    }
                                }


                            } */

                            // Splits the input into full ItemStacks

                            if (!(input.getItem() instanceof GT_IntegratedCircuit_Item)) {
                                int newSize = count * 16;
                                ItemStack newStack;
                                if (newSize > 64) {
                                    for (int i = 0; i < newSize / 64; i++) {
                                        newStack = input.copy();
                                        newStack.stackSize = 64;
                                        fixedInputs.add(newStack);
                                    }
                                }
                                newStack = input.copy();
                                newStack.stackSize = newSize > 64 ? newSize % 64 : newSize;
                                fixedInputs.add(newStack);
                            }
                        }
                    }
                    for (int j = 0; j < recipe.mFluidInputs.length; j++) {
                        FluidStack currFluid = recipe.mFluidInputs[j].copy();
                        currFluid.amount = currFluid.amount * 16;
                        fixedFluids.add(currFluid);
                    }
                    ArrayList<String> a1 = new ArrayList<>();
                    ArrayList<String> a2 = new ArrayList<>();

                    fixedInputs.forEach(is -> a1.add(is.getDisplayName() + " x" + is.stackSize));
                    fixedFluids.forEach(fs -> a2.add(fs.getLocalizedName() + " " + fs.amount + "L"));

                    // Adding the recipes
                    LOGGER.printf(
                            Level.INFO,
                            "ADDED THE RECIPE!\n" + "Inputs: %s\n"
                                    + "Fluids: %s\n"
                                    + "Output: %s\n"
                                    + "Duration: %d\n"
                                    + "Energy: %d\n"
                                    + "Tier: %d",
                            a1,
                            a2,
                            recipe.mOutputs[0].getDisplayName(),
                            recipe.mDuration * 16,
                            recipe.mEUt * 16,
                            info.getRight());

                    MyRecipeAdder.instance.addComponentAssemblyLineRecipe(
                            fixedInputs.toArray(new ItemStack[fixedInputs.size()]),
                            fixedFluids.toArray(new FluidStack[fixedFluids.size()]),
                            info.getLeft().get(16L),
                            recipe.mDuration * 16,
                            recipe.mEUt * 16,
                            info.getRight());
                }
            }
        });
    }

    private static void findAllRecipes() {
        allAssemblerRecipes = new LinkedHashMap<>();
        allAsslineRecipes = new LinkedHashMap<>();
        for (String compPrefix : compPrefixes) {
            for (int i = 0; i <= 5; i++) {
                for (int t = 1; t <= 12; t++) {
                    String vName = GT_Values.VN[t];
                    ItemList currentComponent = ItemList.valueOf(compPrefix + vName);
                    if (currentComponent.hasBeenSet()) {
                        if (t < 6) {
                            allAssemblerRecipes.put(
                                    GT_Recipe.GT_Recipe_Map.sAssemblerRecipes.mRecipeList.stream()
                                            .filter(rec -> rec.mOutputs[0].isItemEqual(currentComponent.get(1)))
                                            .collect(Collectors.toList()),
                                    Pair.of(currentComponent, t));
                        } else {
                            allAsslineRecipes.put(
                                    GT_Recipe.GT_Recipe_AssemblyLine.sAssemblylineRecipes.stream()
                                            .filter(rec -> rec.mOutput.isItemEqual(currentComponent.get(1)))
                                            .collect(Collectors.toList()),
                                    Pair.of(currentComponent, t));
                        }
                    }
                }
            }
        }
    }
}
