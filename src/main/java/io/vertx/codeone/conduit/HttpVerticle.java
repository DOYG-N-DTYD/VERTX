package io.vertx.codeone.conduit;

import java.util.Arrays;
import java.util.logging.Handler;

import com.fasterxml.jackson.core.JsonParser;
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
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Route;
//import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

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
//		apiRouter.post("/users/*").handler(ctx -> {
//			ctx.response().end("apiRoute POST users");
//		});
		apiRouter.get("/users/*").handler(ctx -> {
			ctx.response().end("apiRoute GET users");
		});
		baseRouter.route("/usersAPI/*").subRouter(apiRouter); // So baseRouter will be available
																// http://localhost:3000/usersAPI/users/us_1
		// apiRouter.route("/user*").handler(BodyHandler.create());
		// apiRouter.post("/users").handler(this::registerUser);
		// baseRouter.mountSubRouter("/api", apiRouter); // ??

		// System.out.println("OUTPUT apiRouter+ " +
		// Arrays.toString(apiRouter.getRoutes().toArray()));
		// baseRouter.route("/*").subRouter(registerRouter);
		// registerRouter.post("/register").handler(this::registerUser);

//		registerRouter.get("/register/*").handler(ctx -> {
//			ctx.response().end("apiRoute GET register");
//		});
//		registerRouter.post("/register/*").handler(ctx -> {
//			
//			ctx.response().end("apiRoute POST register");
//		});

		registerRouter.route("/register*").handler(BodyHandler.create());
		registerRouter.post("/register").handler(this::registerUser);
		baseRouter.route("/*").subRouter(registerRouter);
		
		loginRouter.route("/login*").handler(BodyHandler.create());
		loginRouter.post("/login").handler(this::loginUser);
		baseRouter.route("/*").subRouter(loginRouter);

//		registerRouter.route("/register*").handler(BodyHandler.create());
//		registerRouter.post("/register").handler(this::registerUser);
//		registerRouter.get("/register").handler(this::registerUser);
//		baseRouter.route("/*").subRouter(registerRouter);
//TODO		
//			loginRouter.post("/login/*").handler(ctx -> {
//				ctx.response().end("apiRoute POST login");
//			});
//			baseRouter.route("/*").subRouter(loginRouter);
//		
//				itemsRouter.get("/items/*").handler(ctx -> {
//					ctx.response().end("apiRoute GET items");
//				});
//				baseRouter.route("/*").subRouter(itemsRouter);
//				itemsRouter.post("/items/*").handler(ctx -> {
//					ctx.response().end("apiRoute post items");
//				});
//				baseRouter.route("/*").subRouter(itemsRouter);

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
//User user1 = new User("mirek", "sw3d96@gmail.com", "BIO", "mirekmirek", "IMAGE", "jwt.token.here", "UUIDtest");
// routingContext.response()
// 	.setStatusCode(201)
// 	.putHeader("Content-Type","application/json; charset=utf-8")
// 	.end(Json.encodePrettily(user1.toConduitString()));
//User user2 = new User("Lemmy", "lemmy@gmail.com", null, "lemmylemmy", null,
		// "jwt.token.here");
//			routingContext.response().setStatusCode(201).putHeader("Content-Type", "application/json; charset=utf-8")
//			.end(Json.encodePrettily(user2.toConduitString()));

//		JsonObject message = new JsonObject()
//				.put("action","register-user")
//				//.put("user", user1);//routingContext.getBodyAsJson().getJsonObject("user"));
//				.put("user", routingContext.body().asJsonObject().getJsonObject("user")); // ?? GETBODYASJSON. body().asJsonObject()
//
//		routingContext.response()
//		.setStatusCode(201)
//		.putHeader("Content-Type", "application/json; charset=utf-8")
//		.end(Json.encodePrettily(routingContext.body().asJsonObject().getJsonObject("user")));

		JsonObject message = new JsonObject().put("action", "register-user")
				// .put("user", routingContext.body().asJsonObject().getJsonObject("user"));
				// //.getJsonObject("user")
				.put("user", routingContext.getBodyAsJson().getJsonObject("user"));

		System.out.println("XXXXXXXXXXXXXXXXXXx " + routingContext.body().asJsonObject().getJsonObject("user"));
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
						// .putHeader("Content-Length", String.valueOf(userResult.toString().length()))
						.end(Json.encodePrettily(returnedUser.toConduitString()))
						;
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						// .putHeader("Content-Length", String.valueOf(userResult.toString().length()))
						.end(Json.encodePrettily(ar.cause().getMessage()));
			}
		});

//		vertx.<JsonObject>eventBus().request("persistence-address", message, ar -> { //REQUEST <- send
//			if(ar.succeeded()) {
//				System.out.println("HTTPverticle method registerUser SUCCESS 1");
//				User returnedUser = Json.decodeValue(ar.result().body().toString(), User.class);
//				//returnedUser.setToken("jwt.token.here");
//				String token = jwtAuth.generateToken(new JsonObject().put("email", returnedUser.getEmail()).put("password", returnedUser.getPassword()), new JWTOptions().setIgnoreExpiration(true));
//				returnedUser.setToken(token);
//				routingContext.response()
//					.setStatusCode(201)
//					.putHeader("Content-Type", "application/json; charset=utf-8")
//					.end(Json.encodePrettily(returnedUser.toConduitString()));
//				System.out.println("HTTPverticle method registerUser SUCCESS 2");
//			} else {
//				System.out.println("HTTPverticle method registerUser ISSUE 1");
//				routingContext.response()
//				.setStatusCode(500)
//				.putHeader("Content-Type", "application/json; charset=utf-8")
//				.end(Json.encodePrettily(ar.cause().getMessage()));
//				System.out.println("HTTPverticle method registerUser ISSUE 2");
//			}
//		});

	}
	
	private void loginUser(RoutingContext routingContext) {
		// TODO RETURN TOKEN
		JsonObject message = new JsonObject().put("action", "login-user")
				// .put("user", routingContext.body().asJsonObject().getJsonObject("user"));
				// //.getJsonObject("user")
				.put("user", routingContext.getBodyAsJson().getJsonObject("user"));
		
		vertx.eventBus().request("login-address", message, ar -> {
			if (ar.succeeded()) {
				Gson g = new Gson();
				System.out.println("ISSUE @@ "+ar.result().body().toString());
				User returnedUser = g.fromJson(ar.result().body().toString(), User.class);
//	    		String token = jwtAuth.generateToken(new JsonObject()												// TODO ISSUE WITH TOKEN
//	    				.put("email", returnedUser.getEmail())
//	    				.put("password", returnedUser.getPassword()), new JWTOptions().setIgnoreExpiration(true));
//	            returnedUser.setToken(token);
//				returnedUser.setToken("jwt.token.here TEST");
				routingContext.response().setStatusCode(201)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						// .putHeader("Content-Length", String.valueOf(userResult.toString().length()))
						.end(Json.encodePrettily(returnedUser.toConduitString()));
			} else {
				routingContext.response().setStatusCode(500)
						.putHeader("Content-Type", "application/json; charset=utf-8")
						// .putHeader("Content-Length", String.valueOf(userResult.toString().length()))
						.end(Json.encodePrettily(ar.cause().getMessage()));
			}
		});
	}
}
