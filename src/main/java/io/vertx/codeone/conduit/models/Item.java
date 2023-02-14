package io.vertx.codeone.conduit.models;

import io.vertx.core.json.JsonObject;

public class Item {
	String name;
	String owner;
	String UUID;

	public JsonObject toConduitString() {
		JsonObject item = new JsonObject().put("name", this.name).put("owner", this.owner);//.put("UUID", this.UUID)
		JsonObject retVal = new JsonObject().put("item", item);

		return retVal;
	}

	public Item() {
	}

	public Item(String name, String owner, String UUID) {
		this.name = name;
		this.owner = owner;
		this.UUID = UUID;
	}

	public void setUsername(String name) {
		this.name = name;
	}

	public String getUsername() {
		return name;
	}

	public void setEmail(String owner) {
		this.owner = owner;
	}

	public String getEmail() {
		return owner;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

	public String getUUID() {
		return UUID;
	}
}
