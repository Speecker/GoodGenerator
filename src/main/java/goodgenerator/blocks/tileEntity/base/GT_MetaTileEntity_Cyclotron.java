package goodgenerator.blocks.tileEntity.base;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IItemSource;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.GregTech_API;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.objects.GT_ChunkManager;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ExoticEnergyInputHelper;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_AbstractMultiFurnace;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static goodgenerator.loader.Loaders.compactFusionCoil;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.enums.GT_Values.AuthorColen;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.*;
import static java.lang.Math.*;

public class GT_MetaTileEntity_Cyclotron extends GT_MetaTileEntity_AbstractMultiFurnace<GT_MetaTileEntity_Cyclotron> implements ISurvivalConstructable {

    private static final int min_input_hatch = 0;
    private static final int max_input_hatch = 6;
    private static final int min_output_hatch = 0;
    private static final int max_output_hatch = 2;
    private static final int min_input_bus = 0;
    private static final int max_input_bus = 6;
    private static final int min_output_bus = 0;
    private static final int max_output_bus = 1;

    private static final double log4 = Math.log(4);

    // Current discount rate. 1 = 0%, 0 = 100%.
    private double discount = 1;
    private int mHeatingCapacity = 0;
    private long running_time = 0;
    // Custom long EU per tick value given that mEUt is an int. Required to overclock beyond MAX voltage.
    private long EU_per_tick = 0;

    private static final String[][] MID_SEGMENT = new String[][] {
        {
            "         ",
            "         ",
            "CCCCCCCCC",
            "CCCGGGCCC",
            "CCCCCCCCC",
            "         ",
            "         ",
        },
        {
            "         ", "CCCCCCCCC", "WWWWWWWWW", "WWWWRWWWW", "WWWWWWWWW", "CCCCCCCCC", "  F   F  ",
        },
        {
            "         ", "CCCGGGCCC", "WWWWRWWWW", "XXXXXXXXX", "WWWWRWWWW", "CCCGGGCCC", "         ",
        },
        {
            "         ", "CCCCCCCCC", "WWWWWWWWW", "WWWWRWWWW", "WWWWWWWWW", "CCCCCCCCC", "  F   F  ",
        },
        {
            "         ", "         ", "CCCCCCCCC", "CCCGGGCCC", "CCCCCCCCC", "         ", "         ",
        }
    };

    static String[][] rotateCW(String[][] mat) {
        String[][] out = new String[mat.length][mat[0].length];

        int i = 0;
        for (String[] string: mat) {
            out[i] = rotate(string);
            i++;
        }
        return out;
    }

    public static String[] rotate(String [] toRotate)
    {
        String [] returnChar = new String[toRotate[0].length()];
        String [] result = new String[toRotate[0].length()];
        Arrays.fill(returnChar, "");

        for (String s : toRotate)
            for (int cols = 0; cols < s.length(); cols++)
                returnChar[cols] = returnChar[cols] + s.charAt(cols);

        for(int i = 0; i < returnChar.length; i++)
            result[i] =  new StringBuffer(returnChar[i]).reverse().toString();

        return result;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final String[][] CORNER_SEGMENT_0 = new String[][] {{
        "        ",
        "        ",
        "        ",
        "        ",
        "        ",
        "        ",
        "        "
    },{
        "        ",
        "        ",
        "C       ",
        "C       ",
        "C       ",
        "        ",
        "        "
    },{
        "        ",
        "C       ",
        "WCC     ",
        "WCC     ",
        "WCC     ",
        "C       ",
        "        "
    },{
        "        ",
        "CCC     ",
        "WWWC    ",
        "XWWC    ",
        "WWWC    ",
        "CCCF    ",
        "   F    "
    },{
        "        ",
        "CCCC    ",
        "WWWWC   ",
        "WXXWC   ",
        "WWWWC   ",
        "CCCC    ",
        "        "
    },{
        "        ",
        " CCCC   ",
        "CWWWWC  ",
        "CWWXWC  ",
        "CWWWWC  ",
        "FCCCCF  ",
        "F    F  "
    },{
        "        ",
        "   CCC  ",
        " CCWWWC ",
        " CCWXWC ",
        " CCWWWC ",
        "   CCC  ",
        "        "
    },{
        "        ",
        "   CCC  ",
        "  CWWWC ",
        "  CWXWC ",
        "  CWWWC ",
        "   CCC  ",
        "        "
    },{
        "        ",
        "    CCC ",
        "   CWWWC",
        "   CWXWC",
        "   CWWWC",
        "   FCCC ",
        "   F    "
    },{
        "        ",
        "    CCC ",
        "   CWWWC",
        "   CWXWC",
        "   CWWWC",
        "    CCC ",
        "        "
    }};

    private static final String[][] CORNER_SEGMENT_1 = new String[][] {{
        "         ",
        "       CC",
        "       CC",
        "       CC",
        "         ",
        "         "
    },{
        "       CC",
        "     CCWW",
        "     CCWW",
        "     CCWW",
        "       CC",
        "         "
    },{
        "     CCCC",
        "    CWWWW",
        "    CWWXX",
        "    CWWWW",
        "    FCCCC",
        "    F    "
    },{
        "    CCCCC",
        "   CWWWWW",
        "   CWXXWW",
        "   CWWWWW",
        "    CCCCC",
        "         "
    },{
        "   CCCC  ",
        "  CWWWWCC",
        "  CWXWWCC",
        "  CWWWWCC",
        "  FCCCCF ",
        "  F    F "
    },{
        "  CCC    ",
        " CWWWCC  ",
        " CWXWCC  ",
        " CWWWCC  ",
        "  CCC    ",
        "         "
    },{
        "  CCC    ",
        " CWWWC   ",
        " CWXWC   ",
        " CWWWC   ",
        "  CCC    ",
        "         "
    },{
        " CCC     ",
        "CWWWC    ",
        "CWXWC    ",
        "CWWWC    ",
        " CCCF    ",
        "    F    "
    },{
        " CCC     ",
        "CWWWC    ",
        "CWXWC    ",
        "CWWWC    ",
        " CCC     ",
        "         "
    }};


    private static final String[][] CORNER_SEGMENT_2 = rotateCW(CORNER_SEGMENT_1);
    private static final String[][] CORNER_SEGMENT_3 = rotateCW(CORNER_SEGMENT_2);


    private static final String[][] RING_SEGMENT = new String[][] {
        {
            " ", " ", "C", "C", "C", " ", " ",
        },
        {
            " ", "C", "Z", "Z", "Z", "C", " ",
        },
        {
            "C", "Z", "X", "X", "X", "Z", "C",
        },
        {
            "C", "Z", "X", "X", "X", "Z", "C",
        },
        {
            "C", "Z", "X", "X", "X", "Z", "C",
        },
        {
            " ", "C", "Z", "Z", "Z", "C", " ",
        },
        {
            " ", " ", "C", "C", "C", " ", " ",
        },
    };

    @SuppressWarnings("SpellCheckingInspection")
    private static final String[][] CONTROLLER_SEGMENT = new String[][] {{
        "         ",
        "         ",
        "  C   C  ",
        "  C   C  ",
        "  C   C  ",
        "         ",
        "         "
    },{
        "         ",
        "  CCCCC  ",
        "CCCIIICCC",
        "CCCG~GCCC",
        "CCCIIICCC",
        " FCCCCCF ",
        " F     F "
    },{
        "  C   C  ",
        "CCCIIICCC",
        "WWWWWWWWW",
        "WWWXXXWWW",
        "WWWWWWWWW",
        "CCCIIICCC",
        "  C   C  "
    },{
        "  C   C  ",
        "CCCGGGCCC",
        "WWWXXXWWW",
        "XXXXXXXXX",
        "WWWXXXWWW",
        "CCCGGGCCC",
        "  C   C  "
    },{
        "  C   C  ",
        "CCCIIICCC",
        "WWWWWWWWW",
        "WWWXXXWWW",
        "WWWWWWWWW",
        "CCCIIICCC",
        "  C   C  "
    },{
        "         ",
        "  CCCCC  ",
        "CCCIIICCC",
        "CCCGGGCCC",
        "CCCIIICCC",
        " FCCCCCF ",
        " F     F "
    },{
        "         ",
        "         ",
        "  C   C  ",
        "  C   C  ",
        "  C   C  ",
        "         ",
        "         "
    }};

    protected static final int DIM_INJECTION_CASING = 13;
    protected static final int DIM_BRIDGE_CASING = 14;

    private boolean isMultiChunkloaded = true;

    private HeatingCoilLevel mCoilLevel;

    public HeatingCoilLevel getCoilLevel() {
        return mCoilLevel;
    }

    public void setCoilLevel(HeatingCoilLevel aCoilLevel) {
        mCoilLevel = aCoilLevel;
    }

    protected static final String CORNER_SEGMENT_IDENTITY_0 = "CORNER_IDENTITY_0";
    protected static final String CORNER_SEGMENT_IDENTITY_1 = "CORNER_IDENTITY_1";
    protected static final String CORNER_SEGMENT_IDENTITY_2 = "CORNER_IDENTITY_2";
    protected static final String CORNER_SEGMENT_IDENTITY_3 = "CORNER_IDENTITY_3";
    protected static final String CONTROLLER_SEGMENT_IDENTITY = "CONTROLLER_SEGMENT";
    protected static final String MID_SEGMENT_IDENTITY = "MID_SEGMENT";
    protected static final String RING_SEGMENT_IDENTITY = "RING_SEGMENT";

    private int CompactFusionCoilMetadata = -1;

    private static final IStructureDefinition<GT_MetaTileEntity_Cyclotron> STRUCTURE_DEFINITION =
        StructureDefinition.<GT_MetaTileEntity_Cyclotron>builder()
            .addShape(CONTROLLER_SEGMENT_IDENTITY, CONTROLLER_SEGMENT)
            .addShape(MID_SEGMENT_IDENTITY, MID_SEGMENT)
            .addShape(RING_SEGMENT_IDENTITY, RING_SEGMENT)
            .addShape(CORNER_SEGMENT_IDENTITY_0, CORNER_SEGMENT_0)
            .addShape(CORNER_SEGMENT_IDENTITY_1, CORNER_SEGMENT_1)
            .addShape(CORNER_SEGMENT_IDENTITY_2, CORNER_SEGMENT_2)
            .addShape(CORNER_SEGMENT_IDENTITY_3, CORNER_SEGMENT_3)
            .addElement(
                'Z',
                ofBlocksTiered(
                    (block, meta) -> block == compactFusionCoil ? meta : -1,
                    ImmutableList.of(
                        Pair.of(compactFusionCoil, 0),
                        Pair.of(compactFusionCoil, 1),
                        Pair.of(compactFusionCoil, 2),
                        Pair.of(compactFusionCoil, 3),
                        Pair.of(compactFusionCoil, 4)),
                    -1,
                    (t, meta) -> t.CompactFusionCoilMetadata = meta,
                    t -> t.CompactFusionCoilMetadata))
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
                    .casingIndex(DIM_INJECTION_CASING)
                    .dot(1)
                    .buildAndChain(GregTech_API.sBlockCasings1, DIM_INJECTION_CASING))
            .addElement('R', ofBlockUnlocalizedName("IC2", "blockAlloyGlass", 0, true)) // Reinforced glass.
            .addElement('C', ofBlock(GregTech_API.sBlockCasings8, 5)) // Radiation proof casing.
            .addElement(
                'W', ofBlockUnlocalizedName("bartworks", "BW_Machinery_Casings", 1, true)) // Winding coil.
            .addElement('G', ofBlockUnlocalizedName("bartworks", "BW_GlasBlocks", 14, true)) // Cosmic glass.
            .addElement('F', ofFrame(Materials.Infinity)) // Infinity frame.
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
                    casingTexturePages[0][DIM_BRIDGE_CASING],
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
        boolean recipe_process = processRecipe(getCompactedInputs(), getCompactedFluids());

        // If recipe cannot be found then continuity is broken and reset running time to 0.
        if (!recipe_process) {
            resetDiscount();
        }

        return recipe_process;
    }

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
        if (mHeatingCapacity <= tRecipe_0.mSpecialValue) return false;

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

        // All conditions met so increment running_time.
        running_time += mMaxProgresstime;
        return true;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {

        // Reset heating capacity.
        mHeatingCapacity = 0;

        // Get heating capacity from coils in structure.
        setCoilLevel(HeatingCoilLevel.None);

        // Check the main structure
        if (!checkPiece(CONTROLLER_SEGMENT_IDENTITY, 16, 21, 16)) {
            return false;
        }

        if (getCoilLevel() == HeatingCoilLevel.None) return false;

        // Item input bus check.
        if ((mInputBusses.size() < min_input_bus) || (mInputBusses.size() > max_input_bus)) return false;

        // Item output bus check.
        if ((mOutputBusses.size() < min_output_bus) || (mOutputBusses.size() > max_output_bus)) return false;

        // Fluid input hatch check.
        if ((mInputHatches.size() < min_input_hatch) || (mInputHatches.size() > max_input_hatch)) return false;

        // Fluid output hatch check.
        if ((mOutputHatches.size() < min_output_hatch) || (mOutputHatches.size() > max_output_hatch)) return false;

        // If there is more than 1 TT energy hatch, the structure check will fail.
        // If there is a TT hatch and a normal hatch, the structure check will fail.
        if (mExoticEnergyHatches.size() > 0) {
            if (mEnergyHatches.size() > 0) return false;
            if (mExoticEnergyHatches.size() > 1) return false;
        }

        // If there is 0 or more than 2 energy hatches structure check will fail.
        if (mEnergyHatches.size() > 0) {
            if (mEnergyHatches.size() > 2) return false;

            // Check will also fail if energy hatches are not of the same tier.
            byte tier_of_hatch = mEnergyHatches.get(0).mTier;
            for (GT_MetaTileEntity_Hatch_Energy energyHatch : mEnergyHatches) {
                if (energyHatch.mTier != tier_of_hatch) {
                    return false;
                }
            }
        }

        // If there are no energy hatches or TT energy hatches, structure will fail to form.
        if ((mEnergyHatches.size() == 0) && (mExoticEnergyHatches.size() == 0)) return false;

        // One maintenance hatch only. Mandatory.
        if (mMaintenanceHatches.size() != 1) return false;

        // Heat capacity of coils used on multi. No free heat from extra EU!
        mHeatingCapacity = (int) getCoilLevel().getHeat();

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
                resetDiscount();
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
            StatCollector.translateToLocal("GT5U.EBF.heat") + ": " + EnumChatFormatting.GREEN
                + GT_Utility.formatNumbers(mHeatingCapacity) + EnumChatFormatting.RESET + " K",
            "Ticks run: " + EnumChatFormatting.GREEN + GT_Utility.formatNumbers(running_time) + EnumChatFormatting.RESET
                + ", Fuel Discount: " + EnumChatFormatting.RED + GT_Utility.formatNumbers(100 * (1 - discount))
                + EnumChatFormatting.RESET + "%",
            "-----------------------------------------"
        };
    }

    public List<GT_MetaTileEntity_Hatch> getExoticAndNormalEnergyHatchList() {
        List<GT_MetaTileEntity_Hatch> tHatches = new ArrayList<>();
        tHatches.addAll(mExoticEnergyHatches);
        tHatches.addAll(mEnergyHatches);
        return tHatches;
    }

    // Reset running time and discount.
    public void resetDiscount() {
        running_time = 0;
        discount = 1;
    }

    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide() && !aBaseMetaTileEntity.isAllowedToWork()) {
            // Reset running time and discount.
            resetDiscount();
            // If machine has stopped, stop chunkloading.
            GT_ChunkManager.releaseTicket((TileEntity) aBaseMetaTileEntity);
            isMultiChunkloaded = false;
        } else if (aBaseMetaTileEntity.isServerSide() && aBaseMetaTileEntity.isAllowedToWork() && !isMultiChunkloaded) {
            // Load a 3x3 area centered on controller when machine is running.
            GT_ChunkManager.releaseTicket((TileEntity) aBaseMetaTileEntity);

            int ControllerXCoordinate = ((TileEntity) aBaseMetaTileEntity).xCoord;
            int ControllerZCoordinate = ((TileEntity) aBaseMetaTileEntity).zCoord;

            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate - 16));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate - 16));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad(
                (TileEntity) aBaseMetaTileEntity,
                new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate - 16));

            isMultiChunkloaded = true;
        }

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(CONTROLLER_SEGMENT_IDENTITY, stackSize, hintsOnly, 4, 3, 1);
        int tLength = Math.min(stackSize.stackSize + 1, 16);
        for (int i = 1; i < tLength; i++) {
            buildPiece(MID_SEGMENT_IDENTITY, stackSize, hintsOnly, 4 - i * 10, 3, 0);
            buildPiece(MID_SEGMENT_IDENTITY, stackSize, hintsOnly, 4 + i * 10, 3, 0);
        }
        int a = 5;
        int b = -5;
        for (int i = 1; i < tLength + 1; i++) {
            buildPiece(RING_SEGMENT_IDENTITY, stackSize, hintsOnly, a - i * 10, 3, 1);
            buildPiece(RING_SEGMENT_IDENTITY, stackSize, hintsOnly, b + i * 10, 3, 1); // a = 5
        }

        int x_0 = -6 -10 * stackSize.stackSize; // -4 - 11
        int y_0 = 3; // 2
        int z_0 = 1;
        buildPiece(CORNER_SEGMENT_IDENTITY_0, stackSize, hintsOnly, x_0, y_0, z_0);

        int x_1 = 14 + 10 * stackSize.stackSize; // -4 - 11
        int y_1 = 2;
        int z_1 = 0;
        buildPiece(CORNER_SEGMENT_IDENTITY_1, stackSize, hintsOnly, x_1, y_1, z_1);

    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, IItemSource source, EntityPlayerMP actor) {
        if (mMachine) return -1;
        int realBudget = elementBudget >= 200 ? elementBudget : Math.min(200, elementBudget * 5);
        return survivialBuildPiece(
            CONTROLLER_SEGMENT_IDENTITY, stackSize, 16, 21, 16, realBudget, source, actor, false, true);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setLong("eRunningTime", running_time);
        aNBT.setDouble("eLongDiscountValue", discount);
        aNBT.setLong("eLongEUPerTick", EU_per_tick);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        running_time = aNBT.getLong("eRunningTime");
        discount = aNBT.getDouble("eLongDiscountValue");
        EU_per_tick = aNBT.getLong("eLongEUPerTick");
        super.loadNBTData(aNBT);
    }
}
