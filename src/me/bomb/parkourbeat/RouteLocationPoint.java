package me.bomb.parkourbeat;

final class RouteLocationPoint extends LocationPoint {
	private final float oyaw;
	private final float opitch;
	
	protected RouteLocationPoint(double x,double y,double z,float yaw,float pitch,float oyaw,float opitch) {
		super(x, y, z, yaw, pitch);
		this.oyaw = oyaw;
		this.opitch = opitch;
	}
	
	@Override
	public float getYaw() {
		return oyaw;
	}

	@Override
	public float getPitch() {
		return opitch;
	}

}
