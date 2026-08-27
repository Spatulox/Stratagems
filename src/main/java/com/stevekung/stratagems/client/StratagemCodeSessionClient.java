package com.stevekung.stratagems.client;

/**
 * Client-side flag tracking whether the local player currently has an active code-typing session
 * linked to a remote {@link com.stevekung.stratagems.api.receiver.StratagemCodeReceiver}.
 * <p>
 * When {@code true}, arrow-key presses in {@code StratagemsClientMod.clientTick} are forwarded
 * to the server as {@link com.stevekung.stratagems.api.packet.AppendCodeCharPacket} instead of
 * being consumed by the personal stratagem HUD.
 * <p>
 * Set to {@code true} on receipt of {@link com.stevekung.stratagems.api.packet.CodeSessionStartedPacket}
 * (server confirmation); set to {@code false} when the player explicitly cancels via the
 * stratagem-menu keybind.
 */
public class StratagemCodeSessionClient
{
    private static boolean linked;

    private StratagemCodeSessionClient()
    {
    }

    public static boolean isLinked()
    {
        return linked;
    }

    public static void setLinked(boolean linked)
    {
        StratagemCodeSessionClient.linked = linked;
    }
}
