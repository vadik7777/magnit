/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.magnit.summa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author KonovalovVA
 */
class DBPool {

    private String url, user, password, driver;

    DBPool(String url, String user, String password, String driver) throws ClassNotFoundException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        Class.forName(driver);
        //Class.forName("org.postgresql.Driver");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void putConnection(Connection connection) throws SQLException {
        connection.close();
    }

}
