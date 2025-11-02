package com.stevekung.stratagems.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StratagemOffset(
        float forward,
        float backward,
        float upward,
        float downward,
        float yaw,
        float pitch
) {
    public static final StratagemOffset EMPTY = new StratagemOffset(0f, 0f, 0f, 0f, 0f, 0f);

    public static final Codec<StratagemOffset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("forward").forGetter(StratagemOffset::forward),
            Codec.FLOAT.fieldOf("backward").forGetter(StratagemOffset::backward),
            Codec.FLOAT.fieldOf("upward").forGetter(StratagemOffset::upward),
            Codec.FLOAT.fieldOf("downward").forGetter(StratagemOffset::downward),
            Codec.FLOAT.fieldOf("yaw").forGetter(StratagemOffset::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(StratagemOffset::pitch)
    ).apply(instance, StratagemOffset::new));
}
