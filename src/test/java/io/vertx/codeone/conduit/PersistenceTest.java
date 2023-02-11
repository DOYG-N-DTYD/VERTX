package io.vertx.codeone.conduit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.codeone.conduit.models.User;
import io.vertx.codeone.conduit.PersistenceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PersistenceTest {
	private Vertx vertx;
	
	@Before
	public void setUp(TestContext testContext) {
		vertx = Vertx.vertx();
		vertx.deployVerticle(PersistenceVerticle.class.getName(), testContext.asyncAssertSuccess());
		//vertx.deployVerticle(new PersistenceVerticle(), testContext.asyncAssertSuccess());
	}
	
	@After
	public void tearDown(TestContext testContext) {
		vertx.close();
	}
	
	// user{{"test":"test" ...}}
	//Param textContext 
	@Test
	public void testRegisterUserMessage(TestContext testContext) {
		Async async = testContext.async();
		
		JsonObject userToRegister = new JsonObject()
				.put("email", "sw3d@gmail.com")
				.put("username", "mirek")
				.put("password","mirekmirek");
		
		JsonObject message = new JsonObject()
				.put("action","register-user")
				.put("user", userToRegister);
		
		vertx.<JsonObject>eventBus().request("persistence-address", message, ar -> { // send -> in ver4 -> request
			if(ar.succeeded()) {
				testContext.assertNotNull(ar.result().body());
				System.out.println("!!!!!!!!!!!!!!!!!!!!!  "+ar.toString());
				System.out.println("!!!!!!!!!!!!!!!!!!!!!  "+ar.result().toString());
				System.out.println("!!!!!!!!!!!!!!!!!!!!!  "+ar.result().body().toString());
				User returnedUser = Json.decodeValue(ar.result().body().toString(), User.class); // PROBLEM DECODE :(
				testContext.assertEquals("sw3d@gmail.com",returnedUser.getEmail());
				testContext.assertEquals("mirek", returnedUser.getUsername());
				async.complete();
			}else {
				testContext.assertTrue(ar.succeeded());
				async.complete();
			}
		});
	}
}
