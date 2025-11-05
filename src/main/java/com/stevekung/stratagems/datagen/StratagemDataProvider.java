package com.stevekung.stratagems.datagen;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Provider generating json file with a Stratagem instance
 * This is used by external mods to create json stratagem file when DataGeneration, under the data/stratagems/stratagem
 */
public abstract class StratagemDataProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final PackOutput.PathProvider langPathProvider;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private final Codec<Stratagem> codec;
    private final Map<String, JsonObject> translations = new HashMap<>();

    public StratagemDataProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.pathProvider = dataOutput.createRegistryElementsPathProvider(ModRegistries.STRATAGEM);
        this.langPathProvider = dataOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, ModConstants.MOD_ID);
        this.registriesFuture = Objects.requireNonNull(registriesFuture);
        this.codec = Stratagem.DIRECT_CODEC;
    }

    public abstract void generateStratagems(StratagemBuilder builder, HolderLookup.Provider registryLookup) throws Exception;

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        TreeMap<ResourceLocation, JsonElement> entries = new TreeMap<>();

        return registriesFuture.thenCompose(lookup -> {
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

            StratagemBuilder builder = (stratagem) -> {
                DataResult<JsonElement> result = codec.encodeStart(ops, stratagem.STRATAGEM());
                JsonElement json = result.getOrThrow();
                if (entries.put(ModConstants.id(stratagem.KEY_STRING()), json) != null) {
                    throw new IllegalArgumentException("Duplicate stratagem entry " + stratagem.KEY_STRING());
                }

                if (stratagem.NAME() instanceof Map<?, ?> map) {
                    map.forEach((lang, value) -> {
                        String langKey = ((String) lang).toLowerCase(Locale.ROOT).replace('-', '_');
                        JsonObject langJson = translations.computeIfAbsent(langKey, (x) -> new JsonObject());
                        langJson.addProperty(ModConstants.MOD_ID + ".stratagem." + stratagem.KEY_STRING(), value.toString());
                    });
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

            CompletableFuture<?>[] translationFutures = translations.entrySet().stream()
                    .map(entry -> {
                        // We put "" because the langPathProvider already provide the "stratagems/"
                        ResourceLocation langLocation = ResourceLocation.fromNamespaceAndPath("", "lang/" + entry.getKey());
                        Path path = langPathProvider.json(langLocation);
                        return DataProvider.saveStable(output, entry.getValue(), path);
                    })
                    .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(Stream.concat(Arrays.stream(futures), Arrays.stream(translationFutures))
                    .toArray(CompletableFuture[]::new));
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
        void add(AbstractStratagem stratagem) throws Exception;
    }
}