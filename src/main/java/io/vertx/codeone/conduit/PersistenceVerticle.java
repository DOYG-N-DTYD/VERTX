package io.vertx.codeone.conduit;

import java.util.UUID;

import javax.lang.model.element.Element;
import javax.swing.text.Document;

import org.bson.conversions.Bson;
import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.MongoAuthentication;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.auth.mongo.MongoAuthorization;
import io.vertx.ext.auth.mongo.MongoAuthorizationOptions;
import io.vertx.ext.auth.mongo.MongoUserUtil;
import io.vertx.ext.mongo.MongoClient;

public class PersistenceVerticle extends AbstractVerticle {

	// for DB access
	private MongoClient mongoClient;
	// Authentication provider for logging in
	@SuppressWarnings("deprecation")
	private MongoAuth loginAuthProvider;
	private MongoAuthentication userAuthentication;
	private MongoAuthorization userAuthorization;
//private MongoAuthenticationOptions userAuthenticationOptions;  
	//
	MongoUserUtil inserterToDb;
	MongoUserUtil loginerToDb;

	// @SuppressWarnings("deprecation")
	// @Override
	public void start(Promise<Void> startPromise) {
//		ChangebleAdr changer = new ChangebleAdr() {
//			public String setAdress(String adr) {
//				return adr;
//			}
//		};
		// Configure the MongoClient inline. This should be externalized into a config
		// file
		mongoClient = MongoClient.createShared(vertx,
				new JsonObject().put("db_name", config().getString("db_name", "conduit_dev")).put("connection_string",
						config().getString("connection_string", "mongodb://localhost:27017")));
		inserterToDb = MongoUserUtil.create(mongoClient);
		loginerToDb = MongoUserUtil.create(mongoClient);
		// Configure authentication with MongoDB
		// loginAuthProvider = MongoAuth.create(mongoClient, new JsonObject());
		MongoAuthenticationOptions userAuthenticationOptions = new MongoAuthenticationOptions();
		userAuthenticationOptions.setUsernameField("email");
		userAuthenticationOptions.setUsernameCredentialField("email"); // PROBLEM !!!!!
		MongoAuthentication.create(mongoClient, userAuthenticationOptions);

		// userAuthorization.create( mongoClient, userAuthenticationOptions);
		// loginAuthProvider.setUsernameField("email");
		// loginAuthProvider.setUsernameCredentialField("email");

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
			default:
				itemMessage.fail(1, "Unkown action: " + itemMessage.body());
			}
		});

		startPromise.complete();
	}

	private void registerUser(Message<JsonObject> message) {
//message.fail(1, "Unimplemented");

//	  JsonObject retVal = new JsonObject()
//			  	.put("email", "mirek@mirek")
//			  	.put("username", "mirek");
//	  message.reply(retVal);
// TODO create account -> insert to DB
		// TODO if exists -> msg exists
		JsonObject userToRegister = message.body().getJsonObject("user");
		System.out.println("8 " + userToRegister);

		inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {
			if (ar.succeeded()) {
				String id = ar.result();

				System.out.println("ID " + id);
				UUID testUUID = UUID.nameUUIDFromBytes(id.getBytes());
				System.out.println("UUID " + testUUID);

				// JsonObject query = new JsonObject().put("_id",
				// id);//.put("_UUID","generatedUUID");
				JsonObject query = new JsonObject().put("_id", id);
				JsonObject userData = new JsonObject();
				userData.put("username", userToRegister.getString("username"));
				userData.put("email", userToRegister.getString("email"));
				userData.put("UUID", testUUID.toString());
				// userData.put("token", userToRegister.getString("token")); //TODO issue with
				// unic crypted token -> lets make temp :F
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
		System.out.println("FFFFFFFFFFFFFFFFFFFf  "+message.body());
		System.out.println("loginUser METHOD !!!!!!!!!!!!!!!!! START");
		JsonObject userToLogin = message.body().getJsonObject("user");
		MongoAuthorizationOptions userAuthorizationOptions = new MongoAuthorizationOptions();
		userAuthorizationOptions.setUsernameField("email");
		userAuthorizationOptions.setPermissionField("password");
		String providerIDtest = "loginProvider";
		MongoAuthorization.create(providerIDtest, mongoClient, userAuthorizationOptions);

//	 	loginerToDb.createUser(userToLogin.getString("email"), userToLogin.getString("password"), ar -> {
//	 		if (ar.succeeded()) {
//	 			System.out.println(ar.result());
		// JsonObject query = new JsonObject().put("_id", id);
		// JsonObject userData = new JsonObject();
		// userData.put("username", userToLogin.getString("username"));
		// userData.put("token", "tempTokenForUser");
		// JsonObject update = new JsonObject().put("$set", userData);

		// TODO exception IndexOutOfBoundsException
		try {
			mongoClient.find("user", new JsonObject().put("email", userToLogin.getString("email")), res -> {

				if (res.result().toString().length() < 3) {
					System.out.println("DEBUG 22" + res.result().toString().equals("[]"));
					message.fail(2, "User not found: " + null);
				} else {
					System.out.println("DEBUG 33" + res);
					System.out.println("DEBUG 33" + res.result());
					message.reply(userToLogin);
					System.out.println("DEBUG 4 " + res.result().get(0).getString("username"));

					System.out.println("DEBUG 5 " + res.result().get(0).getString("token"));
					if (res.result().get(0).getString("token") == null) { // TODO jwt.token.temp ->
																			// null
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
		System.out.println("PERSISTENCE itemsUser method !!!!!!!!!!!!!!!!!!" + message.body());
		JsonObject userToItems = message.body().getJsonObject("user");
		System.out.println("PERSISTENCE itemsUser method !!!!!!!!!!!!!!!!!! "+userToItems.getString("email"));
//		try {
			mongoClient.find("user", new JsonObject().put("email", userToItems.getString("email")), res -> {
				if (res.result().toString().length() < 3) {
					message.fail(2, "User not found: " + null);
				} else {
					System.out.println("DEBUG 33" + res);
					System.out.println("DEBUG 33" + res.result());
					message.reply(userToItems);
					System.out.println("DEBUG 4 " + res.result().get(0).getString("username"));

					System.out.println("DEBUG 5 " + res.result().get(0).getString("token"));
					if (res.result().get(0).getString("token").equals("TEMPTOKEN")) { // TODO jwt.token.temp ->
																			// null
						System.out.println("USER IN DB, AUTHORIZED SO OK");
						System.out.println("USER ITEMS FROM DATABASE "+res.result().get(0).getString("items"));
						System.out.println("USER ITEMS FROM REQUEST "+userToItems.getString("items"));
//						mongoClient.updateCollection("user",
//								new JsonObject().put("email", userToItems.getString("email")),
//								new JsonObject().put("$set", new JsonObject().put("token", "TEMPTOKEN")), resUpdate -> {
//									if (resUpdate.succeeded()) {
//										System.out.println("SUCCES UPDATE" + userToItems);
//										message.reply(resUpdate);
//									} else {
//										message.fail(2, "insert failed: " + resUpdate.cause().getMessage());
//									}
//								});
					}
				}
			});
//		} catch (Exception e) {
//			System.out.println("User username NOT FOUND " + e);
//		}
	}

//	private void postItemsCreate(Message<JsonObject> message) {
//		// TODO check if token equals TEMPTOKEN
//		// TODO Items from db -> add new -> Write in DB
//		JsonObject userToItemsCreate = message.body().getJsonObject("user");
//		JsonObject ItemsNames = message.body().getJsonObject("items"); // null -> leave or empty list
//		try {
//			mongoClient.find("user", new JsonObject().put("email", userToItemsCreate.getString("email")), res -> {
//				if (res.result().toString().length() < 3) {
//					message.fail(2, "User not found: " + null);
//				} else {
//					message.reply("User found: " + userToItemsCreate);
//					if (res.result().get(0).getString("token").equals("TEMPTOKEN")) { // TODO TEMPTOKEK -> OK
//						message.reply("User Authorized: " + userToItemsCreate);
//						System.out.println(res.result().get(0).getString("items"));
////						mongoClient.updateCollection("user",
////								new JsonObject().put("email", userToItemsCreate.getString("email")),
////								new JsonObject().put("$set", new JsonObject().put("token", "TEMPTOKEN")), resUpdate -> {
////									if (resUpdate.succeeded()) {
////										System.out.println("SUCCES UPDATE" + userToLogin);
////										message.reply(resUpdate);
////									} else {
////										message.fail(2, "insert failed: " + resUpdate.cause().getMessage());
////									}
////								});
//					}
//				}
//			});
//		} catch (Exception e) {
//			System.out.println("User username NOT FOUND " + e);
//		}
//		// TODO create item for authorized user (token equals TEMPTOKEN)
//		// TODO Item_name -> create item -> write in DB
//	}
//
//	private void getItemsGet(Message<JsonObject> message) {
//		// TODO check if token equals TEMPTOKEN
//		// TODO show list of user item's
//
//	}
}
