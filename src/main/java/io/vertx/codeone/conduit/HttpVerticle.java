package io.vertx.codeone.conduit;

import java.util.Arrays;
import java.util.logging.Handler;

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
import io.vertx.ext.web.Route;
//import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class HttpVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		Router baseRouter = Router.router(vertx);
		Router apiRouter = Router.router(vertx);
		baseRouter.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain").end("Hello test VERTX");
		});
		apiRouter.route("/user*").handler(BodyHandler.create());
		apiRouter.post("/users").handler(this::registerUser);
		apiRouter.post("/users/*").handler(ctx -> {
			ctx.response().end("apiRoute POST users");
		});
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
		vertx.createHttpServer().requestHandler(baseRouter).listen(3000, result -> { // in version VERTx 4. Router
																						// implements
																						// Handler<HttpServerRequest>.
																						// (::accept)
			if (result.succeeded()) {
				Future.succeededFuture();
			} else {
				Future.failedFuture(result.cause());
			}
		});
	}

	private void registerUser(RoutingContext routingContext) {
User user1 = new User("mirek", "sw3d96@gmail.com", null, "mirekmirek", null, "jwt.token.here");
		// User user2 = new User("Lemmy", "lemmy@gmail.com", null, "lemmylemmy", null,
		// "jwt.token.here");
 routingContext.response()
 	.setStatusCode(201)
 	.putHeader("Content-Type","application/json; charset=utf-8")
 	.end(Json.encodePrettily(user1.toConduitString()));
//			routingContext.response().setStatusCode(201).putHeader("Content-Type", "application/json; charset=utf-8")
//			.end(Json.encodePrettily(user2.toConduitString()));

		JsonObject message = new JsonObject()
				.put("action","register-user")
				.put("user", routingContext.body().asJsonObject().getJsonObject("user")); // ?? GETBODYASJSON. body().asJsonObject()
		
//		vertx.<JsonObject>eventBus().request("persistence-address", message, ar -> { //REQUEST <- send
//			if(ar.succeeded()) {
//				User returnedUser = Json.decodeValue(ar.result().body().toString(), User.class);
//				returnedUser.setToken("jwt.token.here");
//				routingContext.response()
//					.setStatusCode(201)
//					.putHeader("Content-Type", "application/json; charset=utf-8")
//					.end(Json.encodePrettily(returnedUser.toConduitString()));
//			} else {
//				routingContext.response()
//				.setStatusCode(500)
//				.putHeader("Content-Type", "application/json; charset=utf-8")
//				.end(Json.encodePrettily(ar.cause().getMessage()));
//			}
//		});

	}
}

//@Override
//public Vertx getVertx() {
//	// TODO Auto-generated method stub
//	return null;
//}
//
//@Override
//public void init(Vertx vertx, Context context) {
//	// TODO Auto-generated method stub
//	
//}
//
//@Override
//public void start(Promise<Void> startPromise) throws Exception {
//	// TODO Auto-generated method stub
//	Router baseRouter = Router.router(vertx);
//}
//
//@Override
//public void stop(Promise<Void> stopPromise) throws Exception {
//	// TODO Auto-generated method stub
//	
//}