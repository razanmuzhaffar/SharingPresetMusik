package com.sharingpresetmusik.storage;

import com.sharingpresetmusik.model.Akun;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AkunRepository {

    private final DatabaseManager db;

    public AkunRepository(DatabaseManager db) {
        this.db = db;
    }

    public Akun save(Akun akun) throws SQLException {
        String sql = "INSERT INTO akun (username, password, email) VALUES (?, ?, ?)";
        try (Connection kon = db.getConnection();
             PreparedStatement ps = kon.prepareStatement(sql)) {
            ps.setString(1, akun.getUsername());
            ps.setString(2, akun.getPassword());
            ps.setString(3, akun.getEmail());
            ps.executeUpdate();

            // Cara SQLite untuk ambil id terakhir
            try (Statement stmt = kon.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) akun.setId_user(rs.getInt(1));
            }
        }
        return akun;
    }

    public Optional<Akun> findById(int id_user) throws SQLException {
        String sql = "SELECT * FROM akun WHERE id_user = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public Optional<Akun> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM akun WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public Optional<Akun> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM akun WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }


    private Akun map(ResultSet rs) throws SQLException {
        Akun akun = new Akun();
        akun.setId_user(rs.getInt("id_user"));
        akun.setUsername(rs.getString("username"));
        akun.setPassword(rs.getString("password"));
        akun.setEmail(rs.getString("email"));
        return akun;
    }

    public boolean existsByUsername(String username) throws SQLException {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    public void delete(int id_user) throws SQLException {
        String sql = "DELETE FROM akun WHERE id_user = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_user);
            ps.executeUpdate();
        }
    }

    public List<Akun> findAll() throws SQLException {
        List<Akun> list = new ArrayList<>();
        String sql = "SELECT * FROM akun";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
}