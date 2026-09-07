package com.dtteam.dtbop.data;

import com.dtteam.dtbop.DynamicTreesBOP;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.DTLootTableBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class DTBOPBranchLootProvider extends LootTableProvider {
    public DTBOPBranchLootProvider(final PackOutput output,
                                   final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(new SubProviderEntry(
                BranchLootSubProvider::new, LootContextParamSets.BLOCK)), registries);
    }

    private static final class BranchLootSubProvider implements LootTableSubProvider {
        private final HolderLookup.Provider registries;

        private BranchLootSubProvider(final HolderLookup.Provider registries) {
            this.registries = registries;
        }

        @Override
        public void generate(final BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            final List<String> invalidPrimitiveLogs = new ArrayList<>();
            BuiltInRegistries.BLOCK.stream()
                    .filter(BranchBlock.class::isInstance)
                    .map(BranchBlock.class::cast)
                    .filter(branch -> BuiltInRegistries.BLOCK.getKey(branch).getNamespace()
                            .equals(DynamicTreesBOP.MOD_ID))
                    .filter(BranchBlock::shouldGenerateBranchDrops)
                    .forEach(branch -> {
                        final String familyName = branch.getFamily().getRegistryName().getPath();
                        final String logName = (branch.isStrippedBranch() ? "stripped_" : "")
                                + familyName + "_log";
                        final var primitiveLog = BuiltInRegistries.BLOCK.getValue(
                                Identifier.fromNamespaceAndPath("biomesoplenty", logName));
                        if (primitiveLog.asItem() == Items.AIR) {
                            invalidPrimitiveLogs.add(BuiltInRegistries.BLOCK.getKey(branch) + ": "
                                    + Identifier.fromNamespaceAndPath("biomesoplenty", logName));
                            return;
                        }
                        output.accept(ResourceKey.create(Registries.LOOT_TABLE, branch.getLootTableName()),
                                DTLootTableBuilder.createBranchDrops(
                                        primitiveLog, branch.getFamily().getStick(), registries));
                    });
            if (!invalidPrimitiveLogs.isEmpty()) {
                throw new IllegalStateException("Primitive logs have no items: "
                        + String.join(", ", invalidPrimitiveLogs));
            }
        }
    }
}
