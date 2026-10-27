package com.mycompany.aas.dao.admin;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConditieDAO {

    public void create(String v) throws SQLException {
        String sql = "INSERT INTO Conditie (conditie) VALUES (?)";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            ps.executeUpdate();
        }
    }

    public List<String> listAll() throws SQLException {
        String sql = "SELECT conditie FROM Conditie ORDER BY conditie";
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
            throw new IllegalStateException("Kan conditie niet wijzigen: wordt gebruikt door Rapporten.");
        }
        if (exists(newV)) {
            throw new IllegalStateException("Nieuwe conditie bestaat al.");
        }
        String sql = "UPDATE Conditie SET conditie = ? WHERE conditie = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newV);
            ps.setString(2, oldV);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Conditie niet gevonden: " + oldV);
            }
        }
    }

    public void delete(String v) throws SQLException {
        if (isUsed(v)) {
            throw new IllegalStateException("Kan conditie niet verwijderen: wordt gebruikt door Rapporten.");
        }
        String sql = "DELETE FROM Conditie WHERE conditie = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Conditie niet gevonden: " + v);
            }
        }
    }

    private boolean exists(String v) throws SQLException {
        String sql = "SELECT 1 FROM Conditie WHERE conditie = ? LIMIT 1";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isUsed(String v) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Rapporten WHERE conditie = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
