package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DAO <T>{
    T get(Connection connection ,int Id) throws SQLException;
    List<T> getAll(Connection connection) throws SQLException;
    T create(Connection connection,T dataObject) throws SQLException;
    T update(Connection connection,T dataObject) throws SQLException;

}
