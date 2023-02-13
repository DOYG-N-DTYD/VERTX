package io.vertx.codeone.conduit.models;

import io.vertx.core.json.JsonObject;

public class Item {
	String name;
	String owner;

	public JsonObject toConduitString() {
		JsonObject item = new JsonObject()
				.put("name",this.name)
				.put("owner",this.owner);
		JsonObject retVal = new JsonObject()
				.put("item",item);
		
		return retVal;
	}
	public Item() {}
	public Item(String name, String owner) {
		this.name = name;
		this.owner = owner;
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
}
