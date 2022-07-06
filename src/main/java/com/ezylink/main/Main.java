package com.ezylink.main;

import com.ezylink.vertx.VertXServer;

public class Main {

    public static void main(String[] args) {
        VertXServer server = new VertXServer();
        server.start();
    }

}
