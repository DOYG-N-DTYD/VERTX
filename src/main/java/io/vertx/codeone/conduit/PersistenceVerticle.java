package io.vertx.codeone.conduit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.lang.model.element.Element;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.vertx.codeone.conduit.models.Item;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.auth.mongo.MongoAuthorization;
import io.vertx.ext.auth.mongo.MongoAuthorizationOptions;
import io.vertx.ext.auth.mongo.MongoUserUtil;
import io.vertx.ext.mongo.MongoClient;

public class PersistenceVerticle extends AbstractVerticle {

	private MongoClient mongoClient;
	MongoUserUtil inserterToDb;

	public void start(Promise<Void> startPromise) {
		mongoClient = MongoClient.createShared(vertx,
				new JsonObject().put("db_name", config().getString("db_name", "conduit_dev")).put("connection_string",
						config().getString("connection_string", "mongodb://localhost:27017")));
		mongoClient.createCollection("user");
		mongoClient.createCollection("items");
		inserterToDb = MongoUserUtil.create(mongoClient);
		MongoAuthenticationOptions userAuthenticationOptions = new MongoAuthenticationOptions();
		userAuthenticationOptions.setUsernameField("email");
		userAuthenticationOptions.setUsernameCredentialField("email");
		MongoAuthentication.create(mongoClient, userAuthenticationOptions);
		EventBus eventBus = vertx.eventBus();
//		eventBus.addInboundInterceptor(busMsg -> {
//			JsonObject messageJson = new JsonObject(busMsg.message().body().toString());
//			actionOnAdress = messageJson.getString("action");
//				busMsg.next();
//		});
// TODO KISS ?
		MessageConsumer<JsonObject> consumerPer = eventBus.consumer("persistence-address");// messageJson.getString("action"));
		consumerPer.handler(perMessage -> {
			String action = perMessage.body().getString("action");
			switch (action) {
			case "register-user":
				registerUser(perMessage);
				break;
			default:
				perMessage.fail(1, "Unkown action: " + perMessage.body());
			}
		});
		MessageConsumer<JsonObject> consumerLog = eventBus.consumer("login-address");
		consumerLog.handler(logMessage -> {
			String action = logMessage.body().getString("action");
			switch (action) {
			case "login-user":
				loginUser(logMessage);
				break;
			default:
				logMessage.fail(1, "Unkown action: " + logMessage.body());
			}
		});
		MessageConsumer<JsonObject> consumerItems = eventBus.consumer("item-address");
		consumerItems.handler(itemMessage -> {
			String action = itemMessage.body().getString("action");
			switch (action) {
			case "items-user":
				itemsUser(itemMessage);
				break;
			case "get-items-user":
				getItemsUser(itemMessage);
				break;
			default:
				itemMessage.fail(1, "Unkown action: " + itemMessage.body());
			}
		});

		startPromise.complete();
	}

	private void registerUser(Message<JsonObject> message) {
		JsonObject userToRegister = message.body().getJsonObject("user");
		System.out.println("8 " + userToRegister);
		inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {
			if (ar.succeeded()) {
				String id = ar.result();
				System.out.println("ID " + id);
				UUID testUUID = UUID.nameUUIDFromBytes(id.getBytes());
				System.out.println("UUID " + testUUID);
				JsonObject query = new JsonObject().put("_id", id);
				JsonObject userData = new JsonObject();
				userData.put("username", userToRegister.getString("username"));
				userData.put("email", userToRegister.getString("email"));
				userData.put("UUID", testUUID.toString());
				userData.put("token", null);
				userData.put("items", null);
				JsonObject update = new JsonObject().put("$set", userData);

				mongoClient.updateCollection("user", query, update, res -> {
					if (res.succeeded()) {
						System.out.println("SUCCES " + userToRegister);
						message.reply(Json.encode(userToRegister));
					} else {
						System.out.println("NOOOOO");
						message.fail(2, "insert failed: " + res.cause().getMessage());
					}
				});
				System.out.println("Inserter OK");
			} else {
				message.fail(2, "insert failed: " + ar.cause().getMessage());
				System.out.println("Inserter Not Success");
			}
		});
	}

	private void loginUser(Message<JsonObject> message) {
		// TODO after login -> return TOKEN
		// 1) Check if user in DB
		// 2) set token, msg (success login) <- null <- | token | -> not null -> msg
		// "already logged"
		JsonObject userToLogin = message.body().getJsonObject("user");
		MongoAuthorizationOptions userAuthorizationOptions = new MongoAuthorizationOptions();
		userAuthorizationOptions.setUsernameField("email");
		userAuthorizationOptions.setPermissionField("password");
		String providerIDtest = "loginProvider";
		MongoAuthorization.create(providerIDtest, mongoClient, userAuthorizationOptions);
		try {
			mongoClient.find("user", new JsonObject().put("email", userToLogin.getString("email")), res -> {
				if (res.result().toString().length() < 3) {
					message.fail(2, "User not found: " + null);
				} else {
					message.reply(userToLogin);
					if (res.result().get(0).getString("token") == null) { // TODO jwt.token.temp ->
						System.out.println("USER IN DB, logging, will set temp token ");
						mongoClient.updateCollection("user",
								new JsonObject().put("email", userToLogin.getString("email")),
								new JsonObject().put("$set", new JsonObject().put("token", "TEMPTOKEN")), resUpdate -> {
									if (resUpdate.succeeded()) {
										System.out.println("SUCCES UPDATE" + userToLogin);
										message.reply(resUpdate);
									} else {
										message.fail(2, "insert failed: " + resUpdate.cause().getMessage());
									}
								});
					}
				}
			});
		} catch (Exception e) {
			System.out.println("User username NOT FOUND " + e);
		}
	}

	private void itemsUser(Message<JsonObject> message) {
		JsonObject userToItems = message.body().getJsonObject("user");
		try {
			mongoClient.find("user", new JsonObject().put("email", userToItems.getString("email")), res -> {
				if (res.result().toString().length() < 3) {
					message.fail(2, "User not found: " + null);
				} else {
					message.reply(userToItems);
					if (res.result().get(0).getString("token").equals("TEMPTOKEN")) { // TODO jwt.token.temp ->
						if (userToItems.getString("items") == null) {
							System.out.println("REQUEST NULL ITEMS");
							message.reply("Empty items in request");
						} else {
							JsonArray array = new JsonArray();
							if (res.result().get(0).getString("items") != null) { // itmes DB + items request
								System.out.println("DB NULL ITEMS");
								itemsToJson(parseItemsString(res.result().get(0).getString("items").toString()), array);
							}
							createItems(res.result().get(0).getString("UUID"),
									parseItemsString(userToItems.getString("items").toString())); // Only items from
																									// request
							itemsToJson(parseItemsString(userToItems.getString("items").toString()), array);

							mongoClient.updateCollection("user",
									new JsonObject().put("email", userToItems.getString("email")),
									new JsonObject().put("$set", new JsonObject().put("items", array)), resUpdate -> {
										if (resUpdate.succeeded()) {
											System.out.println("SUCCES UPDATE" + userToItems);
											message.reply("SUCCES UPDATE" + userToItems);
										} else {
											message.fail(2, "insert failed: " + resUpdate.cause().getMessage());
										}
									});
						}
					}
				}
			});
		} catch (Exception e) {
			System.out.println("User username NOT FOUND " + e);
		}
	}

	private List<String> parseItemsString(String items) {
		String buffString = items.replaceAll("[^a-zA-Z0-9,_-]", "");
		List<String> list = new ArrayList<String>();
		for (String item : buffString.split(",")) {
			list.add(item);
		}
		return list;

	}

	private JsonArray itemsToJson(List<String> list, JsonArray array) {
		for (int i = 0; i < list.size(); i++) {
			array.add(list.get(i));
		}
		return array;
	}

	private void createItems(String userUUID, List<String> items) {
		for (String itemName : items) {
			JsonObject item = new JsonObject().put("owner", userUUID).put("name", itemName).put("UUID", null);
			mongoClient.insert("items", item, res -> {
				// TODO: ISSUE WITH UUID
				String id = res.result();
				UUID testUUID = UUID.nameUUIDFromBytes(id.getBytes());
				JsonObject query = new JsonObject().put("_id", id);
				JsonObject itemData = new JsonObject();
				itemData.put("UUID", testUUID.toString());
				JsonObject update = new JsonObject().put("$set", itemData);
				mongoClient.updateCollection("items", query, update, resUpdate -> {
					if (resUpdate.succeeded()) {
						System.out.println("TempUUID added success");
					} else {
						System.out.println("TempUUID NOT added, fail" + resUpdate);
					}
					System.out.println("INSERT ITEMS next OK ");
				});
			});
		}
	}

	private void getItemsUser(Message<JsonObject> message) {
		JsonObject userToItems = message.body().getJsonObject("user");
		System.out.println("DEBUG ### USER userToItems " + message.body().getJsonObject("user"));
		try {
			mongoClient.find("user", new JsonObject().put("email", userToItems.getString("email")), res -> {
				if (res.result().toString().length() < 3) {
					message.fail(2, "User not found: " + null);
				} else {
					message.reply(userToItems);
					if (res.result().get(0).getString("token").equals("TEMPTOKEN")) { // TODO jwt.token.temp ->
						System.out.println("DEBUG ### USER UUID " + res.result().get(0).getString("UUID"));
						mongoClient.find("items", new JsonObject().put("owner", res.result().get(0).getString("UUID")),
								resFoundItems -> {
									if (res.result().toString().length() < 3) {
										message.reply("User have no items");
									} else {
										Gson gson = new Gson();
										Item[] userArray = gson.fromJson(resFoundItems.result().toString(),
												Item[].class);
										for (Item item : userArray) {
											System.out.println("ITEM ID:  " + item._id() + " NAME: "
													+ item.getUsername() + " OWNER: " + item.getEmail());
										}
										// message.reply(userArray);
									}
								});
					} else {
						System.out.println("USER NOT AUTHORIZED");
					}
				}
			});
		} catch (Exception e) {
			System.out.println("User username NOT FOUND " + e);
		}
	}
}
