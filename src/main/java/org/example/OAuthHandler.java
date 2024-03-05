package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class OAuthHandler implements HttpHandler {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public OAuthHandler(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = "http://localhost:8000/auth/callback";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            handleAuthorizationRequest(exchange);
        }
    }

    private void handleAuthorizationRequest(HttpExchange exchange) throws IOException {
        // Redirect user to OAuth provider's authorization endpoint
        String authorizationUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&scope=user"; // Adjust scope as needed
        exchange.getResponseHeaders().set("Location", authorizationUrl);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
    }
}
