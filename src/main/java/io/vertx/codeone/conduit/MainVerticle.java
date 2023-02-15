package io.vertx.codeone.conduit;

import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) {

//	vertx.deployVerticle(new HttpVerticle());
//		List<Future> allFutures = ImmutableList.of(deployVerticle(HttpVerticle.class.getName()).future(),deployVerticle(PersistenceVerticle.class.getName()).future());
		CompositeFuture.all(deployVerticle(HttpVerticle.class.getName()).future(),
				deployVerticle(PersistenceVerticle.class.getName()).future()).onComplete(f -> {
					if (f.succeeded()) {
						startPromise.complete();
					} else {
						startPromise.fail(f.cause());
					}
				});
	}

	Promise<Void> deployVerticle(String verticleName) {
		Promise<Void> retVal = Promise.promise();
		vertx.deployVerticle(verticleName, event -> {
			if (event.succeeded()) {
				retVal.complete();
			} else {
				retVal.fail(event.cause());
			}
		});
		return retVal;
	}
}
