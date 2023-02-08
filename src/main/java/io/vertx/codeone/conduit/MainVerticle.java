package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    vertx.createHttpServer()
        .requestHandler(req -> req.response().end("Hello XXXXXX"))
        .listen(8080);
  }

}
