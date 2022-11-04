package goodgenerator.loader;

import static goodgenerator.util.Log.LOGGER;

import goodgenerator.util.ItemRefer;
import goodgenerator.util.MyRecipeAdder;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.items.GT_IntegratedCircuit_Item;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
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
    private static final String[] blacklistedDictPrefixes = {"circuit"};
    private static final String[] softBlacklistedDictPrefixes = {"Any", "crafting"};

    private static final String[] prefixesToCompact = {"cableGt", "wireGt", "stick", "gearGtSmall"};

    private static LinkedHashMap<List<GT_Recipe>, Pair<ItemList, Integer>> allAssemblerRecipes;
    private static LinkedHashMap<List<GT_Recipe.GT_Recipe_AssemblyLine>, Pair<ItemList, Integer>> allAsslineRecipes;

    public static void run() {
        findAllRecipes();
        generateAssemblerRecipes();
        generateAsslineRecipes();
    }
    /** Normal assembler recipes (LV-IV) **/
    private static void generateAssemblerRecipes() {
        allAssemblerRecipes.forEach((recipeList, info) -> {
            for (GT_Recipe recipe : recipeList) {
                if (recipe != null) {
                    // LOGGER.printf(Level.INFO, "RECIPE: %s", info.getLeft().name());
                    ArrayList<ItemStack> fixedInputs = new ArrayList<>();
                    ArrayList<FluidStack> fixedFluids = new ArrayList<>();

                    // This is done in order to differentiate between emitter and sensor recipes. Without the circuit,
                    // both components have virtually the same recipe after the inputs are melted.
                    if (info.getLeft().name().contains("Sensor")) {
                        fixedInputs.add(GT_Utility.getIntegratedCircuit(1));
                    }
                    for (int j = 0; j < recipe.mInputs.length; j++) {
                        ItemStack input = recipe.mInputs[j];
                        if (GT_Utility.isStackValid(input) && !(input.getItem() instanceof GT_IntegratedCircuit_Item))
                            fixedInputs.addAll(multiplyAndSplitIntoStacks(input, 16));
                    }
                    for (int j = 0; j < recipe.mFluidInputs.length; j++) {
                        FluidStack currFluid = recipe.mFluidInputs[j].copy();
                        currFluid.amount = currFluid.amount * 16;
                        fixedFluids.add(currFluid);
                    }

                    int tier = info.getRight();
                    int energy = (int) Math.min(
                            Integer.MAX_VALUE - 7, ((GT_Values.V[tier] - GT_Values.V[tier > 1 ? tier - 2 : 0]) * 16));

                    MyRecipeAdder.instance.addComponentAssemblyLineRecipe(
                            fixedInputs.toArray(new ItemStack[0]),
                            fixedFluids.toArray(new FluidStack[0]),
                            info.getLeft().get(16L),
                            recipe.mDuration * 16,
                            energy,
                            info.getRight());
                }
            }
        });
    }
    /** Assembly Line Recipes (LuV+) **/
    private static void generateAsslineRecipes() {
        allAsslineRecipes.forEach((recipeList, info) -> {
            for (GT_Recipe.GT_Recipe_AssemblyLine recipe : recipeList) {
                if (recipe != null) {

                    // Arrays of the item and fluid inputs, that are updated to be multiplied and/or condensed in the
                    // following code
                    ArrayList<ItemStack> fixedInputs = new ArrayList<>();
                    ArrayList<FluidStack> fixedFluids = new ArrayList<>();

                    // This is done in order to differentiate between emitter and sensor recipes. Without the circuit,
                    // both components have virtually the same recipe after the inputs are melted.
                    if (info.getLeft().name().contains("Sensor")) {
                        fixedInputs.add(GT_Utility.getIntegratedCircuit(1));
                    }

                    // Multiplies the original fluid inputs
                    for (int j = 0; j < recipe.mFluidInputs.length; j++) {
                        FluidStack currFluid = recipe.mFluidInputs[j].copy();
                        currFluid.amount = currFluid.amount * 16;
                        fixedFluids.add(currFluid);
                    }
                    for (ItemStack input : recipe.mInputs) {
                        if (GT_Utility.isStackValid(input)) {
                            int count = input.stackSize;
                            boolean isConvertedAndFluidFound = false;
                            if (OreDictionary.getOreIDs(input).length > 0 && count > 7 && !isCompactable(input)) {
                                FluidStack foundFluidStack = tryConvertItemStackToFluidMaterial(input);
                                if (foundFluidStack != null) {
                                    boolean alreadyHasFluid = false;
                                    for (FluidStack fluidstack : fixedFluids) {
                                        if (foundFluidStack.getFluid().equals(fluidstack.getFluid())) {
                                            fluidstack.amount += foundFluidStack.amount;
                                            alreadyHasFluid = true;
                                            break;
                                        }
                                    }
                                    if (!alreadyHasFluid) {
                                        fixedFluids.add(foundFluidStack);
                                    }
                                    isConvertedAndFluidFound = true;
                                }
                            }
                            // Converts Gravi Stars to Nuclear Stars
                            if (GT_Utility.areStacksEqual(input, ItemList.Gravistar.get(count))
                                    && info.getRight() > 9) {
                                fixedInputs.add(ItemRefer.Nuclear_Star.get(count));
                            }
                            // Mulitplies the input by 16 and adjusts the stacks accordingly
                            else if (!(input.getItem() instanceof GT_IntegratedCircuit_Item)
                                    && !isConvertedAndFluidFound) {
                                ItemData data = GT_OreDictUnificator.getAssociation(input);
                                if (data != null) {
                                    if (data.mPrefix == OrePrefixes.circuit) {
                                        fixedInputs.addAll(multiplyAndSplitIntoStacks(
                                                GT_OreDictUnificator.get(data.mPrefix, data.mMaterial.mMaterial, count),
                                                16));
                                    } else fixedInputs.addAll(multiplyAndSplitIntoStacks(input, 16));
                                } else fixedInputs.addAll(multiplyAndSplitIntoStacks(input, 16));
                            }
                        }
                    }

                    fixedInputs = compactItems(fixedInputs.toArray(new ItemStack[0]));
                    GT_Recipe added = MyRecipeAdder.instance.addComponentAssemblyLineRecipe(
                            fixedInputs.toArray(new ItemStack[0]),
                            fixedFluids.toArray(new FluidStack[0]),
                            info.getLeft().get(16L),
                            recipe.mDuration,
                            recipe.mEUt,
                            info.getRight());
                }
            }
        });
    }

    private static boolean isCompactable(ItemStack toCompact) {
        ItemData data = GT_OreDictUnificator.getAssociation(toCompact);
        if (data != null) {
            for (String prefix : prefixesToCompact) {
                if (data.mPrefix == OrePrefixes.stickLong) return false;
                if (data.mPrefix.toString().startsWith(prefix)
                        || data.mPrefix == OrePrefixes.stick
                        || data.mPrefix == OrePrefixes.gearGtSmall) return true;
            }
        }
        return false;
    }

    private static FluidStack tryConvertItemStackToFluidMaterial(ItemStack input) {
        ArrayList<String> oreDicts = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(input)) {
            oreDicts.add(OreDictionary.getOreName(id));
        }
        oreDictLoop:
        for (String dict : oreDicts) {
            for (String blacklistedPrefix : blacklistedDictPrefixes) {
                if (dict.startsWith(blacklistedPrefix)) {
                    return null;
                }
            }
            for (String blacklistedPrefix : softBlacklistedDictPrefixes) {
                if (dict.startsWith(blacklistedPrefix)) {
                    continue oreDictLoop;
                }
            }
            OrePrefixes orePrefix;
            try {
                orePrefix = OrePrefixes.valueOf(findBestPrefix(dict));
            } catch (Exception e) {
                continue;
            }

            String strippedOreDict = dict.substring(orePrefix.toString().length());

            // Prevents things like AnyCopper or AnyIron from messing the search up.
            if (strippedOreDict.contains("Any")) continue;
            Fluid foundFluid = FluidRegistry.getFluid("molten." + strippedOreDict.toLowerCase());

            LOGGER.printf(
                    Level.INFO,
                    "Dict information: Prefix: %s, Stripped: %s, Fluid: %s",
                    orePrefix,
                    strippedOreDict,
                    foundFluid.getUnlocalizedName());

            return FluidRegistry.getFluidStack(
                    "molten." + strippedOreDict.toLowerCase(),
                    (int) (orePrefix.mMaterialAmount / (GT_Values.M / 144)) * input.stackSize);
        }
        return null;
    }
    /**
     *
     * @param oreDict The Ore Dictionary entry
     * @return The longest ore prefix that the OreDict string starts with. This makes it the most accurate prefix.
     */
    private static String findBestPrefix(String oreDict) {
        int longestPrefixLength = 0;
        String matchingPrefix = null;
        for (OrePrefixes prefix : OrePrefixes.values()) {
            String name = prefix.toString();
            if (oreDict.startsWith(name)) {
                if (name.length() > longestPrefixLength) {
                    longestPrefixLength = name.length();
                    matchingPrefix = name;
                }
            }
        }
        return matchingPrefix;
    }

    private static ArrayList<ItemStack> compactItems(ItemStack[] items) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (ItemStack item : items) {
            ItemData data = GT_OreDictUnificator.getItemData(item);
            if (data != null) {
                String prefixS = data.mPrefix.toString();
                if (prefixS.startsWith("cableGt")) {
                    if (item.stackSize >= 16) {
                        compactorHelper(item, data, OrePrefixes.cableGt16, stacks);
                    } else if (item.stackSize >= 4) {
                        compactorHelper(item, data, OrePrefixes.cableGt04, stacks);
                    } else stacks.add(item);
                } else if (prefixS.startsWith("wireGt")) {
                    if (item.stackSize >= 16) {
                        compactorHelper(item, data, OrePrefixes.wireGt16, stacks);
                    } else if (item.stackSize >= 4) {
                        compactorHelper(item, data, OrePrefixes.wireGt04, stacks);
                    } else stacks.add(item);
                } else if (data.mPrefix == OrePrefixes.stick && item.stackSize >= 2) {
                    compactorHelper(item, data, OrePrefixes.stickLong, stacks);
                } else if (data.mPrefix == OrePrefixes.gearGtSmall && item.stackSize >= 4) {
                    compactorHelper(item, data, OrePrefixes.gearGt, stacks);
                } else stacks.add(item);

            } else stacks.add(item);
        }
        stacks = mergeStacks(stacks);
        return stacks;
    }

    private static void compactorHelper(
            ItemStack input, ItemData data, OrePrefixes compactInto, ArrayList<ItemStack> output) {
        int materialRatio = (int) ((double) compactInto.mMaterialAmount / data.mPrefix.mMaterialAmount);
        output.addAll(
                multiplyAndSplitIntoStacks(GT_OreDictUnificator.get(compactInto, data.mMaterial.mMaterial, 1), (int)
                        ((double) input.stackSize / materialRatio)));
    }

    /**
     * Multiplies one ItemStack by a multiplier, and splits it into as many full stacks as it needs to.
     * @param stack The ItemStack you want to multiply
     * @param multiplier The number the stack is multiplied by
     * @return A List of stacks that, in total, are the same as the input ItemStack after it has been multiplied.
     */
    private static List<ItemStack> multiplyAndSplitIntoStacks(ItemStack stack, int multiplier) {
        int totalItems = stack.stackSize * multiplier;
        ArrayList<ItemStack> stacks = new ArrayList<>();
        if (totalItems >= 64) {
            for (int i = 0; i < totalItems / 64; i++) {
                stacks.add(GT_Utility.copyAmount(64, stack));
            }
        }
        if (totalItems % 64 > 0) {
            stacks.add(GT_Utility.copyAmount(totalItems % 64, stack));
        }
        return stacks;
    }

    /**
     * Searches the Assembler and Assembly line registry for all of the base component recipes.
     */
    private static void findAllRecipes() {
        allAssemblerRecipes = new LinkedHashMap<>();
        allAsslineRecipes = new LinkedHashMap<>();
        for (String compPrefix : compPrefixes) {

            for (int t = 1; t <= 12; t++) {
                String vName = GT_Values.VN[t];
                ItemList currentComponent = ItemList.valueOf(compPrefix + vName);
                LOGGER.printf(
                        Level.INFO,
                        "Current Component: %s",
                        currentComponent.get(1).getDisplayName());
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
                                        .filter(rec -> {
                                            boolean found = rec.mOutput.isItemEqual(currentComponent.get(1));
                                            if (found) LOGGER.log(Level.INFO, "Found");
                                            return found;
                                        })
                                        .collect(Collectors.toList()),
                                Pair.of(currentComponent, t));
                    }
                }
            }
        }
    }

    private static ArrayList<ItemStack> mergeStacks(List<ItemStack> stacks) {
        ArrayList<ItemStack> output = new ArrayList<>();
        for (int index = 0; index < stacks.size(); index++) {
            ItemStack i = stacks.get(index);
            boolean hasDupe = false;
            int newSize = i.stackSize;
            for (int j = index + 1; j < stacks.size(); j++) {
                ItemStack is2 = stacks.get(j);
                if (GT_Utility.areStacksEqual(i, is2)) {
                    hasDupe = true;
                    newSize += is2.stackSize;
                    stacks.remove(j);
                    j--;
                }
            }
            if (hasDupe) {
                if (newSize >= 64) {
                    for (int k = 0; k < newSize / 64; k++) {
                        output.add(GT_Utility.copyAmount(64, i));
                    }
                }
                if (newSize % 64 > 0) {
                    output.add(GT_Utility.copyAmount(newSize > 64 ? newSize % 64 : newSize, i));
                }
            } else output.add(i);
        }
        return output;
    }
}
