package com.sharingpresetmusik.storage;

import com.sharingpresetmusik.model.FileStorage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileStorageRepository {

    private final DatabaseManager db;

    public FileStorageRepository(DatabaseManager db) {
        this.db = db;
    }

    private FileStorage map(ResultSet rs) throws SQLException {
        FileStorage fs = new FileStorage();
        fs.setBase_url(rs.getString("base_url"));
        fs.setId_preset(rs.getInt("id_preset"));
        return fs;
    }

    public FileStorage save(FileStorage fs) throws SQLException {
        String sql = "INSERT INTO filestorageservice (base_url, id_preset) VALUES (?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fs.getBase_url());
            ps.setInt(2, fs.getId_preset());
            ps.executeUpdate();
        }
        return fs;
    }

    public Optional<FileStorage> findByBaseUrl(String base_url) throws SQLException {
        String sql = "SELECT * FROM filestorageservice WHERE base_url = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, base_url);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public List<FileStorage> findByIdPreset(int id_preset) throws SQLException {
        List<FileStorage> list = new ArrayList<>();
        String sql = "SELECT * FROM filestorageservice WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void delete(String base_url) throws SQLException {
        String sql = "DELETE FROM filestorageservice WHERE base_url = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, base_url);
            ps.executeUpdate();
        }
    }

    public void deleteByIdPreset(int id_preset) throws SQLException {
        String sql = "DELETE FROM filestorageservice WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ps.executeUpdate();
        }
    }

}