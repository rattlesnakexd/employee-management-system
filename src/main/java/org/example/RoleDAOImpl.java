package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class RoleDAOImpl implements RoleDAO{
    @Override
    public Role get(Connection connection, int Id) throws SQLException {
        return null;
    }

    @Override
    public List<Role> getAll(Connection connection) throws SQLException {
        return null;
    }

    @Override
    public Role create(Connection connection, Role dataObject) throws SQLException {
        String query = "INSERT INTO roles (role, can_create, can_update, can_delete, can_view) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, dataObject.getRole());
        preparedStatement.setBoolean(2, dataObject.getCanCreate());
        preparedStatement.setBoolean(3, dataObject.getCanUpdate());
        preparedStatement.setBoolean(4, dataObject.getCanDelete());
        preparedStatement.setBoolean(5, dataObject.getCanView());
        preparedStatement.executeUpdate();
        return null;
    }

    @Override
    public Role update(Connection connection, Role dataObject) throws SQLException {
        String query = "UPDATE roles SET can_create = ?, can_update = ?, can_delete = ?, can_view = ?, new_roles =? WHERE role = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setBoolean(1, dataObject.getCanCreate());
        preparedStatement.setBoolean(2, dataObject.getCanUpdate());
        preparedStatement.setBoolean(3,dataObject.getCanDelete());
        preparedStatement.setBoolean(4, dataObject.getCanView());
        preparedStatement.setBoolean(5, dataObject.getNewRole());
        preparedStatement.setString(6, dataObject.getRole());
        int result = preparedStatement.executeUpdate();
        return dataObject;
    }

    @Override
    public Boolean delete(Connection connection, String name) throws SQLException {
        String query = "DELETE from roles WHERE role = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        return null;
    }
}
