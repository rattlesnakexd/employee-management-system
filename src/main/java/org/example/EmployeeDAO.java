package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public interface EmployeeDAO extends DAO<Employee> {
    Boolean delete(Connection connection, int Id) throws SQLException;
    Employee getUserByUserName (Connection connection, String userName) throws SQLException;
}
