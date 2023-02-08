package io.vertx.codeone.conduit;

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
//import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class HttpVerticle extends AbstractVerticle {

	@SuppressWarnings("deprecation")
	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		Router baseRouter = Router.router(vertx);
		Router apiRouter = Router.router(vertx);
		baseRouter.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain").end("Hello testWithouDeploy HTTPVERTICLE.java");
		});

		apiRouter.route("/user*").handler(BodyHandler.create());
		apiRouter.post("/users").handler(this::registerUser);
		baseRouter.mountSubRouter("/api", apiRouter); // ??

		vertx.createHttpServer().requestHandler(baseRouter).listen(3000, result -> { // in version VERTx 4. Router
																						// implements
																						// Handler<HttpServerRequest>.
																						// (::accept)
			if (result.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(result.cause());
			}
		});
	}

	private void registerUser(RoutingContext routingContext) {
		User user = new User("Mirek", "sw3d96@gmail.com", null, "mirekmirek", null, "jwt.token.here");
		routingContext.response().setStatusCode(201).putHeader("Content-Type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(user.toConduitString()));
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