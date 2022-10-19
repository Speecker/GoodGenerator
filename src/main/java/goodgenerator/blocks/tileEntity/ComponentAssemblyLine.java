package goodgenerator.blocks.tileEntity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_ASSEMBLY_LINE_GLOW;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import goodgenerator.loader.Loaders;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_StructureUtility;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

public class ComponentAssemblyLine extends GT_MetaTileEntity_EnhancedMultiBlockBase<ComponentAssemblyLine> {

    private int casingTier;

    protected static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<ComponentAssemblyLine> STRUCTURE_DEFINITION =
            StructureDefinition.<ComponentAssemblyLine>builder()
                    .addShape(STRUCTURE_PIECE_MAIN, new String[][] {
                        {
                            "         ",
                            "   MMM   ",
                            " BBM~MBB ",
                            "BB MMM BB",
                            "B       B",
                            "B       B",
                            "B  JJJ  B",
                            "B  P P  B",
                            "B  P P  B",
                            "BBBBBBBBB"
                        },
                        {
                            "         ",
                            " GBBBBBG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BBB  A",
                            "A       A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BBB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        // start
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        // end
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "   III   ",
                            " GL   LG ",
                            "G FFEFF G",
                            "AFF E FFA",
                            "AF  D  FA",
                            "AF     FA",
                            "AF     FA",
                            "AF BHB FA",
                            "AF     FA",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  BHB  A",
                            "A  P P  A",
                            "NBBBBBBBN"
                        },
                        {
                            "   BIB   ",
                            " GL   LG ",
                            "G       G",
                            "AC     CA",
                            "AC     CA",
                            "AC     CA",
                            "A E   E A",
                            "A  BHB  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "         ",
                            " GBBBBBG ",
                            "G       G",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A       A",
                            "A  EEE  A",
                            "A       A",
                            "NBBBBBBBN"
                        },
                        {
                            "         ",
                            "         ",
                            " BBBBBBB ",
                            "BB     BB",
                            "B       B",
                            "B       B",
                            "B       B",
                            "B       B",
                            "B  KKK  B",
                            "BBBBBBBBB"
                        }
                    })
                    .addElement('A', ofBlockUnlocalizedName("IC2", "blockAlloyGlass", 0, true))
                    .addElement('B', ofBlock(GregTech_API.sBlockCasings2, 0))
                    .addElement('C', ofBlock(GregTech_API.sBlockCasings2, 3))
                    .addElement('D', ofBlock(GregTech_API.sBlockCasings2, 5))
                    .addElement('E', ofBlock(GregTech_API.sBlockCasings2, 9))
                    .addElement('F', ofBlock(GregTech_API.sBlockCasings2, 13))
                    .addElement('G', ofBlock(GregTech_API.sBlockCasings3, 10))
                    .addElement('H', ofBlock(GregTech_API.sBlockCasings4, 1))
                    .addElement(
                            'I',
                            ofBlocksTiered(
                                    (block, meta) -> block == Loaders.componentAssemblylineCasing ? meta : null,
                                    IntStream.range(0, 14)
                                            .mapToObj(i -> Pair.of(Loaders.componentAssemblylineCasing, i))
                                            .collect(Collectors.toList()),
                                    null,
                                    ComponentAssemblyLine::setCasingTier,
                                    ComponentAssemblyLine::getCasingTier))
                    .addElement('J', ofChain(InputBus.newAny(16, 1), ofBlock(GregTech_API.sBlockCasings2, 0)))
                    .addElement('K', ofChain(OutputBus.newAny(16, 2), ofBlock(GregTech_API.sBlockCasings2, 0)))
                    .addElement(
                            'L',
                            ofChain(Energy.newAny(16, 3, ForgeDirection.UP), ofBlock(GregTech_API.sBlockCasings2, 0)))
                    .addElement('M', ofChain(Maintenance.newAny(16, 4), ofBlock(GregTech_API.sBlockCasings2, 0)))
                    .addElement('N', ofChain(InputHatch.newAny(16, 5), ofBlock(GregTech_API.sBlockCasings2, 0)))
                    .addElement('P', GT_StructureUtility.ofFrame(Materials.Steel))
                    .build();

    public ComponentAssemblyLine(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected ComponentAssemblyLine(String aName) {
        super(aName);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 4, 2, 0);
    }

    @Override
    public IStructureDefinition<ComponentAssemblyLine> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("High-Capacity Component Assembler")
                .addInfo("Controller block for the Component Assembly Line.")
                .addInfo("Assembles basic components (motors, pumps, etc.) in large batches.")
                .addInfo("Much more efficient with materials than competing brands!")
                .beginStructureBlock(9, 10, 33, false)
                .toolTipFinisher(EnumChatFormatting.AQUA + "MadMan310" + EnumChatFormatting.GRAY + " via "
                        + EnumChatFormatting.GREEN + "Good Generator");

        return tt;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ComponentAssemblyLine(mName);
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
                    Textures.BlockIcons.casingTexturePages[0][16],
                    TextureFactory.builder()
                            .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE)
                            .extFacing()
                            .build(),
                    TextureFactory.builder()
                            .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_ACTIVE_GLOW)
                            .extFacing()
                            .glow()
                            .build()
                };
            return new ITexture[] {
                Textures.BlockIcons.casingTexturePages[0][16],
                TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE)
                        .extFacing()
                        .build(),
                TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_ASSEMBLY_LINE_GLOW)
                        .extFacing()
                        .glow()
                        .build()
            };
        }
        return new ITexture[] {Textures.BlockIcons.casingTexturePages[0][16]};
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        return false;
    }

    private void setCasingTier(int tier) {
        casingTier = tier;
    }

    public int getCasingTier() {
        return casingTier;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return false;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        // return GT_Recipe.GT_Recipe_Map.sComponentAssemblyLineRecipes;
        return null;
    }
}
