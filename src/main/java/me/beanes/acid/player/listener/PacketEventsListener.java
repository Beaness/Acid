package me.beanes.acid.player.listener;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.AllArgsConstructor;
import me.beanes.acid.Acid;
import me.beanes.acid.check.Check;
import me.beanes.acid.player.PlayerData;
import org.bukkit.Bukkit;

@AllArgsConstructor
public class PacketEventsListener extends PacketListenerAbstract {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PlayerData data = Acid.get().getPlayerManager().get(event.getUser());
        PacketTypeCommon type = event.getPacketType();

        boolean clientTick = WrapperPlayClientPlayerFlying.isFlying(type);

        if (clientTick) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            data.getPositionTracker().handleClientTick(wrapper);
            data.getRotationTracker().handleClientTick(wrapper);
        } else if (type == Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);
            data.getTransactionTracker().handleClientTransaction(wrapper);
        }

        for (Check check : data.getChecks()) {
            check.onPacketReceive(event);
        }

        if (clientTick) {
            data.getEntityTracker().handleClientTick();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PlayerData data = Acid.get().getPlayerManager().get(event.getUser());
        PacketTypeCommon type = event.getPacketType();

        if (type == Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);

            data.getPositionTracker().handleServerTeleport(wrapper);
        } else if (type == Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation wrapper = new WrapperPlayServerWindowConfirmation(event);
            data.getTransactionTracker().handleServerTransaction(wrapper);
        } else if (type == Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(event);
            data.getEntityTracker().handleSpawnPlayer(wrapper);
        } else if (type == Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);
            data.getEntityTracker().handleEntityTeleport(wrapper);
        } else if (type == Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);
            data.getEntityTracker().handleEntityRelativeMove(wrapper);
        } else if (type == Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            data.getEntityTracker().handleEntityRelativeMoveAndRotation(wrapper);
        } else if (type == Server.ENTITY_ROTATION) {
            WrapperPlayServerEntityRotation wrapper = new WrapperPlayServerEntityRotation(event);
            data.getEntityTracker().handleEntityRotation(wrapper);
        }
    }

    @Override
    public void onUserConnect(UserConnectEvent event) {
        // We hook into the connect event because we want to register on the netty thread (because of the ThreadLocal in PlayerManager)
        PlayerData data = new PlayerData(event.getUser());
        Acid.get().getPlayerManager().add(data);

        // Also add the player to the server cache thread
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            Acid.get().getPlayerManager().add(data);
        });
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        Acid.get().getPlayerManager().remove(event.getUser());

        // Also remove from the server thread cache
        Bukkit.getScheduler().runTask(Acid.get(), () -> {
            Acid.get().getPlayerManager().remove(event.getUser());
        });
    }
}
