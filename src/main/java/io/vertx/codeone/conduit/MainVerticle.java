package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

	@Override
	  public void start(Promise<Void> startPromise) {

	vertx.deployVerticle(new HttpVerticle());
//	    CompositeFuture.all(
//	      deployVerticle(HttpVerticle.class.getName()),
//	      deployVerticle(PersistenceVerticle.class.getName())
//	    ).onComplete(f ->{										//setHandler -> onComplete
//	      if (f.succeeded()) {
//	    	 startPromise.complete();
//	      }else{
//	    	 startPromise.fail(f.cause());
//	      }
//	    });
	  }

	  Future<Void> deployVerticle(String verticleName) {
	    Future<Void> retVal = Future.future(null);			// NULL ???????		
	    vertx.deployVerticle(verticleName, event -> {
	      if (event.succeeded()) {
	        retVal.isComplete(); 							//complete -> isComplete ???????
	      }else{
	        retVal.failed();//fail(event.cause());
	        event.cause();									// ?????
	      }
	    });
	    return retVal;
	  }	
	
	
//	@Override
//	public void start(Promise<Void> startPromise) {
//    vertx.createHttpServer()
//        .requestHandler(req -> req.response().end("Hello XXXXXX"))
//        .listen(3000);
//	  vertx.deployVerticle(new HttpVerticle());

//		CompositeFuture
//				.all(deployVerticle(HttpVerticle.class.getName()), deployVerticle(PersistenceVerticle.class.getName()))
//				.onComplete(f -> { // ver4 onComplete
//					if (f.succeeded()) {
//						Future.succeededFuture(); // startFuture.complete() ??
//					} else {
//						Future.failedFuture(f.cause()); // startFuture.fail() ??
//					}
//				});
//}
//Future.succeededFuture();

//	Future<Void> deployVerticle(String verticleName) {
//		Future<Void> retVal = Future.future(null); // null ???
//		vertx.deployVerticle(verticleName, event -> {
//			if (event.succeeded()) {
//				retVal.onComplete(null); // .complete() ??
//			} else {
//				retVal.onFailure(null); // fail(event.cause()) ??
//			}
//		});
//		return retVal;
//	}
}
