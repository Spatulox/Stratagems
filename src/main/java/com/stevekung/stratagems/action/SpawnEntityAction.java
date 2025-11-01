package com.stevekung.stratagems.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.registry.StratagemActionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.logging.log4j.core.util.Builder;

public record SpawnEntityAction(EntityType<?> entityType, float height, boolean relativeHeight) implements StratagemAction
{
    public static final MapCodec<SpawnEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id")
                    .forGetter(action -> BuiltInRegistries.ENTITY_TYPE.getKey(action.entityType())),
            Codec.FLOAT.optionalFieldOf("height", 0.0f)
                    .forGetter(SpawnEntityAction::height),
            Codec.BOOL.optionalFieldOf("relative", false)
                    .forGetter(SpawnEntityAction::relativeHeight)
    ).apply(instance, (id, height, relative) -> {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceKey.create(Registries.ENTITY_TYPE, id));
        if (type == null) {
            throw new IllegalArgumentException("Unknown entity type: " + id);
        }
        return new SpawnEntityAction(type, height, relative);
    }));

    private static void spawnEntityD(StratagemActionContext context, EntityType<?> entityType, BlockPos pos, float height, boolean relative)
    {
        var level = context.level();
        if (!level.isClientSide())
        {
            var entity = entityType.create(level);
            if (entity != null)
            {
                // For some reason, EAST/WESt are inverted, applaying *-1 helps it to be at the right angle
                float yaw = context.yRot() != null ? -context.yRot() : Direction.NORTH.toYRot(); // Should face the player, but don't, the entity face the same direction as the player for NORTH and SOUTH :/
                double finalY = relative ? (pos.getY() + height) : height;
                entity.moveTo(pos.getX() + 0.5, finalY, pos.getZ() + 0.5, yaw, 0.0f);
                entity.setYRot(yaw);
                entity.setYHeadRot(yaw);
                level.addFreshEntity(entity);
            }
        }
    }

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_ENTITY;
    }

    @Override
    public void action(StratagemActionContext context)
    {
        spawnEntityD(context, entityType, context.blockPos(), height, relativeHeight);
    }

    public static Builder spawnEntity(EntityType<?> entityType)
    {
        return () -> new SpawnEntityAction(entityType, 0.1f, true);
    }

    public static Builder spawnEntity(EntityType<?> entityType, double height, boolean relative)
    {
        return () -> new SpawnEntityAction(entityType, (float)height, relative);
    }
}
