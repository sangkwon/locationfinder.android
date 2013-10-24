
package com.egloos.realmove.android.locationfinder;

import android.location.Location;

public class ProviderInfo {

	private String name;
	private Location lastKnownLocation;

	public ProviderInfo() {
		this(null);
	}

	public ProviderInfo(String name) {
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	@Override
	public String toString() {
		return "ProviderInfo [name=" + name + "]";
	}

}
