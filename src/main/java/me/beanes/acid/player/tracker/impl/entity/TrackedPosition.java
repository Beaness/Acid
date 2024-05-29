package me.beanes.acid.player.tracker.impl.entity;

import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

public class TrackedPosition {
    @Getter
    private double currentX, currentY, currentZ; // x, y, z
    @Getter
    private final double targetX, targetY, targetZ; // tpX, tpY, tpZ
    @Getter @Setter
    private int interpolateTicks;
    @Getter @Setter
    private boolean removeAfterSandwich = false;
    @Getter @Setter
    private BoundingBox cachedBoundingBox = null;

    public TrackedPosition(double currentX, double currentY, double currentZ, double targetX, double targetY, double targetZ, int interpolateTicks) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.currentZ = currentZ;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.interpolateTicks = interpolateTicks;
    }

    public void onTick() {
        if (interpolateTicks > 0) {
            currentX = currentX + (targetX - currentX) / interpolateTicks;
            currentY = currentY + (targetY - currentY) / interpolateTicks;
            currentZ = currentZ + (targetZ - currentZ) / interpolateTicks;

            this.cachedBoundingBox = null; // Invalidate cached bounding box

            --interpolateTicks;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(currentX, currentY, currentZ, targetX, targetY, targetZ, removeAfterSandwich, interpolateTicks);
    }

    public TrackedPosition clone() {
        return new TrackedPosition(this.currentX, this.currentY, this.currentZ, this.targetX, this.targetY, this.targetZ, this.interpolateTicks);
    }
}
