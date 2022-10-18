package goodgenerator.blocks.tileEntity.base;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.enums.GT_Values.AuthorColen;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.*;
import static java.lang.Math.*;

import com.github.technus.tectech.thing.CustomItemList;
import com.github.technus.tectech.thing.casing.GT_Block_CasingsTT;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import goodgenerator.util.cyclotron.*;
import gregtech.api.GregTech_API;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ExoticEnergyInputHelper;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_AbstractMultiFurnace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("SpellCheckingInspection")
public class GT_MetaTileEntity_Cyclotron extends GT_MetaTileEntity_AbstractMultiFurnace<GT_MetaTileEntity_Cyclotron>
        implements ISurvivalConstructable {

    private static final double log4 = Math.log(4);

    protected static final int DIM_INJECTION_CASING = 13;
    protected static final int DIM_BRIDGE_CASING = 14;

    private HeatingCoilLevel mCoilLevel;

    public HeatingCoilLevel getCoilLevel() {
        return mCoilLevel;
    }

    public void setCoilLevel(HeatingCoilLevel aCoilLevel) {
        mCoilLevel = aCoilLevel;
    }

    private int CompactFusionCoilMetadata = -1;

    protected static final String[] CYCLOTRON_IDENTITY = new String[] {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37",
        "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55",
        "56", "57", "58", "59", "60", "61", "62", "63",
    };


    protected static final String controller_segment_identity = "controller_segment_identity";
    protected static final String[][] controller_segment = new String[][]{{
            "IIIII",
            "II~II",
            "IIIII"
        },{
            "IIIII",
            "     ",
            "IIIII"
        },{
            "IIIII",
            "IIIII",
            "IIIII"
        }};

    private static final IStructureDefinition<GT_MetaTileEntity_Cyclotron> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_Cyclotron>builder()
                .addShape(controller_segment_identity, controller_segment)
                    .addShape(CYCLOTRON_IDENTITY[0], CYCLOTRON_SHAPE_FILE_0.CYCLOTRON_SHAPE_0_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[1], CYCLOTRON_SHAPE_FILE_1.CYCLOTRON_SHAPE_1_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[2], CYCLOTRON_SHAPE_FILE_2.CYCLOTRON_SHAPE_2_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[3], CYCLOTRON_SHAPE_FILE_3.CYCLOTRON_SHAPE_3_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[4], CYCLOTRON_SHAPE_FILE_4.CYCLOTRON_SHAPE_4_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[5], CYCLOTRON_SHAPE_FILE_5.CYCLOTRON_SHAPE_5_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[6], CYCLOTRON_SHAPE_FILE_6.CYCLOTRON_SHAPE_6_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[7], CYCLOTRON_SHAPE_FILE_7.CYCLOTRON_SHAPE_7_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[8], CYCLOTRON_SHAPE_FILE_8.CYCLOTRON_SHAPE_8_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[9], CYCLOTRON_SHAPE_FILE_9.CYCLOTRON_SHAPE_9_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[10], CYCLOTRON_SHAPE_FILE_10.CYCLOTRON_SHAPE_10_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[11], CYCLOTRON_SHAPE_FILE_11.CYCLOTRON_SHAPE_11_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[12], CYCLOTRON_SHAPE_FILE_12.CYCLOTRON_SHAPE_12_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[13], CYCLOTRON_SHAPE_FILE_13.CYCLOTRON_SHAPE_13_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[14], CYCLOTRON_SHAPE_FILE_14.CYCLOTRON_SHAPE_14_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[15], CYCLOTRON_SHAPE_FILE_15.CYCLOTRON_SHAPE_15_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[16], CYCLOTRON_SHAPE_FILE_16.CYCLOTRON_SHAPE_16_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[17], CYCLOTRON_SHAPE_FILE_17.CYCLOTRON_SHAPE_17_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[18], CYCLOTRON_SHAPE_FILE_18.CYCLOTRON_SHAPE_18_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[19], CYCLOTRON_SHAPE_FILE_19.CYCLOTRON_SHAPE_19_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[20], CYCLOTRON_SHAPE_FILE_20.CYCLOTRON_SHAPE_20_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[21], CYCLOTRON_SHAPE_FILE_21.CYCLOTRON_SHAPE_21_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[22], CYCLOTRON_SHAPE_FILE_22.CYCLOTRON_SHAPE_22_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[23], CYCLOTRON_SHAPE_FILE_23.CYCLOTRON_SHAPE_23_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[24], CYCLOTRON_SHAPE_FILE_24.CYCLOTRON_SHAPE_24_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[25], CYCLOTRON_SHAPE_FILE_25.CYCLOTRON_SHAPE_25_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[26], CYCLOTRON_SHAPE_FILE_26.CYCLOTRON_SHAPE_26_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[27], CYCLOTRON_SHAPE_FILE_27.CYCLOTRON_SHAPE_27_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[28], CYCLOTRON_SHAPE_FILE_28.CYCLOTRON_SHAPE_28_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[29], CYCLOTRON_SHAPE_FILE_29.CYCLOTRON_SHAPE_29_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[30], CYCLOTRON_SHAPE_FILE_30.CYCLOTRON_SHAPE_30_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[31], CYCLOTRON_SHAPE_FILE_31.CYCLOTRON_SHAPE_31_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[32], CYCLOTRON_SHAPE_FILE_32.CYCLOTRON_SHAPE_32_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[33], CYCLOTRON_SHAPE_FILE_33.CYCLOTRON_SHAPE_33_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[34], CYCLOTRON_SHAPE_FILE_34.CYCLOTRON_SHAPE_34_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[35], CYCLOTRON_SHAPE_FILE_35.CYCLOTRON_SHAPE_35_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[36], CYCLOTRON_SHAPE_FILE_36.CYCLOTRON_SHAPE_36_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[37], CYCLOTRON_SHAPE_FILE_37.CYCLOTRON_SHAPE_37_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[38], CYCLOTRON_SHAPE_FILE_38.CYCLOTRON_SHAPE_38_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[39], CYCLOTRON_SHAPE_FILE_39.CYCLOTRON_SHAPE_39_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[40], CYCLOTRON_SHAPE_FILE_40.CYCLOTRON_SHAPE_40_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[41], CYCLOTRON_SHAPE_FILE_41.CYCLOTRON_SHAPE_41_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[42], CYCLOTRON_SHAPE_FILE_42.CYCLOTRON_SHAPE_42_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[43], CYCLOTRON_SHAPE_FILE_43.CYCLOTRON_SHAPE_43_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[44], CYCLOTRON_SHAPE_FILE_44.CYCLOTRON_SHAPE_44_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[45], CYCLOTRON_SHAPE_FILE_45.CYCLOTRON_SHAPE_45_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[46], CYCLOTRON_SHAPE_FILE_46.CYCLOTRON_SHAPE_46_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[47], CYCLOTRON_SHAPE_FILE_47.CYCLOTRON_SHAPE_47_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[48], CYCLOTRON_SHAPE_FILE_48.CYCLOTRON_SHAPE_48_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[49], CYCLOTRON_SHAPE_FILE_49.CYCLOTRON_SHAPE_49_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[50], CYCLOTRON_SHAPE_FILE_50.CYCLOTRON_SHAPE_50_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[51], CYCLOTRON_SHAPE_FILE_51.CYCLOTRON_SHAPE_51_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[52], CYCLOTRON_SHAPE_FILE_52.CYCLOTRON_SHAPE_52_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[53], CYCLOTRON_SHAPE_FILE_53.CYCLOTRON_SHAPE_53_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[54], CYCLOTRON_SHAPE_FILE_54.CYCLOTRON_SHAPE_54_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[55], CYCLOTRON_SHAPE_FILE_55.CYCLOTRON_SHAPE_55_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[56], CYCLOTRON_SHAPE_FILE_56.CYCLOTRON_SHAPE_56_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[57], CYCLOTRON_SHAPE_FILE_57.CYCLOTRON_SHAPE_57_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[58], CYCLOTRON_SHAPE_FILE_58.CYCLOTRON_SHAPE_58_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[59], CYCLOTRON_SHAPE_FILE_59.CYCLOTRON_SHAPE_59_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[60], CYCLOTRON_SHAPE_FILE_60.CYCLOTRON_SHAPE_60_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[61], CYCLOTRON_SHAPE_FILE_61.CYCLOTRON_SHAPE_61_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[62], CYCLOTRON_SHAPE_FILE_62.CYCLOTRON_SHAPE_62_IDENTITY)
                    .addShape(CYCLOTRON_IDENTITY[63], CYCLOTRON_SHAPE_FILE_63.CYCLOTRON_SHAPE_63_IDENTITY)
                    //                    .addElement(
                    //                            'B',
                    //                            ofBlocksTiered(
                    //                                    (block, meta) -> block == compactFusionCoil ? meta : -1,
                    //                                    ImmutableList.of(
                    //                                            Pair.of(compactFusionCoil, 0),
                    //                                            Pair.of(compactFusionCoil, 1),
                    //                                            Pair.of(compactFusionCoil, 2),
                    //                                            Pair.of(compactFusionCoil, 3),
                    //                                            Pair.of(compactFusionCoil, 4)),
                    //                                    -1,
                    //                                    (t, meta) -> t.CompactFusionCoilMetadata = meta,
                    //                                    t -> t.CompactFusionCoilMetadata))
                    .addElement(
                            'X',
                            ofCoil(
                                    GT_MetaTileEntity_Cyclotron::setCoilLevel,
                                    GT_MetaTileEntity_Cyclotron::getCoilLevel))
                                        .addElement(
                                                'I',
                                                buildHatchAdder(GT_MetaTileEntity_Cyclotron.class)
                                                        .atLeast(
                                                                InputHatch,
                                                                OutputHatch,
                                                                InputBus,
                                                                OutputBus,
                                                                Energy,
                                                                ExoticEnergy,
                                                                Maintenance)
                                                        .casingIndex(5)
                                                        .dot(1)
                                                        .buildAndChain(CustomItemList.eM_Power.getBlock(), 0))
                    .addElement('B', ofBlock(GregTech_API.sBlockCasings8, 5)) // Radiation proof casing.
                    .addElement(
                            'W', ofBlockUnlocalizedName("bartworks", "BW_Machinery_Casings", 1, true)) // Winding coil.
                    //            .addElement('G', ofBlockUnlocalizedName("bartworks", "BW_GlasBlocks", 14, true)) //
                    // Cosmic glass.
                    //            .addElement('F', ofFrame(Materials.Infinity)) // Infinity frame.
                    .build();

    @Override
    protected boolean addBottomHatch(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        boolean exotic = addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
        return super.addBottomHatch(aTileEntity, aBaseCasingIndex) || exotic;
    }

    public GT_MetaTileEntity_Cyclotron(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_Cyclotron(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_Cyclotron(mName);
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addInfo("Transcending Dimensional Boundaries.")
                .addInfo("")
                .addInfo(AuthorColen)
                .addSeparator()
                .beginStructureBlock(0, 0, 0, false)
                .toolTipFinisher("Gregtech");
        return tt;
    }

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        boolean exotic = addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
        return super.addToMachineList(aTileEntity, aBaseCasingIndex) || exotic;
    }

    @Override
    public ITexture[] getTexture(
            IGregTechTileEntity aBaseMetaTileEntity,
            byte aSide,
            byte aFacing,
            byte aColorIndex,
            boolean aActive,
            boolean aRedstone) {
        if (aSide == aFacing) {
            if (aActive)
                return new ITexture[] {
                    casingTexturePages[0][14],
                    TextureFactory.builder()
                            .addIcon(OVERLAY_DTPF_ON)
                            .extFacing()
                            .build(),
                    TextureFactory.builder()
                            .addIcon(OVERLAY_FUSION1_GLOW)
                            .extFacing()
                            .glow()
                            .build()
                };
            return new ITexture[] {
                casingTexturePages[0][DIM_BRIDGE_CASING],
                TextureFactory.builder().addIcon(OVERLAY_DTPF_OFF).extFacing().build()
            };
        }
        return new ITexture[] {casingTexturePages[0][DIM_BRIDGE_CASING]};
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(
                aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "PlasmaForge.png");
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return 0;
    }

    @Override
    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return GT_Recipe.GT_Recipe_Map.sPlasmaForgeRecipes;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_Cyclotron> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        return processRecipe(getCompactedInputs(), getCompactedFluids());
    }

    long EU_per_tick = 0;

    protected boolean processRecipe(ItemStack[] tItems, FluidStack[] tFluids) {

        // Gets the EU input of the
        long tVoltage = GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList());
        long tAmps = GT_ExoticEnergyInputHelper.getMaxInputAmpsMulti(getExoticAndNormalEnergyHatchList());

        long tTotalEU = tVoltage * tAmps;

        // Hacky method to determine if double energy hatches are being used.
        if (getExoticAndNormalEnergyHatchList().get(0) instanceof GT_MetaTileEntity_Hatch_Energy) {
            tTotalEU /= 2L;
        }

        // Look up recipe. If not found it will return null.
        GT_Recipe tRecipe_0 = GT_Recipe.GT_Recipe_Map.sPlasmaForgeRecipes.findRecipe(
                getBaseMetaTileEntity(), false, tTotalEU, tFluids, tItems);

        // Check if recipe found.
        if (tRecipe_0 == null) return false;

        // If coil heat capacity is too low, refuse to start recipe.
        //        if (mHeatingCapacity <= tRecipe_0.mSpecialValue) return false;

        // Reduce fuel quantity if machine has been running for long enough.
        GT_Recipe tRecipe_1 = tRecipe_0.copy();

        // Break out to the outermost for loop when fuel found and discounted. Only 1 fuel per recipe is intended.

        // Takes items/fluids from hatches/busses.
        if (!tRecipe_1.isRecipeInputEqual(true, tFluids, tItems)) return false;

        // Logic for overclocking calculations.
        double EU_input_tier = log(tTotalEU) / log4;
        double EU_recipe_tier = log(tRecipe_0.mEUt) / log4;
        long overclock_count = (long) floor(EU_input_tier - EU_recipe_tier);

        // Vital recipe info. Calculate overclocks here if necessary.
        EU_per_tick = (long) -(tRecipe_0.mEUt * pow(4, overclock_count));

        mMaxProgresstime = (int) (tRecipe_0.mDuration / pow(2, overclock_count));
        mMaxProgresstime = Math.max(1, mMaxProgresstime);

        // Output items/fluids.
        mOutputItems = tRecipe_0.mOutputs.clone();
        mOutputFluids = tRecipe_0.mFluidOutputs.clone();
        updateSlots();

        return true;
    }

    int cachedIndex = -1;

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {

        outside:
        if (cachedIndex == -1) {
            for (int size = 0; size < 64; size++) {
                System.out.println("TRYING SIZE: " + size + ".");
                if (checkPiece(CYCLOTRON_IDENTITY[size], 15 + (size * 5) + 1, 1, 0)) {
                    cachedIndex = size;
                    System.out.println("SIZE " + size + " FOUND.");
                    break outside;
                }
            }
            return false;
        } else {
            System.out.println("CACHED SIZE: " + cachedIndex + ".");
            if (!checkPiece(CYCLOTRON_IDENTITY[cachedIndex], 15 + (cachedIndex * 5) + 1, 1, 0)) {
                System.out.println("CACHED: FAIL.");
                return false;
            }
            System.out.println("CACHED: SUCCESS.");
        }

        // All structure checks passed, return true.
        return true;
    }

    public void clearHatches() {
        super.clearHatches();
        mExoticEnergyHatches.clear();
    }

    @Override
    public boolean addOutput(FluidStack aLiquid) {
        if (aLiquid == null) return false;
        FluidStack tLiquid = aLiquid.copy();

        return dumpFluid(mOutputHatches, tLiquid, true) || dumpFluid(mOutputHatches, tLiquid, false);
    }

    @Override
    public boolean drainEnergyInput(long aEU) {
        return GT_ExoticEnergyInputHelper.drainEnergy(aEU, getExoticAndNormalEnergyHatchList());
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (EU_per_tick < 0) {
            if (!drainEnergyInput(-EU_per_tick)) {
                EU_per_tick = 0;
                criticalStopMachine();
                return false;
            }
        }

        return true;
    }

    @Override
    public String[] getInfoData() {

        long storedEnergy = 0;
        long maxEnergy = 0;

        for (GT_MetaTileEntity_Hatch tHatch : mExoticEnergyHatches) {
            if (isValidMetaTileEntity(tHatch)) {
                storedEnergy += tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy += tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }

        return new String[] {
            "------------ Critical Information ------------",
            StatCollector.translateToLocal("GT5U.multiblock.Progress") + ": " + EnumChatFormatting.GREEN
                    + GT_Utility.formatNumbers(mProgresstime) + EnumChatFormatting.RESET + "t / "
                    + EnumChatFormatting.YELLOW
                    + GT_Utility.formatNumbers(mMaxProgresstime) + EnumChatFormatting.RESET + "t",
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": " + EnumChatFormatting.GREEN
                    + GT_Utility.formatNumbers(storedEnergy) + EnumChatFormatting.RESET + " EU / "
                    + EnumChatFormatting.YELLOW
                    + GT_Utility.formatNumbers(maxEnergy) + EnumChatFormatting.RESET + " EU",
            StatCollector.translateToLocal("GT5U.multiblock.usage") + ": " + EnumChatFormatting.RED
                    + GT_Utility.formatNumbers(-EU_per_tick) + EnumChatFormatting.RESET + " EU/t",
            StatCollector.translateToLocal("GT5U.multiblock.mei") + ": " + EnumChatFormatting.YELLOW
                    + GT_Utility.formatNumbers(
                            GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList()))
                    + EnumChatFormatting.RESET + " EU/t(*" + EnumChatFormatting.YELLOW
                    + GT_Utility.formatNumbers(
                            GT_ExoticEnergyInputHelper.getMaxInputAmpsMulti(getExoticAndNormalEnergyHatchList()))
                    + EnumChatFormatting.RESET + "A) " + StatCollector.translateToLocal("GT5U.machines.tier")
                    + ": " + EnumChatFormatting.YELLOW
                    + VN[
                            GT_Utility.getTier(GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(
                                    getExoticAndNormalEnergyHatchList()))]
                    + EnumChatFormatting.RESET,
            "-----------------------------------------"
        };
    }

    public List<GT_MetaTileEntity_Hatch> getExoticAndNormalEnergyHatchList() {
        List<GT_MetaTileEntity_Hatch> tHatches = new ArrayList<>();
        tHatches.addAll(mExoticEnergyHatches);
        tHatches.addAll(mEnergyHatches);
        return tHatches;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        int size = min(stackSize.stackSize, 63);
        buildPiece(CYCLOTRON_IDENTITY[size], stackSize, hintsOnly, 17 + (size * 5), 2, 1);
        buildPiece(controller_segment_identity, stackSize, hintsOnly, 2, 1, 0);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setLong("eLongEUPerTick", EU_per_tick);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        EU_per_tick = aNBT.getLong("eLongEUPerTick");
        super.loadNBTData(aNBT);
    }
}
