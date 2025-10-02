package com.stevekung.stratagems.registry;

import java.util.List;
import java.util.Optional;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.stevekung.stratagems.action.*;
import com.stevekung.stratagems.api.*;
import com.stevekung.stratagems.api.action.EmptyAction;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.rule.*;

import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Stratagems
{
    public static final ResourceKey<Stratagem> REINFORCE = createKey("reinforce");
    public static final ResourceKey<Stratagem> BOW = createKey("bow");
    public static final ResourceKey<Stratagem> SUPPLY_CHEST = createKey("supply_chest");
    public static final ResourceKey<Stratagem> IRON_SWORD = createKey("iron_sword");
    public static final ResourceKey<Stratagem> IRON_PICKAXE = createKey("iron_pickaxe");
    public static final ResourceKey<Stratagem> BLOCK = createKey("block");
    public static final ResourceKey<Stratagem> TNT = createKey("tnt");
    public static final ResourceKey<Stratagem> FAST_TNT = createKey("fast_tnt");
    public static final ResourceKey<Stratagem> LONG_TNT = createKey("long_tnt");
    public static final ResourceKey<Stratagem> TNT_REARM = createKey("tnt_rearm");

    public static final ResourceKey<Stratagem> ENTITY = createKey("entity");
    public static final ResourceKey<Stratagem> BLOCK_ENTITY = createKey("block_entity");

    public static void bootstrap(BootstrapContext<Stratagem> context)
    {
        register(context, REINFORCE, "wsdaw", new StratagemDisplay(StratagemDisplay.Type.PLAYER_ICON, Optional.empty(), Optional.of(ModConstants.id("textures/stratagem/reinforcement.png")), Optional.empty(), true, Optional.empty()), ReinforceAction.reinforce(), ReinforceRule.defaultRule(), StratagemProperties.withDepletedAndReplenish(0, 2400, 20, ModConstants.BLUE_BEAM_COLOR));
    }

    public static void bootstrapTest(BootstrapContext<Stratagem> context)
    {
        register(context, BOW, "ssawd", Items.BOW, SpawnItemAction.spawnItems(new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 64)), StratagemProperties.simple(100, 6000, ModConstants.BLUE_BEAM_COLOR));
        register(context, SUPPLY_CHEST, "sswd", new StratagemDisplay(StratagemDisplay.Type.TEXTURE, Optional.empty(), Optional.of(ModConstants.id("textures/stratagem/supply_chest.png")), Optional.empty(), true, Optional.empty()), SpawnSupplyAction.spawnSupply(BuiltInLootTables.SPAWN_BONUS_CHEST), DefaultRule.defaultRule(), StratagemProperties.simple(200, 6000, ModConstants.BLUE_BEAM_COLOR));
        register(context, IRON_SWORD, "saswd", Items.IRON_SWORD, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_SWORD)), StratagemProperties.simple(100, 6000, ModConstants.BLUE_BEAM_COLOR));
        register(context, IRON_PICKAXE, "saswwd", Items.IRON_PICKAXE, SpawnItemAction.spawnItem(new ItemStack(Items.IRON_PICKAXE)), StratagemProperties.simple(200, 6000, ModConstants.BLUE_BEAM_COLOR));
        register(context, BLOCK, "ssss", new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(Items.STONE)), Optional.empty(), Optional.empty(), false, Optional.of("64")), SpawnItemAction.spawnItem(new ItemStack(Items.STONE, 64)), StratagemProperties.simple(100, 1200, ModConstants.BLUE_BEAM_COLOR));
        register(context, TNT, "wdsd", Items.TNT, SpawnBombAction.spawnBomb(), DepletedAndRearmRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 3, ModConstants.RED_BEAM_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, FAST_TNT, "wdsw", new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(Items.TNT)), Optional.empty(), Optional.empty(), false, Optional.of("F")), SpawnBombAction.spawnBomb(40), DepletedAndRearmRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 3, ModConstants.RED_BEAM_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, LONG_TNT, "awdw", Items.TNT, SpawnBombAction.spawnBomb(100, Blocks.DIAMOND_BLOCK.defaultBlockState()), DepletedAndRearmRule.defaultRule(), StratagemProperties.withReplenish(40, 60, 1, ModConstants.RED_BEAM_COLOR, new StratagemReplenish(Optional.of(TNT_REARM), "tnt", Optional.empty(), Optional.empty())));
        register(context, TNT_REARM, "wwawd", Items.REDSTONE_TORCH, EmptyAction.empty(), ReplenishRule.defaultRule(), new StratagemProperties(0, -1, 1200, -1, 0, false, false, Optional.of(new StratagemReplenish(Optional.empty(), "tnt", Optional.of(context.lookup(ModRegistries.STRATAGEM).getOrThrow(ModConstants.StratagemTag.TNT_REPLENISH)), Optional.of(SoundEvents.BEACON_ACTIVATE)))));

        register(context, ENTITY, "saswd", Items.GLASS_BOTTLE, SpawnEntityAction.spawnEntity(EntityType.ZOMBIE, 200.0, false), StratagemProperties.simple(100, 6000, ModConstants.BLUE_BEAM_COLOR));
        register(context, BLOCK_ENTITY, "saswd", Items.GLASS_BOTTLE, SpawnBlockAction.spawnBlock(Blocks.FURNACE.defaultBlockState()), StratagemProperties.simple(100, 6000, ModConstants.BLUE_BEAM_COLOR));
    }

    private static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemLike icon, StratagemAction.Builder action, StratagemRule.Builder rule, StratagemProperties properties)
    {
        register(context, key, code, new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(icon)), Optional.empty(), Optional.empty(), true, Optional.empty()), action, rule, properties);
    }

    private static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, StratagemDisplay display, StratagemAction.Builder action, StratagemProperties properties)
    {
        register(context, key, code, display, action, DefaultRule.defaultRule(), properties);
    }

    private static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, ItemLike icon, StratagemAction.Builder action, StratagemProperties properties)
    {
        register(context, key, code, new StratagemDisplay(StratagemDisplay.Type.ITEM, Optional.of(new ItemStack(icon)), Optional.empty(), Optional.empty(), true, Optional.empty()), action, DefaultRule.defaultRule(), properties);
    }

    private static void register(BootstrapContext<Stratagem> context, ResourceKey<Stratagem> key, String code, StratagemDisplay display, StratagemAction.Builder action, StratagemRule.Builder rule, StratagemProperties properties)
    {
        context.register(key, new Stratagem(code, Component.translatable(key.location().toLanguageKey("stratagem")), display, action.build(), rule.build(), properties));
    }

    /**
     * This method is public so that other mods can create keys for their stratagems more easily,
     * since the key is based on the ModConstants.id()
     * @param name
     * @return
     */
    public static ResourceKey<Stratagem> createKey(String name)
    {
        return ResourceKey.create(ModRegistries.STRATAGEM, ModConstants.id(name));
    }

    /**
     * Make one Stratagem available to a player
     * @param player
     * @param key
     * @return
     */
    public static boolean add(ServerPlayer player, ResourceKey<Stratagem> key) {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.add(player, stratagem);
    }

    /**
     * Make one Stratagem available to the server
     * @param server
     * @param key
     * @return
     */
    public static boolean add(MinecraftServer server, ResourceKey<Stratagem> key) {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.add(server, stratagem);
    }

    /**
     * Remove one stratagem from the player
     * @param player
     * @param key
     * @return
     */
    public static boolean remove(ServerPlayer player, ResourceKey<Stratagem> key) {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.remove(player, stratagem);
    }

    /**
     * Remove all the stratagems from the player
     * @param player
     * @return
     */
    public static boolean removeAll(ServerPlayer player) {
        return StratagemManager.removeAll(player);
    }

    /**
     * Remove all the stratagems from the server
     * @param server
     * @param key
     * @return
     */
    public static boolean remove(MinecraftServer server, ResourceKey<Stratagem> key) {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.remove(server, stratagem);
    }

    public static List<StratagemInstance> list(ServerPlayer player) {
        return StratagemManager.list(player);
    }

    public static List<StratagemInstance> list(MinecraftServer server) {
        return StratagemManager.list(server);
    }

    /**
     * Jammed the Stratagem
     * @param player
     * @param key
     * @return
     */
    public static boolean block(ServerPlayer player, ResourceKey<Stratagem> key) {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.block(player, stratagem, false);
    }

    /**
     * Unjammed the Stratagem
     * @param player
     * @param key
     * @return
     */
    public static boolean unblock(ServerPlayer player, ResourceKey<Stratagem> key) {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.block(player, stratagem, true);
    }

    /**
     * Jammed the Stratagem
     * @param server
     * @param key
     * @return
     */
    public static boolean block(MinecraftServer server, ResourceKey<Stratagem> key) {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.block(server, stratagem, false);
    }

    /**
     * Unjammed the Stratagem
     * @param server
     * @param key
     * @return
     */
    public static boolean unblock(MinecraftServer server, ResourceKey<Stratagem> key) {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.block(server, stratagem, true);
    }

    /**
     * Reset the Stratagem to it's initial state
     * @param player
     * @param key
     * @return
     */
    public static boolean reset(ServerPlayer player, ResourceKey<Stratagem> key)
    {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.reset(player, stratagem);
    }

    /**
     * Reset the Stratagem to it's initial state
     * @param server
     * @param key
     * @return
     */
    public static boolean reset(MinecraftServer server, ResourceKey<Stratagem> key)
    {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.reset(server, stratagem);
    }

    public static boolean setModifier(ServerPlayer player, ResourceKey<Stratagem> key, StratagemModifier modifier, boolean clear)
    {
        var stratagem = player.server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.setModifier(player, stratagem, modifier, clear);
    }

    public static boolean setModifier(MinecraftServer server, ResourceKey<Stratagem> key, StratagemModifier modifier, boolean clear)
    {
        var stratagem = server.registryAccess()
                .registryOrThrow(ModRegistries.STRATAGEM)
                .getHolderOrThrow(key);
        return StratagemManager.setModifier(server, stratagem, modifier, clear);
    }

}