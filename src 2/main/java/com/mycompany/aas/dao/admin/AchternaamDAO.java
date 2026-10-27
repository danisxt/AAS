package com.mycompany.aas.dao.admin;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchternaamDAO {

    public void create(String v) throws SQLException {
        String sql = "INSERT INTO Achternamen (achternaam) VALUES (?)";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            ps.executeUpdate();
        }
    }

    public List<String> listAll() throws SQLException {
        String sql = "SELECT achternaam FROM Achternamen ORDER BY achternaam";
        List<String> out = new ArrayList<>();
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        }
        return out;
    }

    public void update(String oldV, String newV) throws SQLException {
        if (isUsed(oldV)) {
            throw new IllegalStateException("Kan achternaam niet wijzigen: wordt gebruikt door Gebruikers.");
        }
        if (exists(newV)) {
            throw new IllegalStateException("Nieuwe achternaam bestaat al.");
        }
        String sql = "UPDATE Achternamen SET achternaam = ? WHERE achternaam = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newV);
            ps.setString(2, oldV);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Achternaam niet gevonden: " + oldV);
            }
        }
    }

    public void delete(String v) throws SQLException {
        if (isUsed(v)) {
            throw new IllegalStateException("Kan achternaam niet verwijderen: wordt gebruikt door Gebruikers.");
        }
        String sql = "DELETE FROM Achternamen WHERE achternaam = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Achternaam niet gevonden: " + v);
            }
        }
    }

    private boolean exists(String v) throws SQLException {
        String sql = "SELECT 1 FROM Achternamen WHERE achternaam = ? LIMIT 1";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isUsed(String v) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Gebruikers WHERE achternaam = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
