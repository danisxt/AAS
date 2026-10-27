package com.mycompany.aas.dao;

import com.mycompany.aas.db.DBCPDataSource;
import com.mycompany.aas.model.Deelnemer;
import com.mycompany.aas.model.Medewerker;
import com.mycompany.aas.model.Vrijwilliger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GebruikerDAO {

//create profiel
    public void createGebruiker(String email, String voornaam, String achternaam) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        if (voornaam == null || voornaam.trim().isEmpty()) {
            throw new IllegalArgumentException("Voornaam is verplicht.");
        }
        if (achternaam == null || achternaam.trim().isEmpty()) {
            throw new IllegalArgumentException("Achternaam is verplicht.");
        }

        email = email.trim();
        voornaam = voornaam.trim();
        achternaam = achternaam.trim();

        String insEmail = "INSERT IGNORE INTO Emailadressen (email) VALUES (?)";
        String insVoornaam = "INSERT IGNORE INTO Voornamen (voornaam) VALUES (?)";
        String insAchternaam = "INSERT IGNORE INTO Achternamen (achternaam) VALUES (?)";
        String insGebruiker = "INSERT INTO Gebruikers (email, voornaam, achternaam) VALUES (?, ?, ?)";

        try (Connection conn = DBCPDataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insEmail)) {
                    ps.setString(1, email);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insVoornaam)) {
                    ps.setString(1, voornaam);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insAchternaam)) {
                    ps.setString(1, achternaam);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insGebruiker)) {
                    ps.setString(1, email);
                    ps.setString(2, voornaam);
                    ps.setString(3, achternaam);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

//read profiel en polymorf mapping
    public Deelnemer findByEmail(String email) throws SQLException {
        return findByEmail(email, false);
    }

// read. vrijwilliger -> return vrijwilliger, medewerker -> return medewerker. niks? return null
    public Deelnemer findByEmail(String email, boolean includeInactive) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        email = email.trim();

        String sqlUser = "SELECT email, voornaam, achternaam FROM Gebruikers WHERE email = ?";

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlUser)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String em = rs.getString("email");
                String vn = rs.getString("voornaam");
                String an = rs.getString("achternaam");
// magic gebeurd hier>
                boolean isV = existsIn(conn, "Vrijwilligers", em);
                boolean isM = existsIn(conn, "Medewerkers", em);

                if (isV) {
                    return new Vrijwilliger(em, vn, an, true);
                }
                if (isM) {
                    return new Medewerker(em, vn, an, true);
                }

                if (includeInactive) {
                    // "drager" object met actief=false
                    return new Vrijwilliger(em, vn, an, false);
                }
                return null;
            }
        }
    }

    // update profiel
  
    public void updateNaam(String email, String newVoornaam, String newAchternaam) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        if (newVoornaam == null || newVoornaam.trim().isEmpty()) {
            throw new IllegalArgumentException("Voornaam is verplicht.");
        }
        if (newAchternaam == null || newAchternaam.trim().isEmpty()) {
            throw new IllegalArgumentException("Achternaam is verplicht.");
        }

        email = email.trim();
        newVoornaam = newVoornaam.trim();
        newAchternaam = newAchternaam.trim();

        String insVoornaam = "INSERT IGNORE INTO Voornamen (voornaam) VALUES (?)";
        String insAchternaam = "INSERT IGNORE INTO Achternamen (achternaam) VALUES (?)";
        String upd = "UPDATE Gebruikers SET voornaam = ?, achternaam = ? WHERE email = ?";

        try (Connection conn = DBCPDataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insVoornaam)) {
                    ps.setString(1, newVoornaam);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insAchternaam)) {
                    ps.setString(1, newAchternaam);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(upd)) {
                    ps.setString(1, newVoornaam);
                    ps.setString(2, newAchternaam);
                    ps.setString(3, email);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Gebruiker niet gevonden: " + email);
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

//private helper
    private boolean existsIn(Connection conn, String table, String email) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
