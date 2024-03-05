package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public interface RoleDAO extends DAO<Role>{
    Boolean delete(Connection connection, String name) throws SQLException;
}
