package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.action.StratagemActionContext;
import com.stevekung.stratagems.api.action.StratagemActionType;
import com.stevekung.stratagems.registry.StratagemActionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.core.util.Builder;

public record SpawnEntityAction(EntityType<?> entityType) implements StratagemAction
{
    public static final MapCodec<SpawnEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(action -> BuiltInRegistries.ENTITY_TYPE.getKey(action.entityType()))
    ).apply(instance, (id) -> {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceKey.create(Registries.ENTITY_TYPE, id));
        if (type == null) {
            throw new IllegalArgumentException("Unknown entity type: " + id);
        }
        return new SpawnEntityAction(type);
    }));

    private static void spawnEntityD(Level level, EntityType<?> entityType, BlockPos pos)
    {
        if (!level.isClientSide())
        {
            var entity = entityType.create(level);
            if (entity != null)
            {
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
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
        spawnEntityD(context.level(), entityType, context.blockPos());
    }

    public static Builder spawnEntity(EntityType<?> entityType)
    {
        return () -> new SpawnEntityAction(entityType);
    }
}