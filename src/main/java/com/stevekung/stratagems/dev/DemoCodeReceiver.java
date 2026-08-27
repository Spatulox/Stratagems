package com.stevekung.stratagems.dev;

import java.util.Set;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.receiver.StratagemCodeHandler;
import com.stevekung.stratagems.api.receiver.StratagemCodeReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dev-only demo block entity that implements {@link StratagemCodeReceiver}.
 * Registered in {@code StratagemsMod.onInitialize()} only when
 * {@code FabricLoader.getInstance().isDevelopmentEnvironment()} returns {@code true}.
 * <p>
 * Place with {@code /setblock ~ ~ ~ stratagems:demo_code_receiver} then right-click to start a
 * typing session. Valid codes are {@code "wasd"}, {@code "wdsa"}, and {@code "dd"}.
 * Each lifecycle hook logs to the "Stratagems" logger so the flow can be verified end-to-end.
 */
public class DemoCodeReceiver extends BlockEntity implements StratagemCodeReceiver
{
    public static BlockEntityType<DemoCodeReceiver> TYPE;
    public static net.minecraft.world.level.block.Block BLOCK_INSTANCE;

    private final StratagemCodeHandler codeHandler = new StratagemCodeHandler(this, Set.of("wasd", "wdsa", "dd"));

    public DemoCodeReceiver(BlockPos pos, BlockState state)
    {
        super(TYPE, pos, state);
    }

    @Override
    public StratagemCodeHandler codeHandler()
    {
        return this.codeHandler;
    }

    @Override
    public void onSessionStart(ServerPlayer player)
    {
        ModConstants.LOGGER.info("[DemoCodeReceiver] Session started by {} — type wasd, wdsa, or dd", player.getName().getString());
    }

    @Override
    public void onCodeMatched(ServerPlayer player, String matchedCode)
    {
        ModConstants.LOGGER.info("[DemoCodeReceiver] '{}' matched by {}!", matchedCode, player.getName().getString());
    }

    @Override
    public void onSessionCancelled(ServerPlayer player, StratagemCodeHandler.CancelReason reason)
    {
        ModConstants.LOGGER.info("[DemoCodeReceiver] Session cancelled for {} — reason: {}", player.getName().getString(), reason);
    }

    public static void register()
    {
        BLOCK_INSTANCE = Registry.register(BuiltInRegistries.BLOCK, ModConstants.id("demo_code_receiver"), new DemoBlock());
        TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModConstants.id("demo_code_receiver"),
                BlockEntityType.Builder.of(DemoCodeReceiver::new, BLOCK_INSTANCE).build());
    }

    public static class DemoBlock extends BaseEntityBlock
    {
        public DemoBlock()
        {
            super(BlockBehaviour.Properties.of());
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
        {
            return new DemoCodeReceiver(pos, state);
        }
    }
}
