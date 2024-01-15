package me.bomb.parkourbeat;

import java.util.HashSet;
import java.util.Random;

final class LocationZone {
	private final static boolean debug = GameOptions.debug;
	private final int minx, miny, minz, maxx, maxy, maxz, sizex, sizey, sizez;
	protected final HashSet<LocationInside> border;
	protected LocationZone(int minx, int miny, int minz, int maxx, int maxy, int maxz) {
		++maxx;
		++maxy;
		++maxz;
		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = maxx;
		this.maxy = maxy;
		this.maxz = maxz;
		this.sizex = maxx - minx;
		this.sizey = maxy - miny;
		this.sizez = maxz - minz;
		if(debug) {
			border = new HashSet<LocationInside>();
			border.add(new LocationInside(minx, miny, minz));
			border.add(new LocationInside(minx, miny, maxz));
			border.add(new LocationInside(minx, maxy, minz));
			border.add(new LocationInside(minx, maxy, maxz));
			border.add(new LocationInside(maxx, miny, minz));
			border.add(new LocationInside(maxx, miny, maxz));
			border.add(new LocationInside(maxx, maxy, minz));
			border.add(new LocationInside(maxx, maxy, maxz));
			for (int x = maxx; --x > minx;) {
				border.add(new LocationInside(x, miny, minz));
				border.add(new LocationInside(x, miny, maxz));
				border.add(new LocationInside(x, maxy, minz));
				border.add(new LocationInside(x, maxy, maxz));
			}
			
			for (int y = maxy; --y > miny;) {
				border.add(new LocationInside(minx, y, minz));
				border.add(new LocationInside(minx, y, maxz));
				border.add(new LocationInside(maxx, y, minz));
				border.add(new LocationInside(maxx, y, maxz));
			}
			
			for (int z = maxz; --z > minz;) {
				border.add(new LocationInside(minx, miny, z));
				border.add(new LocationInside(minx, maxy, z));
				border.add(new LocationInside(maxx, maxy, z));
				border.add(new LocationInside(maxx, miny, z));
			}
		} else {
			border = null;
		}
	}
	protected boolean isOutside(double x, double y, double z) {
		return x<minx||x>maxx||y<miny||y>maxy||z<minz||z>maxz;
	}
	protected boolean isInside(double x, double y, double z) {
		return !isOutside(x, y, z);
	}
	protected boolean isOutside(int x, int y, int z) {
		return x<minx||x>maxx||y<miny||y>maxy||z<minz||z>maxz;
	}
	protected boolean isInside(int x, int y, int z) {
		return !isOutside(x, y, z);
	}
	protected boolean isNotOverlap(LocationZone zone) {
		return zone.minx<minx||zone.minx>maxx||zone.miny<miny||zone.miny>maxy||zone.minz<minz||zone.minz>maxz||zone.maxx<minx||zone.maxx>maxx||zone.maxy<miny||zone.maxy>maxy||zone.maxz<minz||zone.maxz>maxz||minx<zone.minx||minx>zone.maxx||miny<zone.miny||miny>zone.maxy||minz<zone.minz||minz>zone.maxz||maxx<zone.minx||maxx>zone.maxx||maxy<zone.miny||maxy>zone.maxy||maxz<zone.minz||maxz>zone.maxz;
	}
	protected boolean isOverlap(LocationZone zone) {
		return !isNotOverlap(zone);
	}
	protected LocationInside randomInside() {
		Random random = new Random();
		double x = this.minx + this.sizex * random.nextDouble(), y = this.miny + this.sizey * random.nextDouble(), z = this.minz + this.sizez * random.nextDouble();
		return new LocationInside(x, y, z);
	}
	protected LocationInside randomInside(double borderoffset) {
		Random random = new Random();
		double x = this.minx + borderoffset + (this.sizex - borderoffset - borderoffset) * random.nextDouble(), y = this.miny + borderoffset + (this.sizey - borderoffset - borderoffset) * random.nextDouble(), z = this.minz + borderoffset + (this.sizez - borderoffset - borderoffset) * random.nextDouble();
		return new LocationInside(x, y, z);
	}
}
