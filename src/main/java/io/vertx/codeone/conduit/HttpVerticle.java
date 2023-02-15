
package io.vertx.codeone.conduit;

import com.google.gson.Gson;

import io.vertx.codeone.conduit.models.User;
import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
//import io.vertx.core.Promise;
//import io.vertx.core.Verticle;
//import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
//import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle {

	JWTAuth jwtAuth;

	// @Override
	public void start(Promise<Void> startPromise) throws Exception {
//		JWTAuthOptions jwtao = new JWTAuthOptions(new JsonObject().put("keyStore", new JsonObject()
//			      .put("type", "jceks")
//			      .put("path", "keystore.jceks")
//			      .put("password", "secret")));
		// jwtAuth = JWTAuth.create(vertx, jwtao);
//		jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addJwk(
//				   new JsonObject()
//				   .put("kty", "RSA")
//				   .put("n", "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw")
//				   .put("e", "AQAB")
//				   .put("alg", "RS256")
//				   .put("kid", "2011-04-29")));

		Router baseRouter = Router.router(vertx);
		Router apiRouter = Router.router(vertx);
		Router registerRouter = Router.router(vertx);
		Router loginRouter = Router.router(vertx);
		Router itemsRouter = Router.router(vertx);
		baseRouter.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain").end("Hello test VERTX");
		});
		apiRouter.route("/user*").handler(BodyHandler.create());
		apiRouter.post("/users").handler(this::registerUser);
		apiRouter.get("/users/*").handler(ctx -> {
			ctx.response().end("apiRoute GET users");
		});
		baseRouter.route("/usersAPI/*").subRouter(apiRouter);

		registerRouter.route("/register*").handler(BodyHandler.create());
		registerRouter.post("/register").handler(this::registerUser);
		baseRouter.route("/*").subRouter(registerRouter);

		loginRouter.route("/login*").handler(BodyHandler.create());
		loginRouter.post("/login").handler(this::loginUser);
		baseRouter.route("/*").subRouter(loginRouter);

		itemsRouter.route("/items*").handler(BodyHandler.create());
		itemsRouter.post("/items").handler(this::itemsUser);
		itemsRouter.get("/items").handler(this::getItemsUser);
		baseRouter.route("/*").subRouter(itemsRouter);

		vertx.createHttpServer().requestHandler(baseRouter).listen(3000, result -> { // in version VERTx 4. Router
																						// implements
																						// Handler<HttpServerRequest>.
																						// (::accept)
			if (result.succeeded()) {
				Future.succeededFuture();
				startPromise.complete();
				System.out.println("HHTPverticle start method GOOD END");
			} else {
				Future.failedFuture(result.cause());
				startPromise.fail(result.cause());
				System.out.println("HHTPverticle start method BAD END");
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void registerUser(RoutingContext routingContext) {
		JsonObject message = new JsonObject().put("action", "register-user").put("user",
				routingContext.getBodyAsJson().getJsonObject("user"));
		vertx.eventBus().request("persistence-address", message, ar -> {
			if (ar.succeeded()) {
				Gson g = new Gson();
				User returnedUser = g.fromJson(ar.result().body().toString(), User.class);
//	    		String token = jwtAuth.generateToken(new JsonObject()												// TODO ISSUE WITH TOKEN
//	    				.put("email", returnedUser.getEmail())
//	    				.put("password", returnedUser.getPassword()), new JWTOptions().setIgnoreExpiration(true));
//	            returnedUser.setToken(token);
				returnedUser.setToken("jwt.token.here TEST");
				routingContext.response().setStatusCode(201)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(returnedUser.toConduitString()));
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(ar.cause().getMessage()));
			}
		});
	}

	private void loginUser(RoutingContext routingContext) {
		JsonObject message = new JsonObject().put("action", "login-user").put("user",
				routingContext.getBodyAsJson().getJsonObject("user"));

		vertx.eventBus().request("login-address", message, ar -> {
			if (ar.succeeded()) {
				Gson g = new Gson();
				System.out.println("ISSUE @@ " + ar.result().body().toString());
				User returnedUser = g.fromJson(ar.result().body().toString(), User.class);
//	    		String token = jwtAuth.generateToken(new JsonObject()												// TODO ISSUE WITH TOKEN
//	    				.put("email", returnedUser.getEmail())
//	    				.put("password", returnedUser.getPassword()), new JWTOptions().setIgnoreExpiration(true));
//	            returnedUser.setToken(token);
//				returnedUser.setToken("jwt.token.here TEST");
				routingContext.response().setStatusCode(201)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(returnedUser.toConduitString()));
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						.end(Json.encodePrettily(ar.cause().getMessage()));
			}
		});
	}

	private void itemsUser(RoutingContext routingContext) {
		System.out.println("httpVerticle method itemsUser");
		JsonObject message = new JsonObject().put("action", "items-user").put("user",
				routingContext.getBodyAsJson().getJsonObject("user"));
		System.out.println("PRE event bus");
		vertx.eventBus().request("item-address", message, ar -> {
			if (ar.succeeded()) {
//				Gson g = new Gson();
//				System.out.println("ISSUE @@ "+ar.result().body().toString());
//				User returnedUser = g.fromJson(ar.result().body().toString(), User.class);
//	    		String token = jwtAuth.generateToken(new JsonObject()												// TODO ISSUE WITH TOKEN
//	    				.put("email", returnedUser.getEmail())
//	    				.put("password", returnedUser.getPassword()), new JWTOptions().setIgnoreExpiration(true));
//	            returnedUser.setToken(token);
//				returnedUser.setToken("jwt.token.here TEST");
				routingContext.response().setStatusCode(201)
						.putHeader("Content-Type", "application/json; charset=utf-8").end("SUCCESS END ");
				System.out.println("POST event bus 111");
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8").end("ERROR END");
				System.out.println("PRE event 222");
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void getItemsUser(RoutingContext routingContext) {
		JsonObject message = new JsonObject().put("action", "get-items-user").put("user",
				routingContext.getBodyAsJson().getJsonObject("user"));
		vertx.eventBus().request("item-address", message, ar -> {
			if (ar.succeeded()) {
				routingContext.response().setStatusCode(201)
						.putHeader("Content-Type", "application/json; charset=utf-8").end("Success GET ITEMS request");
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8").end("ERROR END");
			}
		});
	}
}
