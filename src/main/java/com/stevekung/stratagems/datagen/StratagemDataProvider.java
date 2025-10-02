package com.stevekung.stratagems.datagen;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.references.ModRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Provider generating json file with a Stratagem instance
 * This is used by external mods to create json stratagem file when DataGeneration, under the data/stratagems/stratagem
 */
public abstract class StratagemDataProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private final Codec<Stratagem> codec;

    public StratagemDataProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.pathProvider = dataOutput.createRegistryElementsPathProvider(ModRegistries.STRATAGEM);
        this.registriesFuture = Objects.requireNonNull(registriesFuture);
        this.codec = Stratagem.DIRECT_CODEC;
    }

    /**
     * Implémenter pour générer les stratagems via le builder.
     */
    public abstract void generateStratagems(StratagemBuilder builder, HolderLookup.Provider registryLookup) throws Exception;

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        TreeMap<ResourceLocation, JsonElement> entries = new TreeMap<>();

        return registriesFuture.thenCompose(lookup -> {
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

            StratagemBuilder builder = (name, stratagem) -> {
                DataResult<JsonElement> result = codec.encodeStart(ops, stratagem);
                JsonElement json = result.getOrThrow();
                if (entries.put(ModConstants.id(name), json) != null) {
                    throw new IllegalArgumentException("Duplicate stratagem entry " + name);
                }
            };

            try {
                this.generateStratagems(builder, lookup);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }

            CompletableFuture<?>[] futures = entries.entrySet().stream()
                    .map(entry -> {
                        Path path = pathProvider.json(entry.getKey());
                        return DataProvider.saveStable(output, entry.getValue(), path);
                    }).toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    private CompletableFuture<?> write(CachedOutput output, ResourceLocation id, JsonElement json) {
        Path path = pathProvider.json(id);
        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "Stratagem Data Provider";
    }

    @FunctionalInterface
    public interface StratagemBuilder {
        void add(String name, Stratagem stratagem) throws Exception;
    }
}