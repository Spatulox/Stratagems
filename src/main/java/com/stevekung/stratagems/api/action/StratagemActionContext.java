package com.stevekung.stratagems.api.action;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @param serverPlayer the player
 * @param level the world
 * @param blockPos block pos where to spawn the stratagem
 * @param random gen random number, probably for the loot chest ?
 * @param yRot to the spawn direction of the block/entity. You can't use the serverPlayer.direction because it's linked to the owner of the stratagem, and live changing
 */
public record StratagemActionContext(ServerPlayer serverPlayer, ServerLevel level, BlockPos blockPos, RandomSource random, @Nullable Float yRot)
{}