package com.mycompany.aas.dao.admin;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmailadresDAO {

    public void create(String email) throws SQLException {
        String sql = "INSERT INTO Emailadressen (email) VALUES (?)";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    public List<String> listAll() throws SQLException {
        String sql = "SELECT email FROM Emailadressen ORDER BY email";
        List<String> out = new ArrayList<>();
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        }
        return out;
    }

    // Update/Delete alleen als niet gebruikt in Gebruikers
    public void update(String oldEmail, String newEmail) throws SQLException {
        if (isUsed(oldEmail)) {
            throw new IllegalStateException("Kan email niet wijzigen: wordt gebruikt door Gebruikers.");
        }
        if (exists(newEmail)) {
            throw new IllegalStateException("Nieuwe email bestaat al.");
        }
        String sql = "UPDATE Emailadressen SET email = ? WHERE email = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setString(2, oldEmail);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Email niet gevonden: " + oldEmail);
            }
        }
    }

    public void delete(String email) throws SQLException {
        if (isUsed(email)) {
            throw new IllegalStateException("Kan email niet verwijderen: wordt gebruikt door Gebruikers.");
        }
        String sql = "DELETE FROM Emailadressen WHERE email = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Email niet gevonden: " + email);
            }
        }
    }

    private boolean exists(String email) throws SQLException {
        String sql = "SELECT 1 FROM Emailadressen WHERE email = ? LIMIT 1";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isUsed(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Gebruikers WHERE email = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
