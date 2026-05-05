package com.sharingpresetmusik.storage;

import java.sql.*;

public class DatabaseManager {

    private final String dbUrl;
    private Connection connection;

    public DatabaseManager(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite driver tidak ditemukan.", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Tabel akun
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS akun (
                    id_user  INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email    VARCHAR(150) NOT NULL UNIQUE
                )
            """);

            // Tabel preset
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS preset (
                    id_preset      INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_user        INTEGER NOT NULL,
                    preset_name    STRING  NOT NULL,
                    preset_desc    STRING,
                    preset_download INTEGER DEFAULT 0,
                    synth_model    STRING,
                    file_url       STRING,
                    category       STRING,
                    FOREIGN KEY (id_user) REFERENCES akun(id_user)
                )
            """);

            // Tabel presetmodel
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS presetmodel (
            id_presetmodel INTEGER PRIMARY KEY AUTOINCREMENT,
            gear_name      STRING  NOT NULL,
            id_preset      INTEGER NOT NULL,
            file_extension STRING,
            FOREIGN KEY (id_preset) REFERENCES preset(id_preset)
            )
            """);

            // Tabel filestorageservice
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS filestorageservice (
                    base_url  STRING  PRIMARY KEY,
                    id_preset INTEGER NOT NULL,
                    FOREIGN KEY (id_preset) REFERENCES preset(id_preset)
                )
            """);

            System.out.println("Semua tabel berhasil dibuat!");

        } catch (SQLException e) {
            throw new RuntimeException("Gagal membuat tabel.", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}