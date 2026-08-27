package com.stevekung.stratagems.api.receiver;

import net.minecraft.server.level.ServerPlayer;

/**
 * Implement directly on a custom {@code Entity} or {@code BlockEntity} subclass to give it the
 * ability to receive a typed wasd code from a nearby player.
 * <p>
 * This capability is completely independent of the {@link com.stevekung.stratagems.api.Stratagem}
 * registry, {@link com.stevekung.stratagems.api.StratagemsData}, cooldowns, and the
 * {@code StratagemBall}/{@code StratagemPod} pipeline. What happens inside each hook is entirely
 * up to the implementing mod.
 * <p>
 * Minimal implementation pattern:
 * <pre>{@code
 * public class MyCoolEntity extends Monster implements StratagemCodeReceiver
 * {
 *     private final StratagemCodeHandler codeHandler = new StratagemCodeHandler(this, Set.of("wasd", "ssdd"));
 *
 *     @Override
 *     public StratagemCodeHandler codeHandler() { return this.codeHandler; }
 *
 *     @Override
 *     public void onCodeMatched(ServerPlayer player, String matchedCode)
 *     {
 *         // do whatever you want here
 *     }
 * }
 * }</pre>
 *
 * A player right-clicks the entity/block to start a typing session; their w/a/s/d keys are then
 * forwarded to {@link StratagemCodeHandler#append(ServerPlayer, char)} by the mod's networking
 * layer until a code matches, the player cancels, or the session times out.
 *
 * @see StratagemCodeHandler
 * @see StratagemCodeSessionManager
 */
public interface StratagemCodeReceiver
{
    /**
     * Returns the backing state machine for this receiver. The implementing class should store one
     * {@link StratagemCodeHandler} instance as a field, construct it in its constructor, and return
     * it here — exactly as {@code VariantHolder} implementations store a synced-data holder.
     */
    StratagemCodeHandler codeHandler();

    /**
     * Called server-side when a player successfully right-clicks and a typing session begins.
     * Corresponds to the start of the code-input phase (the {@code action()} in the original design spec).
     *
     * @param player the player who started typing
     */
    default void onSessionStart(ServerPlayer player)
    {
    }

    /**
     * Called server-side when the accumulated code exactly matches one of the receiver's
     * {@link StratagemCodeHandler#validCodes()}. The session is already closed when this fires.
     * Corresponds to {@code trigger()} in the original design spec.
     *
     * @param player      the player who typed the matching code
     * @param matchedCode the code that matched
     */
    default void onCodeMatched(ServerPlayer player, String matchedCode)
    {
    }

    /**
     * Called server-side when a session ends without a match. Corresponds to {@code cancel()} in
     * the original design spec. The {@link StratagemCodeHandler.CancelReason} parameter lets
     * implementors distinguish the cause without needing separate hook methods.
     *
     * @param player the player who was linked at the time of cancellation
     * @param reason why the session was cancelled
     */
    default void onSessionCancelled(ServerPlayer player, StratagemCodeHandler.CancelReason reason)
    {
    }
}
