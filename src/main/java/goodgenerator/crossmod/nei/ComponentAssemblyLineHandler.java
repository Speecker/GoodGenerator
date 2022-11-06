package goodgenerator.crossmod.nei;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import gregtech.GT_Mod;
import gregtech.api.enums.GT_Values;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.power.EUPower;
import gregtech.common.power.Power;
import gregtech.common.power.UnspecifiedEUPower;
import gregtech.nei.GT_NEI_DefaultHandler;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;

public class ComponentAssemblyLineHandler extends GT_NEI_DefaultHandler {
    public ComponentAssemblyLineHandler(GT_Recipe.GT_Recipe_Map aRecipeMap) {

        super(aRecipeMap);
        this.transferRects.remove(new RecipeTransferRect(new Rectangle(65, 13, 36, 18), getOverlayIdentifier()));
        this.transferRects.add(new RecipeTransferRect(new Rectangle(69, 18, 9, 34), getOverlayIdentifier()));
        if (!NEI_Config.isAdded) {
            FMLInterModComms.sendRuntimeMessage(
                GT_Values.GT,
                "NEIPlugins",
                "register-crafting-handler",
                "gregtech@" + this.getRecipeName() + "@" + this.getOverlayIdentifier());
            GuiCraftingRecipe.craftinghandlers.add(this);
            GuiUsageRecipe.usagehandlers.add(this);
        }
    }
    @Override
    public TemplateRecipeHandler newInstance() {
        return new ComponentAssemblyLineHandler(this.mRecipeMap);
    }

    @Override
    public void drawExtras(int aRecipeIndex) {
        GT_Recipe recipe = ((CachedDefaultRecipe) this.arecipes.get(aRecipeIndex)).mRecipe;
        drawDescription(recipe);
    }

    // I had to do this because the original method was private, so I couldn't override it.
    private void drawDescription(GT_Recipe recipe) {
        if (mPower == null) {
            mPower = getPowerFromRecipeMap();
        }
        mPower.computePowerUsageAndDuration(recipe.mEUt, recipe.mDuration);

        int lineCounter = 0;
        if (mPower.getEuPerTick() > 0) {
            drawLine(lineCounter, GT_Utility.trans("152", "Total: ") + mPower.getTotalPowerString());
            lineCounter++;

            String amperage = mPower.getAmperageString();
            String powerUsage = mPower.getPowerUsageString();
            if (amperage == null || amperage.equals("unspecified") || powerUsage.contains("(OC)")) {
                drawLine(lineCounter, GT_Utility.trans("153", "Usage: ") + powerUsage);
                lineCounter++;
                if (GT_Mod.gregtechproxy.mNEIOriginalVoltage) {
                    Power originalPower = getPowerFromRecipeMap();
                    if (!(originalPower instanceof UnspecifiedEUPower)) {
                        originalPower.computePowerUsageAndDuration(recipe.mEUt, recipe.mDuration);
                        drawLine(
                            lineCounter,
                            GT_Utility.trans("275", "Original voltage: ") + originalPower.getVoltageString());
                        lineCounter++;
                    }
                }
                if (amperage != null && !amperage.equals("unspecified") && !amperage.equals("1")) {
                    drawLine(lineCounter, GT_Utility.trans("155", "Amperage: ") + amperage);
                    lineCounter++;
                }
            } else if (amperage.equals("1")) {
                drawLine(lineCounter, GT_Utility.trans("154", "Voltage: ") + mPower.getVoltageString());
                lineCounter++;
            } else {
                drawLine(lineCounter, GT_Utility.trans("153", "Usage: ") + powerUsage);
                lineCounter++;
                drawLine(lineCounter, GT_Utility.trans("154", "Voltage: ") + mPower.getVoltageString());
                lineCounter++;
                drawLine(lineCounter, GT_Utility.trans("155", "Amperage: ") + amperage);
                lineCounter++;
            }
        }
        if (mPower.getDurationTicks() > 0) {
            if (GT_Mod.gregtechproxy.mNEIRecipeSecondMode) {
                if (mPower.getDurationSeconds() > 1.0d) {
                    drawLine(lineCounter, GT_Utility.trans("158", "Time: ") + mPower.getDurationStringSeconds());
                } else {
                    drawLine(
                        lineCounter,
                        GT_Utility.trans("158", "Time: ")
                            + mPower.getDurationStringSeconds()
                            + String.format(" (%s)", mPower.getDurationStringTicks()));
                }
            } else {
                drawLine(lineCounter, GT_Utility.trans("158", "Time: ") + mPower.getDurationStringTicks());
            }
            lineCounter++;
        }

        if (GT_Utility.isStringValid(this.mRecipeMap.mNEISpecialValuePre)
            && this.mRecipeMap.mNEISpecialValuePre.toLowerCase().contains("casing tier")) {
            drawLine(lineCounter, this.mRecipeMap.mNEISpecialValuePre+GT_Values.VN[recipe.mSpecialValue]);
            lineCounter++;
        } else if (drawOptionalLine(lineCounter, getSpecialInfo(recipe.mSpecialValue))) {
            lineCounter++;
        }
        if (GT_Mod.gregtechproxy.mNEIRecipeOwner) {
            if (recipe.owners.size() > 1) {
                drawLine(
                    lineCounter,
                    EnumChatFormatting.ITALIC
                        + GT_Utility.trans("273", "Original Recipe by: ")
                        + recipe.owners.get(0).getName());
                lineCounter++;
                for (int i = 1; i < recipe.owners.size(); i++) {
                    drawLine(
                        lineCounter,
                        EnumChatFormatting.ITALIC
                            + GT_Utility.trans("274", "Modified by: ")
                            + recipe.owners.get(i).getName());
                    lineCounter++;
                }
            } else if (recipe.owners.size() > 0) {
                drawLine(
                    lineCounter,
                    EnumChatFormatting.ITALIC
                        + GT_Utility.trans("272", "Recipe by: ")
                        + recipe.owners.get(0).getName());
                lineCounter++;
            }
        }
        if (GT_Mod.gregtechproxy.mNEIRecipeOwnerStackTrace
            && recipe.stackTraces != null
            && !recipe.stackTraces.isEmpty()) {
            drawLine(lineCounter, "stackTrace:");
            lineCounter++;
            for (StackTraceElement stackTrace : recipe.stackTraces.get(0)) {
                drawLine(lineCounter, stackTrace.toString());
                lineCounter++;
            }
        }
    }
    private Power getPowerFromRecipeMap() {
        Power power;
        if (this.mRecipeMap.mShowVoltageAmperageInNEI) {
            power = new EUPower((byte) 1, this.mRecipeMap.mAmperage);
        } else {
            power = new UnspecifiedEUPower((byte) 1, this.mRecipeMap.mAmperage);
        }
        return power;
    }
}
