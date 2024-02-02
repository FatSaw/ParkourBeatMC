package me.bomb.parkourbeat.location;

public class LocationPoint {
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public LocationPoint(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    protected final boolean hasMove(double x, double y, double z) {
        return x - this.x >= 0.1 || this.x - x >= 0.1 || y - this.y >= 0.1 || this.y - y >= 0.1 || z - this.z >= 0.1 || this.z - z >= 0.1;
    }

    public final boolean hasMove(LocationPoint locationpoint) {
        return locationpoint.x - this.x >= 0.1 || this.x - locationpoint.x >= 0.1 || locationpoint.y - this.y >= 0.1 || this.y - locationpoint.y >= 0.1 || locationpoint.z - this.z >= 0.1 || this.z - locationpoint.z >= 0.1;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}