package me.beanes.acid.player.tracker.impl;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.tracker.Tracker;

@Getter
public class RotationTracker extends Tracker {
    private float yaw, pitch, lastYaw, lastPitch;

    public RotationTracker(PlayerData data) {
        super(data);
    }

    public void handleClientTick(WrapperPlayClientPlayerFlying wrapper) {
        this.lastYaw = yaw;
        this.lastPitch = pitch;

        if (wrapper.hasRotationChanged()) {
            this.yaw = wrapper.getLocation().getYaw();
            this.pitch = wrapper.getLocation().getPitch();
        }
    }
}
