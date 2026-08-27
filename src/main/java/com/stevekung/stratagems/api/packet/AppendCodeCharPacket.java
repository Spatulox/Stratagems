package com.stevekung.stratagems.api.packet;

import com.stevekung.stratagems.api.ModConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * C2S — sent once per arrow-key press while the player has an active code-typing session
 * linked to a {@link com.stevekung.stratagems.api.receiver.StratagemCodeReceiver}.
 */
public record AppendCodeCharPacket(char character) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<AppendCodeCharPacket> TYPE = new CustomPacketPayload.Type<>(ModConstants.Packets.APPEND_CODE_CHAR);
    public static final StreamCodec<FriendlyByteBuf, AppendCodeCharPacket> CODEC = CustomPacketPayload.codec(AppendCodeCharPacket::write, AppendCodeCharPacket::new);

    private AppendCodeCharPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readChar());
    }

    private void write(FriendlyByteBuf buffer)
    {
        buffer.writeChar(this.character);
    }

    @Override
    public CustomPacketPayload.Type<AppendCodeCharPacket> type()
    {
        return TYPE;
    }
}
