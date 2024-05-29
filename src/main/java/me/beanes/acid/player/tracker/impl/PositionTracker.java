package me.beanes.acid.player.tracker.impl;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import lombok.Getter;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.tracker.Tracker;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Queue;

public class PositionTracker extends Tracker {
    public PositionTracker(PlayerData data) {
        super(data);
    }

    @Getter
    private double x, y, z, lastX, lastY, lastZ;
    @Getter
    private double deltaX, deltaY, deltaZ;
    private boolean onGround;
    @Getter
    private boolean teleport = false;
    @Getter
    private boolean lastTickHasPosition = false;

    private final Queue<Location> teleports = new ArrayDeque<>();

    public void handleClientTick(WrapperPlayClientPlayerFlying wrapper) {
        this.teleport = false;
        this.lastTickHasPosition = wrapper.hasPositionChanged();
        if (this.lastTickHasPosition) {
            this.lastX = x;
            this.lastY = y;
            this.lastZ = z;

            this.x = wrapper.getLocation().getX();
            this.y = wrapper.getLocation().getY();
            this.z = wrapper.getLocation().getZ();

            this.deltaX = this.x - this.lastX;
            this.deltaY = this.y - this.lastY;
            this.deltaZ = this.z - this.lastZ;

            // Check if this move was a teleport
            if (wrapper.hasRotationChanged()) {
                final Location teleportLocation = this.teleports.peek();

                if (teleportLocation != null &&
                        teleportLocation.getX() == this.x &&
                        teleportLocation.getY() == this.y &&
                        teleportLocation.getZ() == this.z &&
                        teleportLocation.getYaw() == wrapper.getLocation().getYaw() &&
                        teleportLocation.getPitch() == wrapper.getLocation().getPitch()
                    // We could also check if onGround == false but 1.7 deals different with this
                ) {
                    this.teleports.remove();
                    Bukkit.broadcastMessage("last move was tp!");
                    this.teleport = true;
                }
            }
        }

        // If the client teleported we can't trust the updated onGround value (the client sends onGround state false on a teleport)
        if (!this.teleport) {
            this.onGround = wrapper.isOnGround();
        }
    }

    public void handleServerTeleport(WrapperPlayServerPlayerPositionAndLook wrapper) {
        // TODO: handle relative teleports
        Location pos = new Location(wrapper.getX(), wrapper.getY(), wrapper.getZ(), wrapper.getYaw() % 360.0F, wrapper.getPitch() % 360.0F); // We have to do % 360.0F because the client also does this
        this.teleports.add(pos);
    }
}
