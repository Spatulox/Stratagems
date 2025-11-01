package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.registry.StratagemActionTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record SpawnBlockAction(BlockState blockState) implements StratagemAction
{
    public static final MapCodec<SpawnBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block_state").forGetter(SpawnBlockAction::blockState)
    ).apply(instance, SpawnBlockAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_BLOCK_ENTITY;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        BlockPos pos = context.blockPos();

        var yRot = context.direction() != null ? context.direction().toYRot() : Direction.NORTH.toYRot();

        BlockState state = this.blockState;
        state = state.setValue(BlockStateProperties.FACING,Direction.fromYRot(yRot));
        level.setBlockAndUpdate(pos, state);

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null)
        {
            be.setChanged();
        }
    }

    public static Builder spawnBlock(BlockState blockState)
    {
        return () -> new SpawnBlockAction(blockState);
    }
}