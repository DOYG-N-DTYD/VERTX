package io.vertx.codeone.conduit;

import java.util.UUID;

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
import io.vertx.ext.auth.mongo.MongoUserUtil;
import io.vertx.ext.mongo.MongoClient;

public class PersistenceVerticle extends AbstractVerticle{

  // for DB access
  private MongoClient mongoClient;
  // Authentication provider for logging in
  @SuppressWarnings("deprecation")
private MongoAuth loginAuthProvider;
private MongoAuthentication userAuthentication;
//private MongoAuthorization userAuthorization;
//private MongoAuthenticationOptions userAuthenticationOptions;  
  //
  MongoUserUtil inserterToDb;

  //@SuppressWarnings("deprecation")
  //@Override
  public void start(Promise<Void> startPromise) {

    // Configure the MongoClient inline.  This should be externalized into a config file
    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("db_name", config().getString("db_name", "conduit_dev")).put("connection_string", config().getString("connection_string", "mongodb://localhost:27017")));
    inserterToDb = MongoUserUtil.create(mongoClient);
    // Configure authentication with MongoDB
    //loginAuthProvider = MongoAuth.create(mongoClient, new JsonObject());
    MongoAuthenticationOptions userAuthenticationOptions = new MongoAuthenticationOptions();
    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
    userAuthenticationOptions.setUsernameField("email");
    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"); 
    userAuthenticationOptions.setUsernameCredentialField("email"); // PROBLEM !!!!!
    System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"); 
    MongoAuthentication.create(mongoClient, userAuthenticationOptions);
    //userAuthorization.create( mongoClient, userAuthenticationOptions);
    //loginAuthProvider.setUsernameField("email");
    //loginAuthProvider.setUsernameCredentialField("email");
    
    EventBus eventBus = vertx.eventBus();
    MessageConsumer<JsonObject> consumer = eventBus.consumer("persistence-address");
    consumer.handler(message -> {
      String action = message.body().getString("action");
      
      switch (action) {
        case "register-user":
          registerUser(message);
          break;
        default:
          message.fail(1, "Unkown action: " + message.body());
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
    JsonObject userToRegister = message.body().getJsonObject("user");
    System.out.println("8 "+ userToRegister);
    
    inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {
    	if(ar.succeeded()) {
    		String id = ar.result();
    		
    		System.out.println("ID " + id);
    		UUID testUUID = UUID.nameUUIDFromBytes(id.getBytes());
    		System.out.println("UUID " + testUUID);
    		
            //JsonObject query = new JsonObject().put("_id", id);//.put("_UUID","generatedUUID");
    		JsonObject query = new JsonObject().put("_id", id);
    		JsonObject userData = new JsonObject();
            userData.put("username", userToRegister.getString("username"));
            userData.put("UUID", testUUID.toString());
            JsonObject update = new JsonObject().put("$set", userData);

            mongoClient.updateCollection("user", query, update, res -> {
              if (res.succeeded()) {
            	  System.out.println("SUCCES "+ userToRegister);
                message.reply(Json.encode(userToRegister));
              }else{
            	  System.out.println("NOOOOO");
                message.fail(2, "insert failed: " + res.cause().getMessage());
              }
            });
    		System.out.println("Inserter OK");
    	}else {
    		message.fail(2, "insert failed: " + ar.cause().getMessage());
    		System.out.println("Inserter Not Success");
    	}
    });
    
//loginAuthProvider.insertUser(userToRegister.getString("email"), userToRegister.getString("password"), null, null, ar -> {   // OK ADDED TO DB !!
////    inserterToDb.createUser(userToRegister.getString("email"), userToRegister.getString("password"), ar -> {  			// NOT WORKING 
//    if (ar.succeeded()) {
//        String id = ar.result();
//        JsonObject query = new JsonObject().put("_id", id);//.put("_UUID","generatedUUID");
//        JsonObject update = new JsonObject()
//          .put("$set", new JsonObject().put("username", userToRegister.getString("username")));
//
//        mongoClient.updateCollection("user", query, update, res -> {
//          if (res.succeeded()) {
//        	  System.out.println("SUCCES "+ userToRegister);
//            message.reply(Json.encode(userToRegister));
//          }else{
//        	  System.out.println("NOOOOO");
//            message.fail(2, "insert failed: " + res.cause().getMessage());
//          }
//        });
//
//      } else{
//    	  System.out.println("10");
//        message.fail(2, "insert failed: " + ar.cause().getMessage());
//      }
//    });

  }
}
