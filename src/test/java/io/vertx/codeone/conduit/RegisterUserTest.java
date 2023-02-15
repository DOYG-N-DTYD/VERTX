package io.vertx.codeone.conduit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;

/**
 * POST /api/users Request body: {"user":{ "username": "mirek", "email":
 * "sw3d96@gmail.com", "password": "mirekmirek" }}
 *
 * Response body: {"user":{ "email": "sw3d@gmail.com", "token": "jwt.token.here"
 * "username": "mirek" "bio": "duck duck duck" "image": null }}
 *
 */
@RunWith(VertxUnitRunner.class)
public class RegisterUserTest {
	private Vertx vertx;
	private WebClient webClient;

	@Before
	public void setup(TestContext testContext) {
		vertx = Vertx.vertx();
		webClient = WebClient.create(vertx);

		vertx.deployVerticle(new MainVerticle()); // not HttpVerticle but MainVerticle
	}

	@After
	public void tearDown(TestContext testContext) {
		vertx.close();
	}

	@Test
	public void testRegisterUser(TestContext testContext) {
		Async async = testContext.async();

		JsonObject userJson = new JsonObject().put("email", "sw3d@gmail.com").put("username", "mirek").put("password",
				"mirekmirek");

		JsonObject user = new JsonObject().put("user", userJson);

		webClient.post(3000, "localhost", "/register").putHeader("Content-Type", "application/json") // "/api/users/"
				// webClient.post(3000, "localhost", "/register").putHeader("Content-Type",
				// "application/json") // "/api/users/"
				.putHeader("X-requested-with", "XMLHttpRequest").sendJsonObject(user, ar -> {
					if (ar.succeeded()) {
						System.out.println("RegisterUserTest OK !!!");
						testContext.assertEquals(201, ar.result().statusCode());
						JsonObject returnedJson = ar.result().bodyAsJsonObject();
						JsonObject returnedUser = returnedJson.getJsonObject("user");
						testContext.assertEquals("mirek", returnedUser.getString("username"));
						testContext.assertEquals("sw3d96@gmail.com", returnedUser.getString("email"));
						// testContext.assertTrue(returnedUser.getString("token").length() > 3);
						testContext.assertNotNull(returnedUser.getString("token"));
						async.complete();
					}
				});
	}
}
