package me.beanes.acid.player.tracker.impl.entity;

import com.github.retrooper.packetevents.util.Vector3d;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class TrackedEntity {
    @Getter
    private final LinkedList<TrackedPosition> trackedPositions = new LinkedList<>();
    private Vector3d serverPos;
    @Getter @Setter
    public int lastPreTransaction;
    private boolean lastWasTeleport = false;

    public TrackedEntity(Vector3d position) {
        trackedPositions.add(new TrackedPosition(position.getX(), position.getY(), position.getZ(), position.getX(), position.getY(), position.getZ(), 0));
        serverPos = position;
    }

    public void onTeleport(Vector3d position) {
        serverPos = position;

        lastWasTeleport = true;
        List<TrackedPosition> newTrackedPositions = new ArrayList<>();

        for (TrackedPosition trackedPosition : trackedPositions) {
            if (Math.abs(trackedPosition.getCurrentX() - serverPos.getX()) < 0.03125D && Math.abs(trackedPosition.getCurrentY() - serverPos.getY()) < 0.015625D && Math.abs(trackedPosition.getCurrentZ() - serverPos.getZ()) < 0.03125D) {
                newTrackedPositions.add(new TrackedPosition(
                        trackedPosition.getCurrentX(),
                        trackedPosition.getCurrentY(),
                        trackedPosition.getCurrentZ(),
                        trackedPosition.getCurrentX(),
                        trackedPosition.getCurrentY(),
                        trackedPosition.getCurrentZ(),
                        3 // Not really needed but mc does this also
                ));
            } else {
                newTrackedPositions.add(new TrackedPosition(
                        trackedPosition.getCurrentX(),
                        trackedPosition.getCurrentY(),
                        trackedPosition.getCurrentZ(),
                        serverPos.getX(),
                        serverPos.getY(),
                        serverPos.getZ(),
                        3
                ));
            }

            // We are not sure if we can remove the old position because it might be that the client did not receive the location yet, so we wait for sandwich
            trackedPosition.setRemoveAfterSandwich(true);
        }

        trackedPositions.addAll(newTrackedPositions);
        this.removeDuplicates();
    }

    public void onRelativeMove(double x, double y, double z) {
        lastWasTeleport = false;
        serverPos = serverPos.add(new Vector3d(x, y, z));
        List<TrackedPosition> newTrackedPositions = new ArrayList<>();

        for (TrackedPosition trackedPosition : trackedPositions) {
            newTrackedPositions.add(new TrackedPosition(
                    trackedPosition.getCurrentX(),
                    trackedPosition.getCurrentY(),
                    trackedPosition.getCurrentZ(),
                    serverPos.getX(),
                    serverPos.getY(),
                    serverPos.getZ(),
                    3
            ));

            // We are not sure if we can remove the old position because it might be that the client did not receive the location yet, so we wait for sandwich (the second transaction)
            trackedPosition.setRemoveAfterSandwich(true);
        }

        trackedPositions.addAll(newTrackedPositions);
        this.removeDuplicates();
    }

    public void confirm() {
        // The client has processed the last new movement and we made every possibility so we can say bye bye to all the old movements
        trackedPositions.removeIf(TrackedPosition::isRemoveAfterSandwich);
    }

    public void onClientTick() {
        List<TrackedPosition> newTrackedPositions = new ArrayList<>();

        for (TrackedPosition trackedPosition : trackedPositions) {
            trackedPosition.onTick();
            // (this is the fun part!)
            // So at this state we are not sure if the client received the new position
            // The problem is if the client has not received it we need to make a new possibility with the new current positions
            // We also have to keep the potential old possibilities incase the new relative position was received but the second transaction was split
            if (trackedPosition.isRemoveAfterSandwich()) {
                // Redo the teleport check because it's possible that the player is actually close enough now because of interpolation
                if (lastWasTeleport && Math.abs(trackedPosition.getCurrentX() - serverPos.getX()) < 0.03125D && Math.abs(trackedPosition.getCurrentY() - serverPos.getY()) < 0.015625D && Math.abs(trackedPosition.getCurrentZ() - serverPos.getZ()) < 0.03125D) {
                    newTrackedPositions.add(new TrackedPosition(
                            trackedPosition.getCurrentX(),
                            trackedPosition.getCurrentY(),
                            trackedPosition.getCurrentZ(),
                            trackedPosition.getCurrentX(),
                            trackedPosition.getCurrentY(),
                            trackedPosition.getCurrentZ(),
                            3 // Not needed
                    ));
                } else {
                    newTrackedPositions.add(new TrackedPosition(
                            trackedPosition.getCurrentX(),
                            trackedPosition.getCurrentY(),
                            trackedPosition.getCurrentZ(),
                            serverPos.getX(),
                            serverPos.getY(),
                            serverPos.getZ(),
                            3
                    ));
                }
            }
        }

        trackedPositions.addAll(newTrackedPositions);

        this.removeDuplicates();
    }

    public void removeDuplicates() {
        LongSet whatDoWeHave = new LongOpenHashSet();

        Iterator<TrackedPosition> iterator = trackedPositions.iterator();
        while (iterator.hasNext()) {
            TrackedPosition trackedPosition = iterator.next();
            long hash = trackedPosition.hashCode();

            // The one false possibility would be a hash collision...
            if (whatDoWeHave.contains(hash)) {
                iterator.remove();
                continue;
            }

            whatDoWeHave.add(hash);
        }
    }
}
