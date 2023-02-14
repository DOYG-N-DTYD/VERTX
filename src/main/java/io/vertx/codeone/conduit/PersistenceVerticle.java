package io.vertx.codeone.conduit;

import java.util.UUID;

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

		startPromise.complete();
	}

	private void registerUser(Message<JsonObject> message) {
//message.fail(1, "Unimplemented");

//	  JsonObject retVal = new JsonObject()
//			  	.put("email", "mirek@mirek")
//			  	.put("username", "mirek");
//	  message.reply(retVal);
// TODO create account -> insert to DB
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
				userData.put("UUID", testUUID.toString());
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
		   	// 2) set token, msg (success login) <- null <- | token | -> not null -> msg "already logged"
		
		System.out.println("loginUser METHOD !!!!!!!!!!!!!!!!! START");
		JsonObject userToLogin = message.body().getJsonObject("user");
		MongoAuthorizationOptions userAuthorizationOptions = new MongoAuthorizationOptions();
		userAuthorizationOptions.setUsernameField("email");
	 	userAuthorizationOptions.setPermissionField("password");
	 	String providerIDtest = "loginProvider";
	 	MongoAuthorization.create(providerIDtest, mongoClient, userAuthorizationOptions);

	 	loginerToDb.createUser(userToLogin.getString("email"), userToLogin.getString("password"), ar -> {
	 		if (ar.succeeded()) {
	 			System.out.println(ar.result());
	 			//JsonObject query = new JsonObject().put("_id", id);
				//JsonObject userData = new JsonObject();
				//userData.put("username", userToLogin.getString("username"));
				//userData.put("token", "tempTokenForUser");
				//JsonObject update = new JsonObject().put("$set", userData);
				
				mongoClient.getCollections(result -> {
					if(result.failed()) {
						System.out.println("loginUser METHOD 1");
						//message.fail(3, "Failed to get collections: " + result.cause().getMessage());
					}else {
						System.out.println("loginUser METHOD 2 :"+result.result().size());
						message.reply(userToLogin);               // TODO AFTER success returned user
						//message.reply(result.result().size());
					}
				});
				
//	 			mongoClient.updateCollection("user", query, update, res -> {
//					if (res.succeeded()) {
//						System.out.println("SUCCES " + userToRegister);
//						message.reply(Json.encode(userToRegister));
//					} else {
//						System.out.println("NOOOOO");
//						message.fail(2, "insert failed: " + res.cause().getMessage());
//					}
//				});
	 		}else {
	 			System.out.println("loginUser METHOD 3");
	 			//message.fail(4, "login failed: " + ar.cause().getMessage());
	 		}
	 			});
		System.out.println("loginUser METHOD !!!!!!!!!!!!!!!!! END END");
		//loginerToDb.;
//		inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {
//			if (ar.succeeded()) {
//				message.reply(Json.encode(userToRegister));
//			} else {
//				message.fail(2, "insert failed: ");
//			}
//		});
	}

	private void postItems() {
		// TODO create item for authorized user
	}

	private void getItems() {
		// TODO show list of user item's

	}
}
