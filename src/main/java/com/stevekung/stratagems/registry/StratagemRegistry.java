package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.stevekung.stratagems.api.references.ModRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

/**
 * Registre singleton pour permettre un enregistrement dynamique de Stratagems par d'autres mods.
 */
public class StratagemRegistry
{
    // Collections thread-safe pour stocker les stratagèmes et leurs clés
    private static final Map<String, Stratagem> LOCAL_STRATAGEMS = new ConcurrentHashMap<>();
    private static final Map<String, ResourceKey<Stratagem>> LOCAL_KEYS = new ConcurrentHashMap<>();

    private static boolean isFrozen = false;
    private static Runnable onFreeze = null;

    public static boolean isFrozen() {
        return isFrozen;
    }

    public static void freeze() {
        isFrozen = true;
        if (onFreeze != null) {
            onFreeze.run();
        }
    }

    public static void setOnFreeze(Runnable callback) {
        onFreeze = callback;
    }

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
            return false;
        }
        LOCAL_STRATAGEMS.put(key, stratagem);
        // Générer la clé ResourceKey associée et la stocker
        LOCAL_KEYS.put(key, ResourceKey.create(ModRegistries.STRATAGEM, ModConstants.id(key)));
        return true;
    }

    /**
     * Récupère un stratagem enregistré.
     * @param key Clé ResourceLocation
     * @return Optional de Stratagem si trouvé.
     */
    public static Optional<Stratagem> getStratagemByStratagemName(String key)
    {
        return Optional.ofNullable(LOCAL_STRATAGEMS.get(key));
    }

    /**
     * Récupère la ResourceKey associée à un stratagem enregistré via son nom.
     * @param key Clé ResourceLocation
     * @return Optional de ResourceKey<Stratagem> si trouvé.
     */
    public static Optional<ResourceKey<Stratagem>> getKeyByStratagemName(String key) {
        return Optional.ofNullable(LOCAL_KEYS.get(key));
    }

    /**
     * Retourne une collection de tous les stratagèmes enregistrés.
     */
    public static Collection<Stratagem> getAll()
    {
        return LOCAL_STRATAGEMS.values();
    }

    /**
     * Méthode à appeler depuis la génération datapack pour injecter tous les stratagèmes enregistrés.
     * @param context BootstrapContext utilisé pour l'enregistrement
     */
    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        System.out.println("Bootstrap Stratagems");
        LOCAL_STRATAGEMS.forEach((keyString, stratagem) -> {
            System.out.println(keyString);
            ResourceKey<Stratagem> key = LOCAL_KEYS.get(keyString);
            context.register(key, stratagem);
        });
    }
}