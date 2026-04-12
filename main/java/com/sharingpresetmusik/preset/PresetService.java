package com.sharingpresetmusik.preset;

import com.sharingpresetmusik.model.*;
import com.sharingpresetmusik.storage.*;
import java.sql.SQLException;
import java.util.List;

public class PresetService {

    private final PresetRepository      presetRepo;
    private final PresetModelRepository presetModelRepo;
    private final FileStorageRepository fileStorageRepo;

    public PresetService(PresetRepository presetRepo,PresetModelRepository presetModelRepo, FileStorageRepository fileStorageRepo) {
        this.presetRepo      = presetRepo;
        this.presetModelRepo = presetModelRepo;
        this.fileStorageRepo = fileStorageRepo;
    }

    public Preset uploadPreset(Preset preset,
                               String gearName,
                               String fileExtension,
                               String baseUrl) throws SQLException {

        // Validasi input
        if (preset.getPreset_name() == null || preset.getPreset_name().isBlank())
            throw new PresetException("Nama preset tidak boleh kosong.");
        if (preset.getId_user() <= 0)
            throw new PresetException("id_user tidak valid.");
        if (gearName == null || gearName.isBlank())
            throw new PresetException("Gear name tidak boleh kosong.");
        if (baseUrl == null || baseUrl.isBlank())
            throw new PresetException("URL tidak boleh kosong.");

        // 1. Simpan ke tabel preset
        presetRepo.save(preset);

        // 2. Simpan ke tabel presetmodel (FK: id_preset)
        PresetModel pm = new PresetModel();
        pm.setGear_name(gearName);
        pm.setId_preset(preset.getId_preset());
        pm.setFile_extension(fileExtension);
        presetModelRepo.save(pm);

        // 3. Simpan ke tabel filestorageservice (FK: id_preset)
        FileStorage fs = new FileStorage();
        fs.setBase_url(baseUrl);
        fs.setId_preset(preset.getId_preset());
        fileStorageRepo.save(fs);

        return preset;
    }

    public String download(int id_preset) throws SQLException {
        // Cek preset ada
        Preset preset = presetRepo.findById(id_preset)
                .orElseThrow(() -> new PresetException("Preset tidak ditemukan."));

        // Cek URL tersedia
        List<FileStorage> storageList = fileStorageRepo.findByIdPreset(id_preset);
        if (storageList.isEmpty())
            throw new PresetException("File preset belum tersedia.");
        // Increment download count
        presetRepo.incrementDownload(id_preset);

        return storageList.get(0).getBase_url();
    }

    public void deletePreset(int id_preset, int requestingUserId) throws SQLException {

        // Cek preset ada dan cek kepemilikan
        Preset preset = presetRepo.findById(id_preset)
                .orElseThrow(() -> new PresetException("Preset tidak ditemukan."));
        if (preset.getId_user() != requestingUserId)
            throw new PresetException("Anda tidak berhak menghapus preset ini.");

        // Hapus dari 3 tabel — urutan dari bawah ke atas
        fileStorageRepo.deleteByIdPreset(id_preset);
        presetModelRepo.deleteByIdPreset(id_preset);
        presetRepo.delete(id_preset);
    }

    public List<Preset> findAll() throws SQLException {
        return presetRepo.findAll();
    }

    public List<Preset> findByUser(int id_user) throws SQLException {
        return presetRepo.findByIdUser(id_user);
    }

    public List<Preset> findByCategory(String category) throws SQLException {
        return presetRepo.findByCategory(category);
    }

    public List<Preset> searchByName(String keyword) throws SQLException {
        return presetRepo.searchByName(keyword);
    }

    public List<PresetModel> getModelsForPreset(int id_preset) throws SQLException {
        return presetModelRepo.findByIdPreset(id_preset);
    }

    public List<FileStorage> getStorageForPreset(int id_preset) throws SQLException {
        return fileStorageRepo.findByIdPreset(id_preset);
    }
}