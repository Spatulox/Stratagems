package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.stevekung.stratagems.api.references.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Registre singleton pour permettre un enregistrement dynamique de Stratagems par d'autres mods.
 */
public class StratagemRegistry
{
    // Collection thread-safe pour stocker les stratagems enregistrés
    private static final Map<String, Stratagem> LOCAL_STRATAGEMS = new ConcurrentHashMap<>();
    public static final Map<ResourceKey<Stratagem>, Stratagem> STRATAGEMS = new ConcurrentHashMap<>();

    /**
     * Enregistre un stratagem sous une clé unique.
     * @param key La clé ResourceLocation du stratagem (ex: modid:stratagem_name)
     * @param stratagem L'instance du stratagem à enregistrer
     * @return true si l'inscription a réussi, false si clé déjà existante.
     */
    public static boolean register(String key, Stratagem stratagem)
    {
        if (LOCAL_STRATAGEMS.containsKey(key))
        {
            // On peut logger un warning ici si besoin (stratagem déjà enregistré)
            return false;
        }
        LOCAL_STRATAGEMS.put(key, stratagem);
        return true;
    }

    /**
     * Récupère un stratagem enregistré.
     * @param key Clé ResourceLocation
     * @return Optional de Stratagem si trouvé.
     */
    public static Optional<Stratagem> get(String key)
    {
        return Optional.ofNullable(LOCAL_STRATAGEMS.get(key));
    }

    /**
     * Retourne une collection de tous les stratagems enregistrés.
     */
    public static Collection<Stratagem> getAll()
    {
        return LOCAL_STRATAGEMS.values();
    }

    /**
     * Retourne la Map publique des ResourceKey et Stratagems enregistrés.
     */
    public static Map<ResourceKey<Stratagem>, Stratagem> getRegisteredStratagems() {
        return STRATAGEMS;
    }

    /**
     * Méthode à appeler depuis la génération datapack pour injecter tous les stratagems enregistrés.
     * @param context BootstrapContext utilisé pour l'enregistrement
     */
    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        if(LOCAL_STRATAGEMS.size() <= 0){
            return;
        }
        LOCAL_STRATAGEMS.forEach((keyString, stratagem) -> {
            ResourceKey<Stratagem> key = ResourceKey.create(ModRegistries.STRATAGEM, ModConstants.id(keyString));
            STRATAGEMS.put(key, stratagem);
            context.register(key, stratagem);
        });
    }

}
