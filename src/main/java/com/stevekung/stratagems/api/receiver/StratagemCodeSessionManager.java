package com.stevekung.stratagems.api.receiver;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side registry that tracks which {@link StratagemCodeReceiver} each player is currently
 * linked to. All methods are called from the server thread via Fabric API event callbacks and
 * packet handlers registered in {@code StratagemsMod}.
 * <p>
 * Third-party mods may also call this directly to force-start or force-cancel a session from
 * their own game logic (e.g. cancel when a custom GUI closes).
 */
public class StratagemCodeSessionManager
{
    private record Link(StratagemCodeReceiver receiver, @Nullable Entity entity, @Nullable BlockPos blockPos)
    {}

    private static final ConcurrentHashMap<UUID, Link> LINKS = new ConcurrentHashMap<>();

    /**
     * Links {@code player} to {@code receiver} and starts a new typing session.
     * If the player was already linked to a different receiver, that session is cancelled first
     * with {@link StratagemCodeHandler.CancelReason#RELINKED_ELSEWHERE}.
     *
     * @param entity   the entity acting as receiver, or {@code null} if it is a block entity
     * @param blockPos the block entity position, or {@code null} if it is an entity
     */
    public static void startSession(ServerPlayer player, StratagemCodeReceiver receiver, @Nullable Entity entity, @Nullable BlockPos blockPos)
    {
        var previous = LINKS.put(player.getUUID(), new Link(receiver, entity, blockPos));
        if (previous != null && previous.receiver() != receiver)
        {
            previous.receiver().codeHandler().cancel(player, StratagemCodeHandler.CancelReason.RELINKED_ELSEWHERE);
        }
        receiver.codeHandler().startSession(player);
    }

    /**
     * Forwards a typed character to the player's currently linked receiver.
     * If the receiver is no longer valid (entity removed, block entity replaced), the session is
     * cancelled automatically before the character is processed.
     */
    public static void handleAppend(ServerPlayer player, char character)
    {
        var link = LINKS.get(player.getUUID());
        if (link == null)
        {
            return;
        }
        if (!isStillValid(player, link))
        {
            LINKS.remove(player.getUUID());
            link.receiver().codeHandler().cancel(player, StratagemCodeHandler.CancelReason.RECEIVER_INVALID);
            return;
        }
        if (link.receiver().codeHandler().append(player, character))
        {
            LINKS.remove(player.getUUID());
        }
    }

    /**
     * Explicitly cancels the player's current session with the given reason and removes the link.
     * No-op if the player has no active session.
     */
    public static void handleCancel(ServerPlayer player, StratagemCodeHandler.CancelReason reason)
    {
        var link = LINKS.remove(player.getUUID());
        if (link != null)
        {
            link.receiver().codeHandler().cancel(player, reason);
        }
    }

    /**
     * Must be called once per server tick (wired into {@code ServerTickEvents.START_SERVER_TICK}
     * in {@code StratagemsMod}). Enforces range, receiver validity, and idle timeout for all
     * currently linked players, cancelling stale sessions automatically.
     */
    public static void tickAll(MinecraftServer server)
    {
        LINKS.entrySet().removeIf(entry ->
        {
            var uuid = entry.getKey();
            var link = entry.getValue();
            var player = server.getPlayerList().getPlayer(uuid);

            if (player == null)
            {
                // Disconnect event fires first and removes the entry in normal flow;
                // this branch is a defensive fallback only (no hook fired, player is gone).
                link.receiver().codeHandler().cancel(null, StratagemCodeHandler.CancelReason.DISCONNECTED);
                return true;
            }
            if (!isStillValid(player, link))
            {
                link.receiver().codeHandler().cancel(player, StratagemCodeHandler.CancelReason.RECEIVER_INVALID);
                return true;
            }

            var distanceSqr = link.blockPos() != null
                    ? player.distanceToSqr(Vec3.atCenterOf(link.blockPos()))
                    : player.distanceToSqr(link.entity());

            if (link.receiver().codeHandler().isOutOfRange(distanceSqr))
            {
                link.receiver().codeHandler().cancel(player, StratagemCodeHandler.CancelReason.OUT_OF_RANGE);
                return true;
            }

            link.receiver().codeHandler().tick(player);
            // tick() may have internally cancelled (e.g. idle timeout); remove the entry if so.
            return !link.receiver().codeHandler().isLinked();
        });
    }

    private static boolean isStillValid(ServerPlayer player, Link link)
    {
        if (link.blockPos() != null)
        {
            return player.level().getBlockEntity(link.blockPos()) == link.receiver();
        }
        return link.entity() != null && !link.entity().isRemoved() && link.entity().level() == player.level();
    }
}
