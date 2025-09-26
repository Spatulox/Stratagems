package com.stevekung.stratagems.datagen;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.StratagemState;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.registry.StratagemRegistry;
import com.stevekung.stratagems.registry.Stratagems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StratagemRegistryPackGenerator extends StratagemDataGenerator {

    private static FabricDataGenerator.Pack pack;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        StratagemRegistry.setOnFreeze(() -> {
            var pack = fabricDataGenerator.createPack();
            //var pack = dataGenerator.createPack();
            var extraProvider = RegistryPatchGenerator.createLookup(fabricDataGenerator.getRegistries(), new RegistrySetBuilder()
                    .add(ModRegistries.STRATAGEM, StratagemRegistry::bootstrap)
            ).thenApply(RegistrySetBuilder.PatchedRegistries::full);

            pack.addProvider((output, provider) -> new DynamicRegistryProvider(output, extraProvider));
            pack.addProvider((output, provider) -> forFeaturePack(output, Component.translatable("dataPack.stratagem_registry_pack.description")));
        });
    }

    public static CompletableFuture<HolderLookup.Provider> getProvider(FabricDataGenerator dataGenerator){
        return RegistryPatchGenerator.createLookup(
                dataGenerator.getRegistries(),
                new RegistrySetBuilder().add(ModRegistries.STRATAGEM, StratagemRegistry::bootstrap)
        ).thenApply(RegistrySetBuilder.PatchedRegistries::full);
    }

    public static void setPack(FabricDataGenerator.Pack pack){
        StratagemRegistryPackGenerator.pack = pack;
    }

    // Première méthode
    public static void generateProvider(FabricDataGenerator dataGenerator) {
        StratagemRegistry.setOnFreeze(() -> {
            //var pack = dataGenerator.createPack();
            var extraProvider = RegistryPatchGenerator.createLookup(dataGenerator.getRegistries(), new RegistrySetBuilder()
                    .add(ModRegistries.STRATAGEM, StratagemRegistry::bootstrap)
            ).thenApply(RegistrySetBuilder.PatchedRegistries::full);

            pack.addProvider((output, provider) -> new DynamicRegistryProvider(output, extraProvider));
            pack.addProvider((output, provider) -> forFeaturePack(output, Component.translatable("dataPack.stratagem_registry_pack.description")));
        });
    }

    // Deuxième méthode
    public static void generateProviders(FabricDataGenerator.Pack pack, FabricDataGenerator dataGenerator) {
        System.out.println("Generate Provider");

        CompletableFuture<HolderLookup.Provider> extraProvider = RegistryPatchGenerator.createLookup(
                dataGenerator.getRegistries(),
                new RegistrySetBuilder().add(ModRegistries.STRATAGEM, StratagemRegistry::bootstrap)
        ).thenApply(RegistrySetBuilder.PatchedRegistries::full);

        extraProvider.thenAccept(provider -> {
            System.out.println("Registry provider ready");
        });

        pack.addProvider((output, registries) -> new DynamicRegistryProvider(output, extraProvider));
        pack.addProvider((output, registries) -> forFeaturePack(output, Component.translatable("dataPack.stratagem_registry_pack.description")));
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(ModRegistries.STRATAGEM, StratagemRegistry::bootstrap);
    }

    public static class DynamicRegistryProvider extends FabricDynamicRegistryProvider {
        public DynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
            super(output, provider);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            entries.addAll(registries.lookupOrThrow(ModRegistries.STRATAGEM));
        }

        @Override
        public String getName() {
            return "Stratagem Registry Pack Generator";
        }
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput output, Component description)
    {
        var datapackVersion = DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA);
        return new PackMetadataGenerator(output).add(PackMetadataSection.TYPE, new PackMetadataSection(description, datapackVersion, Optional.of(new InclusiveRange<>(DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES), datapackVersion))));
    }

}
