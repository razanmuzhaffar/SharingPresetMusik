package com.sharingpresetmusik.preset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharingpresetmusik.model.Preset;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class PresetService {

    private static final String BASE_URL = "http://127.0.0.1:8000/api";

    private final HttpClient   http;
    private final ObjectMapper mapper;

    public PresetService() {
        this.http   = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // ── Public (tanpa token) ─────────────────────────────────────────────────────

    public List<Preset> findAll() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        handleError(res);

        return mapper.readValue(res.body(),
                mapper.getTypeFactory().constructCollectionType(List.class, Preset.class));
    }

    public Preset findById(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        handleError(res);

        return mapper.readValue(res.body(), Preset.class);
    }

    // ── Protected (butuh token) ──────────────────────────────────────────────────

    public Preset uploadPreset(Preset preset, Path filePath, Path demoPath, String token) throws Exception {
        String boundary = "----JavaBoundary" + System.currentTimeMillis();
        byte[] body = buildMultipart(boundary, preset, filePath, demoPath);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        handleError(res);

        return mapper.readValue(res.body(), Preset.class);
    }

    public void deletePreset(int id, String token) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets/" + id))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        handleError(res);
    }

    public Path downloadPreset(int id, Path saveDir) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets/" + id + "/download"))
                .GET()
                .build();

        HttpResponse<Path> res = http.send(req,
                HttpResponse.BodyHandlers.ofFileDownload(saveDir,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE));

        if (res.statusCode() >= 400)
            throw new PresetException("Gagal download preset.");

        return res.body();
    }

    // ── findByUser — filter manual sebagai fallback kalau backend belum handle ──

    public List<Preset> findByUser(int userId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/presets?user_id=" + userId))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        handleError(res);

        List<Preset> all = mapper.readValue(res.body(),
                mapper.getTypeFactory().constructCollectionType(List.class, Preset.class));

        // Filter manual — jaga-jaga kalau backend abaikan query param user_id
        return all.stream()
                .filter(p -> p.getId_user() == userId)
                .toList();
    }

    // ── Multipart builder ────────────────────────────────────────────────────────

    private byte[] buildMultipart(String boundary, Preset preset, Path filePath, Path demoPath) throws Exception {
        var out = new java.io.ByteArrayOutputStream();

        // Helper untuk nulis string ke stream
        java.util.function.Consumer<String> write = s -> {
            try {
                out.write(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        String CRLF = "\r\n";
        String dash = "--";

        // Field-field teks — urutan pakai LinkedHashMap supaya deterministik
        Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("preset_name", preset.getPreset_name() != null ? preset.getPreset_name() : "");
        fields.put("preset_desc", preset.getPreset_desc() != null ? preset.getPreset_desc() : "");
        fields.put("synth_model", preset.getSynth_model() != null ? preset.getSynth_model() : "");
        fields.put("category",    preset.getCategory()    != null ? preset.getCategory()    : "");
        fields.put("genre",       preset.getGenre()       != null ? preset.getGenre()       : "");

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            write.accept(dash + boundary + CRLF);
            write.accept("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF + CRLF);
            write.accept(entry.getValue() + CRLF);
        }

        // File preset (wajib)
        writeFilePart(out, boundary, "file", filePath);

        // File demo audio (opsional)
        if (demoPath != null) {
            writeFilePart(out, boundary, "demo_file", demoPath);
        }

        // Closing boundary
        out.write((CRLF + dash + boundary + dash + CRLF)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return out.toByteArray();
    }

    private void writeFilePart(java.io.ByteArrayOutputStream out,
                               String boundary, String fieldName, Path path) throws Exception {
        String CRLF = "\r\n";
        String header = "--" + boundary + CRLF
                + "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + path.getFileName() + "\"" + CRLF
                + "Content-Type: application/octet-stream" + CRLF + CRLF;

        out.write(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.write(Files.readAllBytes(path));
        out.write(CRLF.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // ── Error handler ────────────────────────────────────────────────────────────

    private void handleError(HttpResponse<String> res) throws Exception {
        if (res.statusCode() >= 400) {
            Map<String, Object> json = mapper.readValue(res.body(), Map.class);
            String msg = (String) json.getOrDefault("message", "Terjadi kesalahan.");
            throw new PresetException(msg);
        }
    }
}