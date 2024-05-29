package me.beanes.acid.util;

import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;

// Copied from MCP 1.8.9
public class BoundingBoxUtils {
    public static Vector3d calculateIntercept(BoundingBox box, Vector3d vecA, Vector3d vecB)
    {
        Vector3d vec3 = getIntermediateWithXValue(vecA, vecB, box.getMinX());
        Vector3d vec31 = getIntermediateWithXValue(vecA, vecB, box.getMaxX());
        Vector3d vec32 = getIntermediateWithYValue(vecA, vecB, box.getMinY());
        Vector3d vec33 = getIntermediateWithYValue(vecA, vecB, box.getMaxY());
        Vector3d vec34 = getIntermediateWithZValue(vecA, vecB, box.getMinZ());
        Vector3d vec35 = getIntermediateWithZValue(vecA, vecB, box.getMaxZ());

        if (!isVecInYZ(box, vec3))
        {
            vec3 = null;
        }

        if (!isVecInYZ(box, vec31))
        {
            vec31 = null;
        }

        if (!isVecInXZ(box, vec32))
        {
            vec32 = null;
        }

        if (!isVecInXZ(box, vec33))
        {
            vec33 = null;
        }

        if (!isVecInXY(box, vec34))
        {
            vec34 = null;
        }

        if (!isVecInXY(box, vec35))
        {
            vec35 = null;
        }

        Vector3d vec36 = null;

        if (vec3 != null)
        {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || vecA.distanceSquared(vec31) < vecA.distanceSquared(vec36)))
        {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || vecA.distanceSquared(vec32) < vecA.distanceSquared(vec36)))
        {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || vecA.distanceSquared(vec33) < vecA.distanceSquared(vec36)))
        {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || vecA.distanceSquared(vec34) < vecA.distanceSquared(vec36)))
        {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || vecA.distanceSquared(vec35) < vecA.distanceSquared(vec36)))
        {
            vec36 = vec35;
        }

        if (vec36 == null)
        {
            return null;
        }
        else
        {
            return vec36;
        }
    }

    public static boolean isVecInside(BoundingBox box, Vector3d vec)
    {
        return vec.getX() > box.getMinX() && vec.getX() < box.getMaxX() ? (vec.getY() > box.getMinY() && vec.getY() < box.getMaxY() ? vec.getZ() > box.getMinZ() && vec.getZ() < box.getMaxZ() : false) : false;
    }

    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    private static Vector3d getIntermediateWithXValue(Vector3d self, Vector3d vec, double x)
    {
        double d0 = vec.getX() - self.getX();
        double d1 = vec.getY() - self.getY();
        double d2 = vec.getZ() - self.getZ();

        if (d0 * d0 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (x - self.getX()) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(self.getX() + d0 * d3, self.getY() + d1 * d3, self.getZ() + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    private static Vector3d getIntermediateWithYValue(Vector3d self, Vector3d vec, double y)
    {
        double d0 = vec.getX() - self.getX();
        double d1 = vec.getY() - self.getY();
        double d2 = vec.getZ() - self.getZ();

        if (d1 * d1 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (y - self.getY()) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(self.getX() + d0 * d3, self.getY() + d1 * d3, self.getZ() + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    private static Vector3d getIntermediateWithZValue(Vector3d self, Vector3d vec, double z)
    {
        double d0 = vec.getX() - self.getX();
        double d1 = vec.getY() - self.getY();
        double d2 = vec.getZ() - self.getZ();

        if (d2 * d2 < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double d3 = (z - self.getZ()) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(self.getX() + d0 * d3, self.getY() + d1 * d3, self.getZ() + d2 * d3) : null;
        }
    }

    /**
     * Checks if the specified vector is within the YZ dimensions of the bounding box. Args: Vec3D
     */
    private static boolean isVecInYZ(BoundingBox self, Vector3d vec)
    {
        return vec == null ? false : vec.getY() >= self.getMinY() && vec.getY() <= self.getMaxY() && vec.getZ() >= self.getMinZ() && vec.getZ() <= self.getMaxZ();
    }

    /**
     * Checks if the specified vector is within the XZ dimensions of the bounding box. Args: Vec3D
     */
    private static boolean isVecInXZ(BoundingBox self, Vector3d vec)
    {
        return vec == null ? false : vec.getX() >= self.getMinX() && vec.getX() <= self.getMaxX() && vec.getZ() >= self.getMinZ() && vec.getZ() <= self.getMaxZ();
    }

    /**
     * Checks if the specified vector is within the XY dimensions of the bounding box. Args: Vec3D
     */
    private static boolean isVecInXY(BoundingBox self, Vector3d vec)
    {
        return vec == null ? false : vec.getX() >= self.getMinX() && vec.getX() <= self.getMaxX() && vec.getY() >= self.getMinY() && vec.getY() <= self.getMaxY();
    }
}
