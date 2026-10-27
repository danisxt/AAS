package com.mycompany.aas.dao;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolDAO {

    public boolean isVrijwilliger(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM Vrijwilligers WHERE email = ? LIMIT 1";
        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isMedewerker(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM Medewerkers WHERE email = ? LIMIT 1";
        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void setVrijwilliger(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        String sql = "INSERT INTO Vrijwilligers (email) VALUES (?)";
        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.executeUpdate();
        }
    }

    public void setMedewerker(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        String sql = "INSERT INTO Medewerkers (email) VALUES (?)";
        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.executeUpdate();
        }
    }

 //soft delete
    public void deactivate(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        String delV = "DELETE FROM Vrijwilligers WHERE email = ?";
        String delM = "DELETE FROM Medewerkers WHERE email = ?";

        try (Connection conn = DBCPDataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(delV)) {
                    ps.setString(1, email.trim());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(delM)) {
                    ps.setString(1, email.trim());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
}
