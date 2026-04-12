package com.sharingpresetmusik.storage;
import com.sharingpresetmusik.model.PresetModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.ResultSet;

public class PresetModelRepository {

    private final DatabaseManager db;

    public PresetModelRepository(DatabaseManager db) {
        this.db = db;
    }

    private PresetModel map(ResultSet rs) throws SQLException {
        PresetModel pm = new PresetModel();
        pm.setGear_name(rs.getString("gear_name"));
        pm.setId_preset(rs.getInt("id_preset"));
        pm.setFile_extension(rs.getString("file_extension"));
        return pm;
    }

    public PresetModel save(PresetModel pm) throws SQLException {
        String sql = "INSERT INTO presetmodel (gear_name, id_preset, file_extension) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pm.getGear_name());
            ps.setInt(2, pm.getId_preset());
            ps.setString(3, pm.getFile_extension());
            ps.executeUpdate();
        }
        return pm;
    }

    public Optional<PresetModel> findByGearName(String gear_name) throws SQLException {
        String sql = "SELECT * FROM presetmodel WHERE gear_name = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gear_name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public List<PresetModel> findByIdPreset(int id_preset) throws SQLException {
        List<PresetModel> list = new ArrayList<>();
        String sql = "SELECT * FROM presetmodel WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<PresetModel> findAll() throws SQLException {
        List<PresetModel> list = new ArrayList<>();
        String sql = "SELECT * FROM presetmodel";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void delete(String gear_name) throws SQLException {
        String sql = "DELETE FROM presetmodel WHERE gear_name = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gear_name);
            ps.executeUpdate();
        }
    }

    public void deleteByIdPreset(int id_preset) throws SQLException {
        String sql = "DELETE FROM presetmodel WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ps.executeUpdate();
        }
    }

}