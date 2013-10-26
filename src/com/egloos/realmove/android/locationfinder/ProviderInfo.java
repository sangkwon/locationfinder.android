
package com.egloos.realmove.android.locationfinder;

import android.location.Location;

public class ProviderInfo {

	private String name;
	private Location location;

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

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "ProviderInfo [name=" + name + "]";
	}

}
