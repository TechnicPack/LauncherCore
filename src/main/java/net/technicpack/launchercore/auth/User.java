package net.technicpack.launchercore.auth;

import com.google.gson.JsonObject;

public class User {
	private String id;
	private JsonObject userProperties;

	public String getId() {
		return this.id;
	}

	public JsonObject getUserProperties() {
		return this.userProperties;
	}
}
