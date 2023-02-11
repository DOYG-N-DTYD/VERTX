package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.MongoUserUtil;
import io.vertx.ext.mongo.MongoClient;

public class PersistenceVerticle extends AbstractVerticle{

  // for DB access
  private MongoClient mongoClient;
  // Authentication provider for logging in
  private MongoAuth loginAuthProvider;
  
  //
  MongoUserUtil inserterToDb;

  @Override
  public void start(Promise<Void> startPromise) {
    // Configure the MongoClient inline.  This should be externalized into a config file
    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("db_name", config().getString("db_name", "conduit_dev")).put("connection_string", config().getString("connection_string", "mongodb://localhost:27017")));
    System.out.println("1111111111111111111111111");
    // Configure authentication with MongoDB
    loginAuthProvider = MongoAuth.create(mongoClient, new JsonObject());
    loginAuthProvider.setUsernameField("email");
    loginAuthProvider.setUsernameCredentialField("email");
    System.out.println("22222222222222222222222222");

    EventBus eventBus = vertx.eventBus();
    System.out.println("22222222  22222222 ");
    MessageConsumer<JsonObject> consumer = eventBus.consumer("persistence-address");
    System.out.println("22222222  222 222 ");
    consumer.handler(message -> {
    	System.out.println("333333333333333333333333");
      String action = message.body().getString("action");
      System.out.println("444444444444444444444444");
      switch (action) {
        case "register-user":
        	System.out.println("555555555555555555555");
          registerUser(message);
          break;
        default:
        	System.out.println("6666666666666666");
          message.fail(1, "Unkown action: " + message.body());
      }
    });
    System.out.println("777777777777777777777777777");
    startPromise.complete();

  }

  /**
   * Receive Json in the following format:
   *
    {
     "username": "Jacob",
     "email": "jake@jake.jake",
     "password": "jakejake"
     }
   *
   * and return Json in the following format:
   *
   {
       "email": "jake@jake.jake",
       "token": "jwt.token.here",
       "username": "Jacob",
       "bio": "I work at statefarm",
       "image": null
   }
   *
   * @param message
   */

@SuppressWarnings("deprecation")
private void registerUser(Message<JsonObject> message) {
//message.fail(1, "Unimplemented");
	
//	  JsonObject retVal = new JsonObject()
//			  	.put("email", "mirek@mirek")
//			  	.put("username", "mirek");
//	  message.reply(retVal);
    JsonObject userToRegister = message.body().getJsonObject("user");
    System.out.println("8");
loginAuthProvider.insertUser(userToRegister.getString("email"), userToRegister.getString("password"), null, null, ar -> {   // OK ADDED TO DB !!
//    inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {  			// NOT WORKING 
    if (ar.succeeded()) {
    	System.out.println("9");
        String id = ar.result();
        
        JsonObject query = new JsonObject().put("_id", id);
        JsonObject update = new JsonObject()
          .put("$set", new JsonObject().put("username", userToRegister.getString("username")));

        mongoClient.updateCollection("user", query, update, res -> {
          if (res.succeeded()) {
            message.reply(Json.encode(userToRegister));
          }else{
            message.fail(2, "insert failed: " + res.cause().getMessage());
          }
        });

      } else{
    	  System.out.println("10");
        message.fail(2, "insert failed: " + ar.cause().getMessage());
      }
    });

  }
}
