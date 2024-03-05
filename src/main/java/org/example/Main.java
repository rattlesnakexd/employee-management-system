package org.example;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static final int PORT = 8000;
    public static final String GITHUB_CLIENT_ID = "09859816888297ad874b";
    public static final String GITHUB_CLIENT_SECRET = "678b658307ad1f651c21bf942eaefa73a082e694";

    public static void main(String[] args) throws Exception {
        System.out.println("Main program started");

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/employees", new EmployeeHandler());
        server.createContext("/api/roles", new RoleHandler());
        server.createContext("/auth", new OAuthHandler(GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET));
        server.createContext("/auth/callback", new OAuthCallbackHandler(GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET));
        server.setExecutor(null);

        // Start server
        server.start();
        System.out.println("Server started on port " + PORT);
    }
}
