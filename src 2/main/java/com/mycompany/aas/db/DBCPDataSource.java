package com.mycompany.aas.db;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import org.apache.commons.dbcp2.BasicDataSource;

public final class DBCPDataSource {

    private static final BasicDataSource ds = new BasicDataSource();

    static {
        ds.setUrl(
                "jdbc:mysql://localhost:3306/kunst_adoptie"
                + "?useSSL=false"
                + "&serverTimezone=UTC"
                + "&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci"
        );
        ds.setUsername("dani");
        ds.setPassword("DS20ds14");

        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }

    private DBCPDataSource() {
        
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    
    public static void setActorEmail(Connection con, String email) throws SQLException {
        try (PreparedStatement ps
                = con.prepareStatement("SET @actor_email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }
}
