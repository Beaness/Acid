package me.beanes.acid.player.tracker.impl.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.tracker.Tracker;

public class EntityTracker extends Tracker {
    private final Int2ObjectMap<TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    public EntityTracker(PlayerData data) {
        super(data);
    }

    public TrackedEntity getTrackedEntity(int entityId) {
        return entityMap.get(entityId);
    }

    public void handleSpawnPlayer(WrapperPlayServerSpawnPlayer wrapper) {
        TrackedEntity trackedEntity = new TrackedEntity(wrapper.getPosition());
        int entityId = wrapper.getEntityId();

        entityMap.put(entityId, trackedEntity);
    }

    public void handleEntityTeleport(WrapperPlayServerEntityTeleport wrapper) {
        TrackedEntity trackedEntity = getTrackedEntity(wrapper.getEntityId());

        if (trackedEntity == null) {
            return;
        }

        checkForPluginStupidity(trackedEntity);

        trackedEntity.setLastPreTransaction(data.getTransactionTracker().getLastTransactionSent());

        Vector3d position = wrapper.getPosition();

        data.getTransactionTracker().pre(() -> {
            trackedEntity.onTeleport(position);
        });

        data.getTransactionTracker().post(trackedEntity::confirm);
    }

    public void handleEntityRelativeMove(WrapperPlayServerEntityRelativeMove wrapper) {
        handleRelativeMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    public void handleEntityRelativeMoveAndRotation(WrapperPlayServerEntityRelativeMoveAndRotation wrapper) {
        handleRelativeMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    public void handleEntityRotation(WrapperPlayServerEntityRotation wrapper) {
        handleRelativeMove(wrapper.getEntityId(), 0, 0, 0);
    }

    private void handleRelativeMove(int entityId, double deltaX, double deltaY, double deltaZ) {
        TrackedEntity trackedEntity = getTrackedEntity(entityId);

        if (trackedEntity == null) {
            return;
        }

        checkForPluginStupidity(trackedEntity);

        data.getTransactionTracker().pre(() -> {
            trackedEntity.onRelativeMove(deltaX, deltaY, deltaZ);
        });

        data.getTransactionTracker().post(trackedEntity::confirm);
    }

    public void handleClientTick() {
        // Don't interpolate for the response of a teleport
        if (data.getPositionTracker().isTeleport()) {
            return;
        }

        for (final TrackedEntity entity : entityMap.values()) {
            entity.onClientTick();
        }
    }

    private void checkForPluginStupidity(TrackedEntity trackedEntity) {
        // This should never happen because the spigot entity tracker should not send multiple position packets of an entity within the same tick
        // However some dumb plugin could do this by accident so to be safe we keep track of the last transaction
        if (trackedEntity.getLastPreTransaction() == data.getTransactionTracker().getLastTransactionSent()) {
            data.getTransactionTracker().sendTransaction(false);
        }

        trackedEntity.setLastPreTransaction(data.getTransactionTracker().getLastTransactionSent());
    }
}
