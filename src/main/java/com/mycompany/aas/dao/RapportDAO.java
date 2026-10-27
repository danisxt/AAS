package com.mycompany.aas.dao;

import com.mycompany.aas.db.DBCPDataSource;
import com.mycompany.aas.model.Rapport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RapportDAO {

    public void createRapport(String email, int kunstwerkId, LocalDateTime startdatum,
            LocalDateTime aangemaaktOp, String conditie) throws SQLException {

        String sql
                = "INSERT INTO Rapporten (email, kunstwerk_id, startdatum, aangemaakt_op, conditie) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);
            ps.setTimestamp(3, Timestamp.valueOf(startdatum));
            ps.setTimestamp(4, Timestamp.valueOf(aangemaaktOp));
            ps.setString(5, conditie);

            ps.executeUpdate();
        }
    }

    public List<Rapport> listRapporten(String email, int kunstwerkId, LocalDateTime startdatum) throws SQLException {
        String sql
                = "SELECT email, kunstwerk_id, startdatum, aangemaakt_op, conditie "
                + "FROM Rapporten "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? "
                + "ORDER BY aangemaakt_op DESC";

        List<Rapport> out = new ArrayList<>();

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);
            ps.setTimestamp(3, Timestamp.valueOf(startdatum));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Rapport(
                            rs.getString("email"),
                            rs.getInt("kunstwerk_id"),
                            rs.getTimestamp("startdatum").toLocalDateTime(),
                            rs.getTimestamp("aangemaakt_op").toLocalDateTime(),
                            rs.getString("conditie")
                    ));
                }
            }
        }

        return out;
    }

    public List<String> listConditieOpties() throws SQLException {
        String sql = "SELECT conditie FROM Conditie ORDER BY conditie";
        List<String> out = new ArrayList<>();

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(rs.getString("conditie"));
            }
        }

        return out;
    }

//update alleen de conditie
    public void updateRapportConditie(String email, int kunstwerkId, LocalDateTime startdatum,
            LocalDateTime aangemaaktOp, String newConditie) throws SQLException {
        String sql
                = "UPDATE Rapporten "
                + "SET conditie = ? "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? AND aangemaakt_op = ?";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newConditie);
            ps.setString(2, email);
            ps.setInt(3, kunstwerkId);
            ps.setTimestamp(4, Timestamp.valueOf(startdatum));
            ps.setTimestamp(5, Timestamp.valueOf(aangemaaktOp));

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Rapport niet gevonden (mogelijk al verwijderd).");
            }
        }
    }

//verwijdert bestaand rapport op basis van volledige pk
    public void deleteRapport(String email, int kunstwerkId, LocalDateTime startdatum,
            LocalDateTime aangemaaktOp) throws SQLException {
        String sql
                = "DELETE FROM Rapporten "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? AND aangemaakt_op = ?";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);
            ps.setTimestamp(3, Timestamp.valueOf(startdatum));
            ps.setTimestamp(4, Timestamp.valueOf(aangemaaktOp));

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Rapport niet gevonden (mogelijk al verwijderd).");
            }
        }
    }
}
