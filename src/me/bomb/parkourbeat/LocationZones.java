package me.bomb.parkourbeat;

final class LocationZones {
	protected final LocationZone register, respawn;
	protected LocationZones(LocationZone register, LocationZone respawn) {
		this.register = register;
		this.respawn = respawn;
	}
}
