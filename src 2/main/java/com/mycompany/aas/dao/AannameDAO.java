package com.mycompany.aas.dao;

import com.mycompany.aas.db.DBCPDataSource;
import com.mycompany.aas.model.Aanname;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AannameDAO {

//create aanname
    public void createAanname(String email,
            int kunstwerkId,
            LocalDateTime startdatum,
            LocalDateTime einddatum, // mag null
            Integer status, // mag null (0/1/2 of null)
            int aannametype) throws SQLException { // 0/1/2

        String sqlInsertKunstwerkId
                = "INSERT IGNORE INTO Kunstwerk_IDs (kunstwerk_id) VALUES (?)";

        String sqlInsertAanname
                = "INSERT INTO Aannames (email, kunstwerk_id, startdatum, einddatum, status, aannametype) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBCPDataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sqlInsertKunstwerkId)) {
                    ps1.setInt(1, kunstwerkId);
                    ps1.executeUpdate();
                }

                try (PreparedStatement ps2 = conn.prepareStatement(sqlInsertAanname)) {
                    ps2.setString(1, email);
                    ps2.setInt(2, kunstwerkId);
                    ps2.setTimestamp(3, Timestamp.valueOf(startdatum));

                    if (einddatum == null) {
                        ps2.setNull(4, Types.TIMESTAMP);
                    } else {
                        ps2.setTimestamp(4, Timestamp.valueOf(einddatum));
                    }

                    if (status == null) {
                        ps2.setNull(5, Types.TINYINT);
                    } else {
                        ps2.setInt(5, status);
                    }

                    ps2.setInt(6, aannametype);

                    ps2.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /**
     * Status wijzigen (alleen medewerker mag dit; trigger controleert
     * @actor_email)
     */
    public void updateStatus(Connection conn,
            String targetEmail,
            int kunstwerkId,
            LocalDateTime startdatum,
            Integer newStatus) throws SQLException {

        String sql
                = "UPDATE Aannames "
                + "SET status = ? "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (newStatus == null) {
                ps.setNull(1, Types.TINYINT);
            } else {
                ps.setInt(1, newStatus);
            }

            ps.setString(2, targetEmail);
            ps.setInt(3, kunstwerkId);
            ps.setTimestamp(4, Timestamp.valueOf(startdatum));

            ps.executeUpdate();
        }
    }

    /**
     * Convenience: status wijzigen mét actorEmail in één call.
     */
    public void updateStatusAsActor(String actorEmail,
            String targetEmail,
            int kunstwerkId,
            LocalDateTime startdatum,
            Integer newStatus) throws SQLException {

        try (Connection conn = DBCPDataSource.getConnection()) {
            DBCPDataSource.setActorEmail(conn, actorEmail);
            updateStatus(conn, targetEmail, kunstwerkId, startdatum, newStatus);
        }
    }

    /**
     * Einddatum zetten (bijv. adoptie beëindigen of wijzigen).
     */
    public void setEinddatum(String email, int kunstwerkId, LocalDateTime startdatum, LocalDateTime einddatum) throws SQLException {
        String sql
                = "UPDATE Aannames "
                + "SET einddatum = ? "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ?";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            if (einddatum == null) {
                ps.setNull(1, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(1, Timestamp.valueOf(einddatum));
            }

            ps.setString(2, email);
            ps.setInt(3, kunstwerkId);
            ps.setTimestamp(4, Timestamp.valueOf(startdatum));

            ps.executeUpdate();
        }
    }

    /**
     * Alle aannames van een gebruiker (handig voor Rapport-tab).
     */
    public List<Aanname> listAannamesByEmail(String email) throws SQLException {
        String sql
                = "SELECT email, kunstwerk_id, startdatum, einddatum, status, aannametype "
                + "FROM Aannames "
                + "WHERE email = ? "
                + "ORDER BY startdatum DESC";

        List<Aanname> out = new ArrayList<>();

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapAanname(rs));
                }
            }
        }

        return out;
    }

    /**
     * Alle aannames op kunstwerk (handig voor beheer/status-overzicht).
     */
    public List<Aanname> listAannamesByKunstwerk(int kunstwerkId) throws SQLException {
        String sql
                = "SELECT email, kunstwerk_id, startdatum, einddatum, status, aannametype "
                + "FROM Aannames "
                + "WHERE kunstwerk_id = ? "
                + "ORDER BY startdatum DESC";

        List<Aanname> out = new ArrayList<>();

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kunstwerkId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapAanname(rs));
                }
            }
        }

        return out;
    }

   //bestaat de aanname?
    public boolean existsAanname(String email, int kunstwerkId, LocalDateTime startdatum) throws SQLException {
        String sql
                = "SELECT 1 FROM Aannames "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? "
                + "LIMIT 1";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);
            ps.setTimestamp(3, Timestamp.valueOf(startdatum));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Laatste (meest recente) aanname van een user voor een kunstwerk. Geeft
     * null als er geen is.
     */
    public Aanname findLatestAanname(String email, int kunstwerkId) throws SQLException {
        String sql
                = "SELECT email, kunstwerk_id, startdatum, einddatum, status, aannametype "
                + "FROM Aannames "
                + "WHERE email = ? AND kunstwerk_id = ? "
                + "ORDER BY startdatum DESC "
                + "LIMIT 1";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapAanname(rs);
            }
        }
    }

    private Aanname mapAanname(ResultSet rs) throws SQLException {
        String email = rs.getString("email");
        int kunstwerkId = rs.getInt("kunstwerk_id");
        LocalDateTime start = rs.getTimestamp("startdatum").toLocalDateTime();

        Timestamp eindTs = rs.getTimestamp("einddatum");
        LocalDateTime eind = (eindTs == null) ? null : eindTs.toLocalDateTime();

        Object statusObj = rs.getObject("status");
        Integer status = (statusObj == null) ? null : ((Number) statusObj).intValue();

        int type = rs.getInt("aannametype");

        return new Aanname(email, kunstwerkId, start, eind, status, type);
    }

//annuleren als er geen rapporten bestaan
    public boolean hasRapportenForAanname(String email, int kunstwerkId, LocalDateTime startdatum) throws SQLException {
        String sql
                = "SELECT 1 FROM Rapporten "
                + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? "
                + "LIMIT 1";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, kunstwerkId);
            ps.setTimestamp(3, Timestamp.valueOf(startdatum));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

//verwijdert aanname als er geen rapporten bestaan
    public void deleteAannameIfNoRapporten(String email, int kunstwerkId, LocalDateTime startdatum) throws SQLException {
        try (Connection conn = DBCPDataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // check rapporten op dezelfde connection 
                String chk
                        = "SELECT 1 FROM Rapporten "
                        + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ? "
                        + "LIMIT 1";

                try (PreparedStatement ps = conn.prepareStatement(chk)) {
                    ps.setString(1, email);
                    ps.setInt(2, kunstwerkId);
                    ps.setTimestamp(3, Timestamp.valueOf(startdatum));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalStateException("Kan niet annuleren: er bestaan al rapporten voor deze adoptie.");
                        }
                    }
                }

                String del
                        = "DELETE FROM Aannames "
                        + "WHERE email = ? AND kunstwerk_id = ? AND startdatum = ?";

                try (PreparedStatement ps = conn.prepareStatement(del)) {
                    ps.setString(1, email);
                    ps.setInt(2, kunstwerkId);
                    ps.setTimestamp(3, Timestamp.valueOf(startdatum));
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (RuntimeException ex) {
                conn.rollback();
                throw ex;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
}
