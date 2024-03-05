package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class RoleHandler implements HttpHandler {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String accessToken = extractAccessToken(exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER));
        String[] authorizedRoles = {"Administrator"};
        if (validateAccessToken(accessToken) && validateRole(authorizedRoles)) {
            String[] pathPieces = getRequestPathPieces(exchange.getRequestURI());

            if (pathPieces == null || pathPieces.length < 2) {
                throw new IOException("Null/malformed request URI path: " + exchange.getRequestURI().getPath());
            }
            if (!"roles".equals(pathPieces[1])) {
                throw new IOException("Missing or unexpected domain (first) part of request URI path, should have started with 'employees': " + exchange.getRequestURI().getPath());
            }
            String operation = pathPieces[2];
            if (operation == null || operation.trim().isEmpty()) {
                throw new IOException("Operation (second) piece of URI Path is missing or empty: " + exchange.getRequestURI().getPath());
            }

            switch (operation) {
                case "getAll":
                    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                        getAllRoles(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "GET");
                    }
                case "create":
                    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                        createRole(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "POST");
                    }
                    break;
                case "update":
                    if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
                        updateRole(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "PUT");
                    }
                    break;
                case "delete":
                    if (exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                        deleteRole(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "DELETE");
                    }
                    break;
                case "get":
                    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                        getRole(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "GET");
                    }
                    break;
            }
        } else {
            sendUnauthorizedResponse(exchange);
        }
    }

    private void createRole(HttpExchange exchange) throws IOException {
        try {
            Connection connect = DBFunctions.getConnection();
            if (connect != null) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }

                Gson gson = new Gson();
                Role role = gson.fromJson(requestBody.toString(), Role.class);

                RoleDAO roleDAO = new RoleDAOImpl();
                roleDAO.create(connect, role);

                DBFunctions.closeConnection(connect);

                EmployeeHandler.Response response = new EmployeeHandler.Response("success", "Role created");
                sendResponse(exchange, 200, gson.toJson(response));
            }
        } catch (IOException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Internal Server Error");
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (SQLException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        }
    }

    private void updateRole(HttpExchange exchange) throws IOException {
        try {
            Connection connect = DBFunctions.getConnection();
            if (connect != null) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }

                Gson gson = new Gson();
                Role role = gson.fromJson(requestBody.toString(), Role.class);

                RoleDAO roleDAO = new RoleDAOImpl();
                role = roleDAO.update(connect, role);

                DBFunctions.closeConnection(connect);

                sendResponse(exchange, 200, gson.toJson(role));
            }
        } catch (IOException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Internal Server Error");
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (SQLException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        }
    }

    private void deleteRole(HttpExchange exchange) throws IOException {
        try {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] queryParams = query.split("&");
                String name = null;
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("name")) {
                        name = keyValue[1];
                        break;
                    }
                }

                if (name != null) {
                    Connection connect = DBFunctions.getConnection();
                    if (connect != null) {
                        RoleDAO roleDAO = new RoleDAOImpl();
                        roleDAO.delete(connect, name);
                        DBFunctions.closeConnection(connect);
                        Gson gson = new Gson();
                        EmployeeHandler.Response response = new EmployeeHandler.Response("success", "Role Deleted");
                        sendResponse(exchange, 200, gson.toJson(response));
                    } else {
                        Gson gson = new Gson();
                        EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Database Error: Failed to establish connection");
                        sendResponse(exchange, 500, gson.toJson(response));
                    }
                } else {
                    Gson gson = new Gson();
                    EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Invalid role name parameter");
                    sendResponse(exchange, 400, gson.toJson(response));
                }
            } else {
                Gson gson = new Gson();
                EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Missing role name parameter");
                sendResponse(exchange, 400, gson.toJson(response));
            }
        } catch (SQLException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (NumberFormatException e) {
            Gson gson = new Gson();
            EmployeeHandler.Response response = new EmployeeHandler.Response("error", "Invalid role name format");
            sendResponse(exchange, 400, gson.toJson(response));
        }
    }

    private void getRole(HttpExchange exchange) {
    }

    private void getAllRoles(HttpExchange exchange) {
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static String[] getRequestPathPieces(URI requestURI) {
        if (requestURI == null) {
            throw new IllegalArgumentException("Null URI requestURI provided!");
        }
        String path = requestURI.getPath();
        if (path != null && path.startsWith("/")) {
            path = (path.length() > 1) ? path.substring(1) : null;
        }
        return (path != null && !path.isEmpty()) ? path.split("/") : null;
    }

    static class Response {
        private final String status;
        private final String message;

        public Response(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private void sendMethodNotAllowedResponse(HttpExchange exchange, String allowedMethod) throws IOException {
        exchange.sendResponseHeaders(405, -1);
        exchange.getResponseHeaders().set("Allow", allowedMethod);
        exchange.getResponseBody().close();
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean validateAccessToken(String accessToken) {
        return Objects.equals(accessToken, AuthContext.getAccessToken()) && AuthContext.getAccessToken() != null;
    }

    private boolean validateRole(String[] authorizedRoles) {
        if (AuthContext.getEmployee() != null) {
            for (String role : authorizedRoles) {
                if (role.equals(AuthContext.getEmployee().getRole())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
        exchange.getResponseBody().close();
    }
}
