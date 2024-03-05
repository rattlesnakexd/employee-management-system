package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EmployeeHandler implements HttpHandler {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String accessToken = extractAccessToken(exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER));
        String[] authorizedRoles = {"Administrator", "Manager"};
        if (validateAccessToken(accessToken) && validateRole(authorizedRoles)) {
            String[] pathPieces = getRequestPathPieces(exchange.getRequestURI());

            if (pathPieces == null || pathPieces.length < 2) {
                throw new IOException("Null/malformed request URI path: " + exchange.getRequestURI().getPath());
            }

            if (!"employees".equals(pathPieces[1])) {
                throw new IOException("Missing or unexpected domain (first) part of request URI path, should have started with 'employees': " + exchange.getRequestURI().getPath());
            }

            String operation = pathPieces[2];

            if (operation == null || operation.trim().isEmpty()) {
                throw new IOException("Operation (second) piece of URI Path is missing or empty: " + exchange.getRequestURI().getPath());
            }

            switch (operation) {
                case "getAll":
                    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                        getAllEmployees(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "GET");
                    }
                case "create":
                    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                        createEmployee(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "POST");
                    }
                    break;
                case "update":
                    if (exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
                        updateEmployee(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "PUT");
                    }
                    break;
                case "delete":
                    if (exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                        deleteEmployee(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "DELETE");
                    }
                    break;
                case "get":
                    if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                        getEmployee(exchange);
                    } else {
                        sendMethodNotAllowedResponse(exchange, "GET");
                    }
                    break;
            }
        } else {
            sendUnauthorizedResponse(exchange);
        }
    }

    private boolean validateAccessToken(String accessToken) {
        return Objects.equals(accessToken, AuthContext.getAccessToken());
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

    private void getAllEmployees(HttpExchange exchange) throws IOException {
        try {
            Connection connect = DBFunctions.getConnection();
            if (connect != null) {
                EmployeeDAO employeeDAO = new EmployeeDAOImp();
                List<Employee> employees = employeeDAO.getAll(connect);
                DBFunctions.closeConnection(connect);
                Gson gson = new Gson();
                sendResponse(exchange, 200, gson.toJson(employees));
            } else {
                Gson gson = new Gson();
                Response response = new Response("error", "Database Error: Failed to establish connection");
                sendResponse(exchange, 500, gson.toJson(response));
            }
        } catch (SQLException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        }
    }

    private void getEmployee(HttpExchange exchange) throws IOException {
        try {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] queryParams = query.split("&");
                int employee_id = -1;
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("employee_id")) {
                        employee_id = Integer.parseInt(keyValue[1]);
                        break;
                    }
                }
                if (employee_id != -1) {
                    Connection connect = DBFunctions.getConnection();
                    if (connect != null) {
                        EmployeeDAO employeeDAO = new EmployeeDAOImp();
                        Employee employee = employeeDAO.get(connect, employee_id);
                        DBFunctions.closeConnection(connect);
                        Gson gson = new Gson();
                        sendResponse(exchange, 200, gson.toJson(employee));
                    } else {
                        Gson gson = new Gson();
                        Response response = new Response("error", "Database Error: Failed to establish connection");
                        sendResponse(exchange, 500, gson.toJson(response));
                    }
                } else {
                    Gson gson = new Gson();
                    Response response = new Response("error", "Invalid employee ID parameter");
                    sendResponse(exchange, 400, gson.toJson(response));
                }
            } else {
                Gson gson = new Gson();
                Response response = new Response("error", "Missing employee ID parameter");
                sendResponse(exchange, 400, gson.toJson(response));
            }
        } catch (SQLException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (NumberFormatException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Invalid employee ID format");
            sendResponse(exchange, 400, gson.toJson(response));
        }
    }

    private void deleteEmployee(HttpExchange exchange) throws IOException {
        try {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            if (query != null && !query.isEmpty()) {
                String[] queryParams = query.split("&");
                int employee_id = -1;
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("employee_id")) {
                        employee_id = Integer.parseInt(keyValue[1]);
                        break;
                    }
                }

                if (employee_id != -1) {
                    Connection connect = DBFunctions.getConnection();
                    if (connect != null) {
                        EmployeeDAO employeeDAO = new EmployeeDAOImp();
                        employeeDAO.delete(connect, employee_id);
                        DBFunctions.closeConnection(connect);
                        Gson gson = new Gson();
                        Response response = new Response("success", "Employee Deleted");
                        sendResponse(exchange, 200, gson.toJson(response));
                    } else {
                        Gson gson = new Gson();
                        Response response = new Response("error", "Database Error: Failed to establish connection");
                        sendResponse(exchange, 500, gson.toJson(response));
                    }
                } else {
                    Gson gson = new Gson();
                    Response response = new Response("error", "Invalid employee ID parameter");
                    sendResponse(exchange, 400, gson.toJson(response));
                }
            } else {
                Gson gson = new Gson();
                Response response = new Response("error", "Missing employee ID parameter");
                sendResponse(exchange, 400, gson.toJson(response));
            }
        } catch (SQLException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (NumberFormatException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Invalid employee ID format");
            sendResponse(exchange, 400, gson.toJson(response));
        }
    }

    private void updateEmployee(HttpExchange exchange) throws IOException {
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
                Employee employee = gson.fromJson(requestBody.toString(), Employee.class);
                EmployeeDAO employeeDAO = new EmployeeDAOImp();
                Employee updatedEmployee = employeeDAO.update(connect, employee);
                DBFunctions.closeConnection(connect);
                sendResponse(exchange, 200, gson.toJson(updatedEmployee));
            }
        } catch (IOException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Internal Server Error");
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (SQLException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        }
    }

    private void createEmployee(HttpExchange exchange) throws IOException {
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
                Employee employee = gson.fromJson(requestBody.toString(), Employee.class);
                EmployeeDAO employeeDAO = new EmployeeDAOImp();
                employeeDAO.create(connect, employee);
                DBFunctions.closeConnection(connect);
                Response response = new Response("success", "Employee created");
                sendResponse(exchange, 200, gson.toJson(response));
            }
        } catch (IOException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Internal Server Error");
            sendResponse(exchange, 500, gson.toJson(response));
        } catch (SQLException e) {
            Gson gson = new Gson();
            Response response = new Response("error", "Database Error: " + e.getMessage());
            sendResponse(exchange, 500, gson.toJson(response));
        }
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

    private void sendUnauthorizedResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
        exchange.getResponseBody().close();
    }
}
