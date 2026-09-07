package com.dtteam.dtbop;

import com.dtteam.dtbop.data.DTBOPBranchLootProvider;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DynamicTreesBOP.MOD_ID)
public class DynamicTreesBOP {
    public static final String MOD_ID = "dtbop";

    public DynamicTreesBOP(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::gatherClientData);
        eventBus.register(DTBOPRegistries.class);

        NeoForgeRegistryHandler.setup(MOD_ID, eventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DTBOPRegistries.setup();
    }

    private void gatherClientData(final GatherDataEvent.Client event) {
        event.getGenerator().addProvider(true, new DTBOPBranchLootProvider(
                event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }

    public static Identifier location (String name){
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

}
