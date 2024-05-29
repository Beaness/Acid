package me.beanes.acid.util;

import com.github.retrooper.packetevents.util.Vector3d;
import me.beanes.acid.util.trig.TrigHandler;

// Copied from MCP 1.8.9
public class MCMath {
    public static Vector3d getVectorForRotation(TrigHandler trig, float pitch, float yaw)
    {
        float f = trig.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = trig.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -trig.cos(-pitch * 0.017453292F);
        float y = trig.sin(-pitch * 0.017453292F);
        return new Vector3d(f1 * f2, y, f * f2);
    }
}
