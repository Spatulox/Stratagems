package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S — sent when the player explicitly cancels an active code-typing session (e.g. by pressing
 * the stratagem-menu key while linked to a
 * {@link com.stevekung.stratagems.api.receiver.StratagemCodeReceiver}).
 */
public record CancelCodeSessionPacket() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CancelCodeSessionPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.CANCEL_CODE_SESSION);
    public static final StreamCodec<FriendlyByteBuf, CancelCodeSessionPacket> CODEC = CustomPacketPayload.codec(CancelCodeSessionPacket::write, CancelCodeSessionPacket::new);

    private CancelCodeSessionPacket(FriendlyByteBuf buffer)
    {
        this();
    }

    private void write(FriendlyByteBuf buffer)
    {
    }

    @Override
    public CustomPacketPayload.Type<CancelCodeSessionPacket> type()
    {
        return TYPE;
    }
}
