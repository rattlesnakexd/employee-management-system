package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImp implements EmployeeDAO {
    @Override
    public Employee get(Connection connection, int Id) throws SQLException {
        Employee employee = null;
        String query = "SELECT employee_id, first_name, last_name, role FROM Employee WHERE employee_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, Id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet != null && resultSet.next()) {
            employee = new Employee(resultSet.getInt("employee_id"), resultSet.getString("first_name"), resultSet.getString("last_name"), resultSet.getString("role"));
        }
        return employee;
    }

    @Override
    public List<Employee> getAll(Connection connection) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT employee_id, first_name, last_name, role FROM Employee";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int employeeId = resultSet.getInt("employee_id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String role = resultSet.getString("role");
                Employee employee = new Employee(employeeId, firstName, lastName, role);
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public Employee create(Connection connection, Employee dataObject) throws SQLException {
        String query = "INSERT INTO employee (employee_id, first_name, last_name, role, username) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, dataObject.getEmployeeId());
        preparedStatement.setString(2, dataObject.getFirstName());
        preparedStatement.setString(3, dataObject.getLastName());
        preparedStatement.setString(4, dataObject.getRole());
        preparedStatement.setString(5,dataObject.getUserName());
        preparedStatement.executeUpdate();
        return dataObject;
    }

    @Override
    public Employee update(Connection connection, Employee dataObject) throws SQLException {
        String query = "UPDATE employee SET first_name = ?, last_name = ?, role = ? WHERE employee_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, dataObject.getFirstName());
        preparedStatement.setString(2, dataObject.getLastName());
        preparedStatement.setString(3, dataObject.getRole());
        preparedStatement.setInt(4, dataObject.getEmployeeId());
        preparedStatement.executeUpdate();
        return dataObject;
    }

    @Override
    public Boolean delete(Connection connection, int Id) throws SQLException {
        String query = "DELETE from employee WHERE employee_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, Id);
        preparedStatement.executeUpdate();
        return null;
    }

    @Override
    public Employee getUserByUserName(Connection connection, String userName) throws SQLException {
        Employee employee = null;
        String query = "SELECT employee_id, first_name, last_name, role FROM Employee Where username = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, userName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet != null && resultSet.next()) {
            employee = new Employee(resultSet.getInt("employee_id"), resultSet.getString("first_name"), resultSet.getString("last_name"), resultSet.getString("role"));
        }
        return employee;
    }
}
