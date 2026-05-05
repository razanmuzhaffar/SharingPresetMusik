package com.sharingpresetmusik.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharingpresetmusik.model.Akun;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.Map;

public class AkunService {

    private static final String BASE_URL  = "http://127.0.0.1:8000/api";
    private static final Path   TOKEN_FILE = Path.of(
            System.getProperty("user.home"), ".sharingpresetmusik", "session.json"
    );

    private final HttpClient   http;
    private final ObjectMapper mapper;

    private String token;
    private Akun   akun;

    public AkunService() {
        this.http   = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        loadSession();
    }

    // ── Register ────────────────────────────────────────────────────────────────

    public Akun register(String name, String username, String email, String password) throws Exception {
        if (username == null || username.isBlank())
            throw new AuthException("Username tidak boleh kosong.");
        if (email == null || email.isBlank())
            throw new AuthException("Email tidak boleh kosong.");
        if (password == null || password.length() < 6)
            throw new AuthException("Password minimal 6 karakter.");

        String body = mapper.writeValueAsString(Map.of(
                "name",     name,
                "username", username,
                "email",    email,
                "password", password
        ));

        HttpResponse<String> res = sendPost("/register", body, false);
        handleError(res);

        Map<?, ?> json = mapper.readValue(res.body(), Map.class);
        return parseAndSaveSession(json);
    }

    // ── Login ────────────────────────────────────────────────────────────────────

    public Akun login(String email, String password) throws Exception {
        if (email == null || email.isBlank())
            throw new AuthException("Email tidak boleh kosong.");
        if (password == null || password.isBlank())
            throw new AuthException("Password tidak boleh kosong.");

        String body = mapper.writeValueAsString(Map.of(
                "email",    email,
                "password", password
        ));

        HttpResponse<String> res = sendPost("/login", body, false);
        handleError(res);

        Map<?, ?> json = mapper.readValue(res.body(), Map.class);
        return parseAndSaveSession(json);
    }

    // ── Logout ───────────────────────────────────────────────────────────────────

    public void logout() throws Exception {
        if (token != null) {
            sendPost("/logout", "{}", true);
        }
        token = null;
        akun  = null;
        Files.deleteIfExists(TOKEN_FILE);
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public String getToken() { return token; }
    public Akun   getAkun()  { return akun;  }
    public boolean isLoggedIn() { return token != null && akun != null; }

    // ── Internal ─────────────────────────────────────────────────────────────────

    private HttpResponse<String> sendPost(String endpoint, String body, boolean withAuth)
            throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (withAuth && token != null)
            builder.header("Authorization", "Bearer " + token);

        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Akun parseAndSaveSession(Map<?, ?> json) throws Exception {
        token = (String) json.get("token");

        Map<?, ?> userData = (Map<?, ?>) json.get("user");
        akun = new Akun(
                (String) userData.get("username"),
                "",
                (String) userData.get("email"),
                (Integer) userData.get("id")
        );

        saveSession();
        return akun;
    }

    private void saveSession() throws Exception {
        Files.createDirectories(TOKEN_FILE.getParent());
        Map<String, Object> session = Map.of(
                "token",    token,
                "id",       akun.getId_user(),
                "username", akun.getUsername(),
                "email",    akun.getEmail()
        );
        mapper.writeValue(TOKEN_FILE.toFile(), session);
    }

    private void loadSession() {
        try {
            if (!Files.exists(TOKEN_FILE)) return;
            Map<?, ?> session = mapper.readValue(TOKEN_FILE.toFile(), Map.class);
            token = (String) session.get("token");
            akun  = new Akun(
                    (String) session.get("username"),
                    "",
                    (String) session.get("email"),
                    (Integer) session.get("id")
            );
        } catch (Exception e) {
            token = null;
            akun  = null;
        }
    }

    private void handleError(HttpResponse<String> res) throws Exception {
        if (res.statusCode() >= 400) {
            Map<String, Object> json = mapper.readValue(res.body(), Map.class);
            String msg = (String) json.getOrDefault("message", "Terjadi kesalahan.");
            throw new AuthException(msg);
        }
    }
}