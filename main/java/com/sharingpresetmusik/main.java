package com.sharingpresetmusik;

import com.sharingpresetmusik.auth.AuthService;
import com.sharingpresetmusik.model.*;
import com.sharingpresetmusik.preset.PresetException;
import com.sharingpresetmusik.preset.PresetService;
import com.sharingpresetmusik.storage.*;

public class main {
    public static void main(String[] args) throws Exception {

        DatabaseManager db = new DatabaseManager("sharingpresetmusik.db");
        db.initSchema();

        try (java.sql.Connection conn = db.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM filestorageservice");
            stmt.execute("DELETE FROM presetmodel");
            stmt.execute("DELETE FROM preset");
            stmt.execute("DELETE FROM akun");
        }

        // Setup repository & service
        AkunRepository        akunRepo        = new AkunRepository(db);
        PresetRepository      presetRepo      = new PresetRepository(db);
        PresetModelRepository presetModelRepo = new PresetModelRepository(db);
        FileStorageRepository fileStorageRepo = new FileStorageRepository(db);

        AuthService   authService   = new AuthService(akunRepo);
        PresetService presetService = new PresetService(presetRepo, presetModelRepo, fileStorageRepo);

        // Buat akun dulu
        Akun akun = authService.register("djbeats", "dj@test.com", "Secure1Pass");
        System.out.println("✅ Akun dibuat: " + akun.getUsername());

        // ── Test uploadPreset ──────────────────────────────
        System.out.println("\n── Test Upload ──");
        Preset preset = new Preset();
        preset.setId_user(akun.getId_user());
        preset.setPreset_name("Dark Bass");
        preset.setCategory("Bass");
        preset.setSynth_model("Serum");
        preset.setPreset_desc("Bass yang gelap");
        preset.setFile_url("https://example.com/darkbass");

        presetService.uploadPreset(preset,
                "Serum VST",
                ".fxp",
                "https://cdn.example.com/darkbass.fxp");
        System.out.println("✅ Upload berhasil! id_preset: " + preset.getId_preset());

        // Cek 3 tabel terisi
        System.out.println("✅ presetmodel: " + presetService.getModelsForPreset(preset.getId_preset()).size() + " gear");
        System.out.println("✅ filestorageservice: " + presetService.getStorageForPreset(preset.getId_preset()).size() + " URL");

        // ── Tambah data dummy lebih banyak ─────────────────
        System.out.println("\n── Tambah Data Dummy ──");
        Akun akun2 = authService.register("guitarist", "guitar@test.com", "Pass1234");
        System.out.println("✅ Akun 2 dibuat: " + akun2.getUsername());

        Preset preset2 = new Preset();
        preset2.setId_user(akun2.getId_user());
        preset2.setPreset_name("Crunchy Guitar");
        preset2.setCategory("Guitar Tones");
        preset2.setSynth_model("Guitar Rig");
        preset2.setPreset_desc("Crunchy tone for rock");
        preset2.setFile_url("https://example.com/crunchy");
        presetService.uploadPreset(preset2, "Guitar Rig 6", ".grir", "https://cdn.example.com/crunchy.grir");
        System.out.println("✅ Preset 2 uploaded: " + preset2.getId_preset());

        Preset preset3 = new Preset();
        preset3.setId_user(akun.getId_user());
        preset3.setPreset_name("Plucky Synth");
        preset3.setCategory("Synthesizer");
        preset3.setSynth_model("Serum");
        preset3.setPreset_desc("Plucky lead synth");
        preset3.setFile_url("https://example.com/plucky");
        presetService.uploadPreset(preset3, "Serum Lead VST", ".fxp", "https://cdn.example.com/plucky.fxp");
        System.out.println("✅ Preset 3 uploaded: " + preset3.getId_preset());

        Preset preset4 = new Preset();
        preset4.setId_user(akun2.getId_user());
        preset4.setPreset_name("Deep Sub Bass");
        preset4.setCategory("Bass");
        preset4.setSynth_model("Massive");
        preset4.setPreset_desc("Deep sub for EDM");
        preset4.setFile_url("https://example.com/deepsub");
        presetService.uploadPreset(preset4, "Massive VST", ".nmsv", "https://cdn.example.com/deepsub.nmsv");
        System.out.println("✅ Preset 4 uploaded: " + preset4.getId_preset());

        // ── Test pencarian ─────────────────────────────────
        System.out.println("\n── Test Pencarian ──");
        System.out.println("✅ findAll: " + presetService.findAll().size() + " preset");
        System.out.println("✅ findByUser: " + presetService.findByUser(akun.getId_user()).size() + " preset");
        System.out.println("✅ findByCategory Bass: " + presetService.findByCategory("Bass").size() + " preset");
        System.out.println("✅ searchByName 'dark': " + presetService.searchByName("dark").size() + " preset");

        // ── Test download ──────────────────────────────────
        System.out.println("\n── Test Download ──");
        String url = presetService.download(preset.getId_preset());
        System.out.println("✅ Download URL: " + url);
        int downloadCount = presetRepo.findById(preset.getId_preset()).get().getPreset_download();
        System.out.println("✅ Download count: " + downloadCount);

        // ── Test delete ────────────────────────────────────
        System.out.println("\n── Test Delete ──");

        // Delete oleh bukan pemilik
        try {
            presetService.deletePreset(preset.getId_preset(), 999);
        } catch (PresetException e) {
            System.out.println("✅ Bukan pemilik tertangkap: " + e.getMessage());
        }

        // Delete oleh pemilik
        presetService.deletePreset(preset.getId_preset(), akun.getId_user());
        System.out.println("✅ Delete berhasil!");
        System.out.println("✅ findAll setelah delete: " + presetService.findAll().size() + " preset");
        System.out.println("✅ presetmodel sisa: " + presetService.getModelsForPreset(preset.getId_preset()).size());
        System.out.println("✅ filestorageservice sisa: " + presetService.getStorageForPreset(preset.getId_preset()).size());

        db.close();
        System.out.println("\n✅ PresetService selesai sempurna!");
    }
}