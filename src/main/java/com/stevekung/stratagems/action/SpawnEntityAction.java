package com.stevekung.stratagems.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stevekung.stratagems.api.ModConstants;
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
        StratagemOffset offset
) implements StratagemAction
{
    public static final MapCodec<SpawnEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id")
                    .forGetter(action -> BuiltInRegistries.ENTITY_TYPE.getKey(action.entityType())),
            StratagemOffset.CODEC.optionalFieldOf("offset", StratagemOffset.EMPTY)
                    .forGetter(SpawnEntityAction::offset)
    ).apply(instance, (id, offset) -> {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceKey.create(Registries.ENTITY_TYPE, id));
        if (type == null) {
            throw new IllegalArgumentException("Unknown entity type: " + id);
        }
        return new SpawnEntityAction(type, offset);
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

        float horizontalOffset = - (decalage.forward() - decalage.backward());
        double offsetX = Math.sin(yawRad) * horizontalOffset;
        double offsetZ = -Math.cos(yawRad) * horizontalOffset;
        float verticalOffset = decalage.upward() - decalage.downward();

        double finalX = pos.getX() + 0.5 + offsetX;
        double finalY = pos.getY() + verticalOffset;
        double finalZ = pos.getZ() + 0.5 + offsetZ;

        entity.moveTo(finalX, finalY, finalZ, finalYaw, finalPitch);
        entity.setYRot(finalYaw);
        entity.setXRot(finalPitch);
        entity.setYHeadRot(finalYaw);
        String posTag = String.format("original_beacon_position;x,y,z,yaw,pitch;%d:%d:%d:%f:%f", pos.getX(), pos.getY(), pos.getZ(), finalYaw, finalPitch);
        entity.addTag(posTag);
        level.addFreshEntity(entity);
    }

    @Override
    public StratagemActionType getType()
    {
        return StratagemActionTypes.SPAWN_ENTITY;
    }

    @Override
    public void action(StratagemActionContext context) {
        spawnEntityD(context, entityType, context.blockPos(), offset);
    }

    public static Builder spawnEntity(EntityType<?> entityType) {
        return () -> new SpawnEntityAction(entityType, StratagemOffset.EMPTY);
    }

    public static Builder spawnEntity(EntityType<?> entityType, StratagemOffset offset) {
        return () -> new SpawnEntityAction(entityType, offset);
    }
}
