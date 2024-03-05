package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class OAuthCallbackHandler implements HttpHandler {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public OAuthCallbackHandler(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = "http://localhost:8000/auth/callback";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String code = exchange.getRequestURI().getQuery().split("=")[1];
            String accessToken = exchangeAuthorizationCodeForAccessToken(code);
            AuthContext.setAccessToken(accessToken);
            JSONObject userInfo = retrieveUserInfoFromGitHub(accessToken);
            String userName = userInfo.getString("login");
            Employee employee = null;
            try {
                employee = checkUser(userName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("access_token", accessToken);

            if (employee != null) {
                Gson gson = new Gson();
                String employeeJson = gson.toJson(employee);
                jsonResponse.put("employee", new JSONObject(employeeJson));
                AuthContext.setEmployee(employee);
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, jsonResponse.toString().getBytes().length);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write(jsonResponse.toString().getBytes());
            responseBody.close();
        }
    }

    private String exchangeAuthorizationCodeForAccessToken(String authorizationCode) throws IOException {
        String requestBody = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + authorizationCode +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpURLConnection connection = null;
        try {
            URI uri = new URI(tokenUrl);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.getBytes());
            }

            try (InputStream inputStream = connection.getInputStream()) {
                String response = new Scanner(inputStream).useDelimiter("\\A").next();
                Map<String, String> tokenResponse = parseFormData(response);
                return tokenResponse.get("access_token");
            }
        } catch (Exception e) {
            throw new IOException("Failed to exchange authorization code for access token", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONObject retrieveUserInfoFromGitHub(String accessToken) throws IOException {
        String userDetailsUrl = "https://api.github.com/user";
        HttpURLConnection connection = null;
        try {
            URI uri = new URI(userDetailsUrl);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            InputStream inputStream = connection.getInputStream();
            String response = new Scanner(inputStream).useDelimiter("\\A").next();
            return new JSONObject(response);
        } catch (Exception e) {
            throw new IOException("Failed to retrieve user information from GitHub", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Employee checkUser(String userName) throws SQLException {
        Employee employee = null;
        try {
            Connection connect = DBFunctions.getConnection();
            if (connect != null) {
                EmployeeDAO employeeDAO = new EmployeeDAOImp();
                employee = employeeDAO.getUserByUserName(connect, userName);
                DBFunctions.closeConnection(connect);
            }
        } catch (SQLException e) {
            throw e;
        }
        return employee;
    }

    private Map<String, String> parseFormData(String inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String formData = scanner.hasNext() ? scanner.next() : "";
        String[] pairs = formData.split("&");

        Map<String, String> params = new HashMap<>();
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
