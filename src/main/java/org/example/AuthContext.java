package org.example;

public class AuthContext {
    static String accessToken = null;
    static Employee employee = null;

    public AuthContext(String accessToken) {
        AuthContext.accessToken = accessToken;
    }

    public static Employee getEmployee() {
        return employee;
    }

    public static void setEmployee(Employee employee) {
        AuthContext.employee = employee;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        AuthContext.accessToken = accessToken;
    }
}
