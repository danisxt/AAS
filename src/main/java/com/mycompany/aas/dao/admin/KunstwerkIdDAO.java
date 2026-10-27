package com.mycompany.aas.dao.admin;

import com.mycompany.aas.db.DBCPDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KunstwerkIdDAO {

    public List<Integer> listAll() throws SQLException {
        String sql = "SELECT kunstwerk_id FROM Kunstwerk_IDs ORDER BY kunstwerk_id";

        List<Integer> out = new ArrayList<>();

        try (Connection conn = DBCPDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(rs.getInt("kunstwerk_id"));
            }
        }

        return out;
    }
}
