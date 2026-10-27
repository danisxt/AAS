package com.mycompany.aas.dao.admin;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AannameTypeDAO {

    public void create(int v) throws SQLException {
        String sql = "INSERT INTO AannameTypes (aannametype) VALUES (?)";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, v);
            ps.executeUpdate();
        }
    }

    public List<Integer> listAll() throws SQLException {
        String sql = "SELECT aannametype FROM AannameTypes ORDER BY aannametype";
        List<Integer> out = new ArrayList<>();
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getInt(1));
            }
        }
        return out;
    }

    public void update(int oldV, int newV) throws SQLException {
        if (isUsed(oldV)) {
            throw new IllegalStateException("Kan aannametype niet wijzigen: wordt gebruikt door Aannames.");
        }
        if (exists(newV)) {
            throw new IllegalStateException("Nieuwe aannametype bestaat al.");
        }
        String sql = "UPDATE AannameTypes SET aannametype = ? WHERE aannametype = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newV);
            ps.setInt(2, oldV);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aannametype niet gevonden: " + oldV);
            }
        }
    }

    public void delete(int v) throws SQLException {
        if (isUsed(v)) {
            throw new IllegalStateException("Kan aannametype niet verwijderen: wordt gebruikt door Aannames.");
        }
        String sql = "DELETE FROM AannameTypes WHERE aannametype = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, v);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Aannametype niet gevonden: " + v);
            }
        }
    }

    private boolean exists(int v) throws SQLException {
        String sql = "SELECT 1 FROM AannameTypes WHERE aannametype = ? LIMIT 1";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isUsed(int v) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Aannames WHERE aannametype = ?";
        try (Connection c = DBCPDataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, v);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
