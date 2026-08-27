package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C — sent by the server to confirm that a code-typing session has started for the receiving
 * player. The client uses this to begin routing arrow-key presses to the remote receiver instead
 * of the personal stratagem HUD.
 */
public record CodeSessionStartedPacket() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CodeSessionStartedPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.CODE_SESSION_STARTED);
    public static final StreamCodec<FriendlyByteBuf, CodeSessionStartedPacket> CODEC = CustomPacketPayload.codec(CodeSessionStartedPacket::write, CodeSessionStartedPacket::new);

    private CodeSessionStartedPacket(FriendlyByteBuf buffer)
    {
        this();
    }

    private void write(FriendlyByteBuf buffer)
    {
    }

    @Override
    public CustomPacketPayload.Type<CodeSessionStartedPacket> type()
    {
        return TYPE;
    }
}
