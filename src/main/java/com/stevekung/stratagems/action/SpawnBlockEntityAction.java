package com.stevekung.stratagems.action;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.registry.StratagemActionTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public record SpawnBlockEntityAction(BlockState blockState) implements StratagemAction
{
    public static final MapCodec<SpawnBlockEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("block_state").forGetter(SpawnBlockEntityAction::blockState)
    ).apply(instance, SpawnBlockEntityAction::new));

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_BLOCK_ENTITY;
    }

    /*@Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        BlockPos pos = context.blockPos();

        level.setBlockAndUpdate(pos, this.blockState);

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null)
        {
            be.setChanged();
        }
    }*/

    @Override
    public void action(StratagemActionContext context)
    {
        var level = context.level();
        BlockPos pos = context.blockPos();

        BlockState state = this.blockState;
        state = state.rotate(Rotation.getRandom(context.random()));
        level.setBlockAndUpdate(pos, state);

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null)
        {
            be.setChanged();
        }
    }

    public static Builder spawnBlockEntity(BlockState blockState)
    {
        return () -> new SpawnBlockEntityAction(blockState);
    }
}