package com.ezylink.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class VertXServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertXServer.class);

    public void start() {
        Vertx vertx = Vertx.vertx();

        final HttpServer server;
        final Router router = Router.router(vertx);
        final RequestHandler requestHandler = new RequestHandler();

        server = vertx.createHttpServer();
        requestHandler.handleRequest(router);
        server.requestHandler(router).listen(
                8080, ar ->
                {
                    if (ar.succeeded()) {
                        LOGGER.info("Vertx Server is running...");
                    }
                }
        );
    }

}
