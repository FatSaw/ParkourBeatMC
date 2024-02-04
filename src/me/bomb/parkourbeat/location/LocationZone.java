package me.bomb.parkourbeat.location;

import java.util.concurrent.ThreadLocalRandom;

public class LocationZone {
    private final int minX, minY, minZ, maxX, maxY, maxZ;
    private final int sizeX, sizeY, sizeZ;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public LocationZone(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX + 1;
        this.maxY = maxY + 1;
        this.maxZ = maxZ + 1;
        this.sizeX = maxX - minX;
        this.sizeY = maxY - minY;
        this.sizeZ = maxZ - minZ;
    }

    public boolean isOutside(double x, double y, double z) {
        return x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ;
    }

    public boolean isInside(double x, double y, double z) {
        return !isOutside(x, y, z);
    }

    protected boolean isOutside(int x, int y, int z) {
        return x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ;
    }

    protected boolean isInside(int x, int y, int z) {
        return !isOutside(x, y, z);
    }

    protected boolean isNotOverlap(LocationZone zone) {
        return zone.maxX < minX || zone.minX > maxX || zone.maxY < minY || zone.minY > maxY || zone.maxZ < minZ || zone.minZ > maxZ;
    }

    protected boolean isOverlap(LocationZone zone) {
        return !isNotOverlap(zone);
    }

    protected LocationInside randomInside() {
        double x = generateRandomCoordinate(minX, sizeX);
        double y = generateRandomCoordinate(minY, sizeY);
        double z = generateRandomCoordinate(minZ, sizeZ);
        return new LocationInside(x, y, z);
    }

    public LocationInside randomInside(double borderOffset) {
        double x = generateRandomCoordinate(minX + borderOffset, sizeX - 2 * borderOffset);
        double y = generateRandomCoordinate(minY + borderOffset, sizeY - 2 * borderOffset);
        double z = generateRandomCoordinate(minZ + borderOffset, sizeZ - 2 * borderOffset);
        return new LocationInside(x, y, z);
    }

    private double generateRandomCoordinate(double min, double range) {
        return min + range * random.nextDouble();
    }
}
