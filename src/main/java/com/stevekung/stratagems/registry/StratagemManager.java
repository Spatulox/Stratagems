package com.stevekung.stratagems.registry;

import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemInstance;
import com.stevekung.stratagems.api.StratagemModifier;
import com.stevekung.stratagems.api.packet.ClearStratagemsPacket;
import com.stevekung.stratagems.api.packet.UpdateStratagemPacket;
import com.stevekung.stratagems.api.references.ModRegistries;
import com.stevekung.stratagems.api.util.PacketUtils;
import com.stevekung.stratagems.api.util.StratagemUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Please, do not use this class, prefer using the Stratagems class
 */
public class StratagemManager {
    public static boolean add(ServerPlayer player, Holder<Stratagem> stratagem) {
        var stratagemsData = player.stratagemsData();
        if (StratagemUtils.anyMatch(stratagemsData, stratagem)) {
            return false;
        }
        stratagemsData.add(stratagem);
        PacketUtils.sendClientUpdateStratagemPacket(player.server, player, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean add(MinecraftServer server, Holder<Stratagem> stratagem) {
        var stratagemsData = server.overworld().stratagemsData();
        if (StratagemUtils.anyMatch(stratagemsData, stratagem)) {
            return false;
        }
        stratagemsData.add(stratagem);
        PacketUtils.sendClientUpdateStratagemPacket(server, null, UpdateStratagemPacket.Action.ADD, stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean remove(ServerPlayer player, Holder<Stratagem> stratagem) {
        var stratagemsData = player.stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        PacketUtils.sendClientUpdateStratagemPacket(player.server, player, UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(stratagem));
        stratagemsData.remove(stratagem);
        return true;
    }

    public static boolean remove(MinecraftServer server, Holder<Stratagem> stratagem) {
        var stratagemsData = server.overworld().stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        PacketUtils.sendClientUpdateStratagemPacket(server, null, UpdateStratagemPacket.Action.REMOVE, stratagemsData.instanceByHolder(stratagem));
        stratagemsData.remove(stratagem);
        return true;
    }

    public static boolean removeAll(ServerPlayer player) {
        player.stratagemsData().clear();
        player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(false, true, player.getUUID())));
        return true;
    }

    public static boolean removeAll(MinecraftServer server) {
        server.overworld().stratagemsData().clear();
        for (var player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundCustomPayloadPacket(new ClearStratagemsPacket(true)));
        }
        return true;
    }

    public static List<StratagemInstance> list(ServerPlayer player) {
        return new ArrayList<>(player.stratagemsData().listInstances());
    }

    public static List<StratagemInstance> list(MinecraftServer server) {
        return new ArrayList<>(server.overworld().stratagemsData().listInstances());
    }

    public static boolean block(ServerPlayer player, Holder<Stratagem> stratagem, boolean unblock) {
        var stratagemsData = player.stratagemsData();

        if (unblock) {
            stratagemsData.block(stratagem, true); // unblock via block with unblock = true
        } else {
            stratagemsData.block(stratagem, false);
        }

        PacketUtils.sendClientUpdateStratagemPacket(player.server, player,
                UpdateStratagemPacket.Action.UPDATE,
                stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean block(MinecraftServer server, Holder<Stratagem> stratagem, boolean unblock) {
        var stratagemsData = server.overworld().stratagemsData();

        if (unblock) {
            stratagemsData.block(stratagem, true);
        } else {
            stratagemsData.block(stratagem, false);
        }

        PacketUtils.sendClientUpdateStratagemPacket(server, null,
                UpdateStratagemPacket.Action.UPDATE,
                stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean reset(ServerPlayer player, Holder<Stratagem> stratagem) {
        var stratagemsData = player.stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        stratagemsData.reset(stratagem);
        PacketUtils.sendClientUpdateStratagemPacket(player.server, player, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean reset(MinecraftServer server, Holder<Stratagem> stratagem) {
        var stratagemsData = server.overworld().stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        stratagemsData.reset(stratagem);
        PacketUtils.sendClientUpdateStratagemPacket(server, null, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean setModifier(ServerPlayer player, Holder<Stratagem> stratagem, StratagemModifier modifier, boolean clear) {
        var stratagemsData = player.stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        if (clear) {
            stratagemsData.modified(stratagem, modifier, true);
        } else {
            stratagemsData.modified(stratagem, modifier, false);
        }
        PacketUtils.sendClientUpdateStratagemPacket(player.server, player, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(stratagem));
        return true;
    }

    public static boolean setModifier(MinecraftServer server, Holder<Stratagem> stratagem, StratagemModifier modifier, boolean clear) {
        var stratagemsData = server.overworld().stratagemsData();
        if (StratagemUtils.noneMatch(stratagemsData, stratagem)) {
            return false;
        }
        if (clear) {
            stratagemsData.modified(stratagem, modifier, true);
        } else {
            stratagemsData.modified(stratagem, modifier, false);
        }
        PacketUtils.sendClientUpdateStratagemPacket(server, null, UpdateStratagemPacket.Action.UPDATE, stratagemsData.instanceByHolder(stratagem));
        return true;
    }
}