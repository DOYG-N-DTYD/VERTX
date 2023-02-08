package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpVerticle implements Verticle {
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		// TODO Auto-generated method stub
		Router baseRouter = Router.router(vertx);
		Router apiRouter = Router.router(vertx);

		baseRouter.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain").end("Hello CodeOne");
		});

		apiRouter.route("/user*").handler(BodyHandler.create());
		apiRouter.post("/users").handler(this::registerUser);
		baseRouter.mountSubRouter("/api", apiRouter);

		vertx.createHttpServer().requestHandler(baseRouter::accept).listen(8080, result -> {
			if (result.succeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(result.cause());
			}
		});
	}

	private void registerUser(RoutingContext routingContext) {
		User user = new User("Mirek","sw3d96@gmail.com",null,"mirekmirek",null,"jwt.token.here");
		routingContext.response();
			.setStatusCode(201)
			.putHeader("Content-Type","application/json; charset=utf-8")
			.end(Json.encodePrettily(user.toConduitJson()));
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