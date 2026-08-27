package com.stevekung.stratagems.api.receiver;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerPlayer;

/**
 * Mutable state machine that tracks an in-progress typed code for a {@link StratagemCodeReceiver}.
 * One instance per receiver; the implementing class constructs and holds it as a field and returns
 * it from {@link StratagemCodeReceiver#codeHandler()}.
 * <p>
 * Valid codes are plain {@code wasd} strings supplied at construction — completely independent of
 * the {@link com.stevekung.stratagems.api.Stratagem} registry and its cooldown/action pipeline.
 */
public class StratagemCodeHandler
{
    public static final int DEFAULT_IDLE_TIMEOUT_TICKS = 200;
    public static final double DEFAULT_MAX_RANGE_BLOCKS = 6.0d;

    private final StratagemCodeReceiver receiver;
    private final Set<String> validCodes;
    private final int idleTimeoutTicks;
    private final double maxRangeSqr;

    private String code = "";
    @Nullable
    private UUID linkedPlayerUuid;
    private int idleTicks;

    public StratagemCodeHandler(StratagemCodeReceiver receiver, Set<String> validCodes)
    {
        this(receiver, validCodes, DEFAULT_IDLE_TIMEOUT_TICKS, DEFAULT_MAX_RANGE_BLOCKS);
    }

    public StratagemCodeHandler(StratagemCodeReceiver receiver, Set<String> validCodes, int idleTimeoutTicks, double maxRangeBlocks)
    {
        this.receiver = receiver;
        this.validCodes = ImmutableSet.copyOf(validCodes);
        this.idleTimeoutTicks = idleTimeoutTicks;
        this.maxRangeSqr = maxRangeBlocks * maxRangeBlocks;
    }

    public Set<String> validCodes()
    {
        return this.validCodes;
    }

    public boolean isLinked()
    {
        return this.linkedPlayerUuid != null;
    }

    public boolean isLinkedTo(ServerPlayer player)
    {
        return this.linkedPlayerUuid != null && this.linkedPlayerUuid.equals(player.getUUID());
    }

    @Nullable
    public UUID linkedPlayerUuid()
    {
        return this.linkedPlayerUuid;
    }

    public String code()
    {
        return this.code;
    }

    /**
     * Starts a new session for the given player. Any previously accumulated code is discarded.
     * Fires {@link StratagemCodeReceiver#onSessionStart(ServerPlayer)}.
     */
    public void startSession(ServerPlayer player)
    {
        this.linkedPlayerUuid = player.getUUID();
        this.code = "";
        this.idleTicks = 0;
        this.receiver.onSessionStart(player);
    }

    /**
     * Appends a character to the accumulated code, only if {@code player} is the currently linked player.
     * <ul>
     *   <li>If the result exactly matches one of {@link #validCodes()}: ends the session and fires
     *       {@link StratagemCodeReceiver#onCodeMatched(ServerPlayer, String)}, then returns {@code true}.</li>
     *   <li>If no valid code starts with the new prefix (dead end): resets the code to empty while
     *       keeping the session alive (soft reset — mirrors how the player HUD clears {@code inputCode}
     *       on a dead-end without closing the menu).</li>
     *   <li>Otherwise: accumulates and returns {@code false}.</li>
     * </ul>
     *
     * @return {@code true} if the append produced an exact match and the session has been ended.
     */
    public boolean append(ServerPlayer player, char c)
    {
        if (!this.isLinkedTo(player))
        {
            return false;
        }
        this.idleTicks = 0;
        this.code += c;

        if (this.validCodes.contains(this.code))
        {
            var matched = this.code;
            this.endSession();
            this.receiver.onCodeMatched(player, matched);
            return true;
        }
        if (this.validCodes.stream().noneMatch(valid -> valid.startsWith(this.code)))
        {
            this.code = "";
        }
        return false;
    }

    /**
     * Cancels an active session. Fires {@link StratagemCodeReceiver#onSessionCancelled} only when
     * {@code player} is non-null. Passing {@code null} silently cleans up (used as a defensive
     * fallback when the player is no longer available, e.g. already disconnected).
     */
    public void cancel(@Nullable ServerPlayer player, CancelReason reason)
    {
        if (!this.isLinked())
        {
            return;
        }
        this.endSession();
        if (player != null)
        {
            this.receiver.onSessionCancelled(player, reason);
        }
    }

    /**
     * Advances the idle-timeout counter. Call once per server tick while a session is active
     * (this is handled automatically by {@link StratagemCodeSessionManager#tickAll}, but receiver
     * implementations that manage their own ticking can also call this directly).
     *
     * @param linkedPlayer the currently linked player; if {@code null}, silently ends the session
     *                     without firing the cancel hook (defensive fallback for disconnect races).
     */
    public void tick(@Nullable ServerPlayer linkedPlayer)
    {
        if (!this.isLinked())
        {
            return;
        }
        if (linkedPlayer == null)
        {
            this.endSession();
            return;
        }
        if (++this.idleTicks > this.idleTimeoutTicks)
        {
            this.cancel(linkedPlayer, CancelReason.IDLE_TIMEOUT);
        }
    }

    /**
     * @param distanceSqr squared distance between the linked player and this receiver's position
     * @return {@code true} if the player has moved beyond the configured maximum range
     */
    public boolean isOutOfRange(double distanceSqr)
    {
        return distanceSqr > this.maxRangeSqr;
    }

    private void endSession()
    {
        this.linkedPlayerUuid = null;
        this.code = "";
        this.idleTicks = 0;
    }

    public enum CancelReason
    {
        PLAYER_CANCELLED,
        OUT_OF_RANGE,
        DISCONNECTED,
        RECEIVER_INVALID,
        IDLE_TIMEOUT,
        RELINKED_ELSEWHERE
    }
}
