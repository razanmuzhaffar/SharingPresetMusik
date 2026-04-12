package com.sharingpresetmusik.storage;
import com.sharingpresetmusik.model.Preset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PresetRepository {

    private final DatabaseManager db;

    public PresetRepository(DatabaseManager db) {
        this.db = db;
    }

    private Preset map(ResultSet rs) throws SQLException {
        Preset p = new Preset();
        p.setId_preset(rs.getInt("id_preset"));
        p.setId_user(rs.getInt("id_user"));
        p.setPreset_name(rs.getString("preset_name"));
        p.setPreset_desc(rs.getString("preset_desc"));
        p.setPreset_download(rs.getInt("preset_download"));
        p.setSynth_model(rs.getString("synth_model"));
        p.setFile_url(rs.getString("file_url"));
        p.setCategory(rs.getString("category"));
        return p;
    }

    public Preset save(Preset preset) throws SQLException {
        String sql = "INSERT INTO preset (id_user, preset_name, preset_desc, preset_download, synth_model, file_url, category) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, preset.getId_user());
            ps.setString(2, preset.getPreset_name());
            ps.setString(3, preset.getPreset_desc());
            ps.setInt(4, preset.getPreset_download());
            ps.setString(5, preset.getSynth_model());
            ps.setString(6, preset.getFile_url());
            ps.setString(7, preset.getCategory());
            ps.executeUpdate();

            // Ambil id yang baru dibuat
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) preset.setId_preset(rs.getInt(1));
            }
        }
        return preset;
    }

    public Optional<Preset> findById(int id_preset) throws SQLException {
        String sql = "SELECT * FROM preset WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    public List<Preset> findAll() throws SQLException {
        List<Preset> list = new ArrayList<>();
        String sql = "SELECT * FROM preset ORDER BY id_preset DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Preset> findByIdUser(int id_user) throws SQLException {
        List<Preset> list = new ArrayList<>();
        String sql = "SELECT * FROM preset WHERE id_user = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Preset> findByCategory(String category) throws SQLException {
        List<Preset> list = new ArrayList<>();
        String sql = "SELECT * FROM preset WHERE LOWER(category) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Preset> searchByName(String keyword) throws SQLException {
        List<Preset> list = new ArrayList<>();
        String sql = "SELECT * FROM preset WHERE LOWER(preset_name) LIKE LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void incrementDownload(int id_preset) throws SQLException {
        String sql = "UPDATE preset SET preset_download = preset_download + 1 WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ps.executeUpdate();
        }
    }

    public void delete(int id_preset) throws SQLException {
        String sql = "DELETE FROM preset WHERE id_preset = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id_preset);
            ps.executeUpdate();
        }
    }

}