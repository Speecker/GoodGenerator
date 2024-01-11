package goodgenerator.crossmod;

import static gregtech.api.enums.Mods.Automagy;
import static gregtech.api.enums.Mods.GTPlusPlus;
import static gregtech.api.enums.Mods.NewHorizonsCoreMod;
import static gregtech.api.enums.Mods.ThaumicBases;
import static gregtech.api.enums.Mods.ThaumicEnergistics;
import static gregtech.api.enums.Mods.ThaumicTinkerer;
import static gregtech.api.enums.Mods.WitchingGadgets;

public class LoadedList {

    public static boolean GTPP;
    public static boolean GTNH_CORE;
    public static boolean THAUMIC_BASES;
    public static boolean THAUMIC_TINKERER;
    public static boolean AUTOMAGY;
    public static boolean WITCHING_GADGETS;
    public static boolean THAUMIC_ENERGISTICS;

    public static void init() {
        GTPP = GTPlusPlus.isModLoaded();
        GTNH_CORE = NewHorizonsCoreMod.isModLoaded();
        THAUMIC_BASES = ThaumicBases.isModLoaded();
        THAUMIC_TINKERER = ThaumicTinkerer.isModLoaded();
        AUTOMAGY = Automagy.isModLoaded();
        WITCHING_GADGETS = WitchingGadgets.isModLoaded();
        THAUMIC_ENERGISTICS = ThaumicEnergistics.isModLoaded();
    }
}
