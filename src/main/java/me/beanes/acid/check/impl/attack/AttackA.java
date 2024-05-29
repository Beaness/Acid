package me.beanes.acid.check.impl.attack;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.beanes.acid.Acid;
import me.beanes.acid.check.Check;
import me.beanes.acid.player.PlayerData;
import me.beanes.acid.player.tracker.impl.entity.TrackedEntity;
import me.beanes.acid.player.tracker.impl.entity.TrackedPosition;
import me.beanes.acid.util.BoundingBoxUtils;
import me.beanes.acid.util.MCMath;
import me.beanes.acid.util.trig.TrigHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;

public class AttackA extends Check {

    // 1.62 is normal eight height, 1.62 - 0.08 for sneaking
    private static final double[] eyeHeights = new double[] {(double) (1.62f), (double) (1.62f - 0.08f)};
    public AttackA(PlayerData data) {
        super(data, "Attack", "A", "Checks for illegal attack packets");
    }
    private final Int2ObjectMap<Vector3d> attacks = new Int2ObjectLinkedOpenHashMap<>();
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!data.getPositionTracker().isTeleport() && !attacks.isEmpty()) {
                this.processAttacks();
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                attacks.put(wrapper.getEntityId(), new Vector3d(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ()));
            }
        }
    }

    private void processAttacks() {
        Vector3d[] lookVectors;
        if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
            // Older optifine versions of 1.8 have the legacy fast math too, super fun!
            lookVectors = new Vector3d[]{
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    // Vanilla uses the last yaw -> a lot of clients & mods have this fixed though
                    // Read more: https://github.com/prplz/MouseDelayFix
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw()),
                    MCMath.getVectorForRotation(TrigHandler.FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getLastYaw())
            };
        } else {
            // 1.7 all fine
            lookVectors = new Vector3d[]{
                    MCMath.getVectorForRotation(TrigHandler.VANILLA_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw()),
                    MCMath.getVectorForRotation(TrigHandler.LEGACY_FAST_MATH, data.getRotationTracker().getPitch(), data.getRotationTracker().getYaw())
            };
        }

        try {
            for (Map.Entry<Integer, Vector3d> attack : attacks.entrySet()) {
                final Vector3d from = attack.getValue();
                final TrackedEntity trackedEntity = data.getEntityTracker().getTrackedEntity(attack.getKey());

                processAttack(from, data.getPositionTracker().isLastTickHasPosition(), trackedEntity.getTrackedPositions(), lookVectors);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        attacks.clear();
    }

    public void processAttack(Vector3d from, boolean isLastMoveIncludedPosition, List<TrackedPosition> trackedPositions, Vector3d[] lookVectors) {
        double reach = Double.MAX_VALUE;

        for (TrackedPosition trackedPosition : trackedPositions) {
            BoundingBox boundingBox = trackedPosition.getCachedBoundingBox();

            // 0.03 uncertainty
            float expand = isLastMoveIncludedPosition ? 0 : 0.03F;

            if (boundingBox == null) {
                // Player's bounding box are 0.6 width & 1.8 height
                // We add 0.1 to width & height though because 1.7 & 1.8 hitbox is 0.1 bigger
                boundingBox = new BoundingBox(
                        trackedPosition.getCurrentX() - 0.4F - expand, trackedPosition.getCurrentY() - 0.1F - expand, trackedPosition.getCurrentZ() - 0.4F - expand,
                        trackedPosition.getCurrentX() + 0.4F + expand, trackedPosition.getCurrentY() + 1.9F + expand, trackedPosition.getCurrentZ() + 0.4F + expand
                );
            }

            outer: for (final Vector3d lookVec : lookVectors) {
                for (final double eye : eyeHeights) {
                    Vector3d startReach = new Vector3d(from.getX(), from.getY() + eye, from.getZ());
                    Vector3d endReach = startReach.add(lookVec.getX() * 6, lookVec.getY() * 6, lookVec.getZ() * 6);

                    if (BoundingBoxUtils.isVecInside(boundingBox, startReach)) {
                        reach = 0;
                        break outer;
                    }

                    Vector3d intercept = BoundingBoxUtils.calculateIntercept(boundingBox, startReach, endReach);

                    if (intercept != null) {
                        reach = Math.min(startReach.distance(intercept), reach);
                    }
                }
            }
        }

        if (reach == Double.MAX_VALUE) {
            alert(ChatColor.RED + data.getUser().getName() + " Missed hitbox [branches=" + trackedPositions.size() + "]");
        } else if (reach > 3) {
            alert(ChatColor.RED + data.getUser().getName() + " dist=" + reach + " branches=" + trackedPositions.size());
        } else if (Acid.DEBUG) {
            alert(ChatColor.GREEN + data.getUser().getName() + " dist=" + reach + " branches=" + trackedPositions.size());
        }
    }
}
