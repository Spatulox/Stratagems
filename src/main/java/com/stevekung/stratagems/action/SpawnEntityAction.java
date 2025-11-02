package com.stevekung.stratagems.action;

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

public record SpawnEntityAction(
        EntityType<?> entityType,
        StratagemOffset decalage
) implements StratagemAction
{
    public static final MapCodec<SpawnEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id")
                    .forGetter(action -> BuiltInRegistries.ENTITY_TYPE.getKey(action.entityType())),
            StratagemOffset.CODEC.optionalFieldOf("decalage", StratagemOffset.EMPTY)
                    .forGetter(SpawnEntityAction::decalage)
    ).apply(instance, (id, decalage) -> {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceKey.create(Registries.ENTITY_TYPE, id));
        if (type == null) {
            throw new IllegalArgumentException("Unknown entity type: " + id);
        }
        return new SpawnEntityAction(type, decalage);
    }));


    private static void spawnEntityD(
            StratagemActionContext context,
            EntityType<?> entityType,
            BlockPos pos,
            StratagemOffset decalage)
    {
        var level = context.level();
        if (level.isClientSide()) return;

        var entity = entityType.create(level);
        if (entity == null) return;

        float baseYaw = context.yRot() != null ? -context.yRot() : Direction.NORTH.toYRot();
        float finalYaw = decalage.yaw() + baseYaw;
        float finalPitch = decalage.pitch();

        float yawRad = (float) Math.toRadians(finalYaw);

        float horizontalOffset = decalage.forward() - decalage.backward();
        double offsetX = Math.sin(yawRad) * horizontalOffset;
        double offsetZ = -Math.cos(yawRad) * horizontalOffset;
        float verticalOffset = decalage.upward() - decalage.downward();

        double finalY = pos.getY() + verticalOffset;

        entity.moveTo(pos.getX() + 0.5 + offsetX, finalY, pos.getZ() + 0.5 + offsetZ, finalYaw, finalPitch);
        entity.setYRot(finalYaw);
        entity.setXRot(finalPitch);
        entity.setYHeadRot(finalYaw);

        level.addFreshEntity(entity);
    }

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_ENTITY;
    }

    @Override
    public void action(StratagemActionContext context) {
        spawnEntityD(context, entityType, context.blockPos(), decalage);
    }

    public static Builder spawnEntity(EntityType<?> entityType) {
        return () -> new SpawnEntityAction(entityType, StratagemOffset.EMPTY);
    }

    public static Builder spawnEntity(EntityType<?> entityType, StratagemOffset decalage) {
        return () -> new SpawnEntityAction(entityType, decalage);
    }
}
