package com.spacework.controller.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class HealthCheckHandler implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"message\": \"Servidor SpaceWork ejecutándose correctamente\",\n" +
                "  \"timestamp\": \"" + new java.util.Date() + "\",\n" +
                "  \"url\": \"http://localhost:8080\"\n" +
                "}";
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
