package com.sharingpresetmusik.auth;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sharingpresetmusik.model.Akun;
import com.sharingpresetmusik.storage.AkunRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public class AuthService {

    private static final int    BCRYPT_COST = 12;
    private static final long   TOKEN_EXP   = 7L * 24 * 60 * 60 * 1000;
    private static final String SECRET      = "SharingPresetMusik-SecretKey-2024!!";

    private final AkunRepository akunRepo;
    private final Key            signingKey;

    public AuthService(AkunRepository akunRepo) {
        this.akunRepo   = akunRepo;
        this.signingKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public Akun register(String username, String email, String password) throws SQLException {

        // Validasi username
        if (username == null || username.isBlank())
            throw new AuthException("Username tidak boleh kosong.");
        if (username.length() < 3 || username.length() > 50)
            throw new AuthException("Username harus 3-50 karakter.");
        if (!username.matches("^[a-zA-Z0-9_.\\-]+$"))
            throw new AuthException("Username hanya boleh huruf, angka, '_', '-', '.'");

        // Validasi email
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new AuthException("Format email tidak valid.");

        // Validasi password
        if (password == null || password.length() < 8)
            throw new AuthException("Password minimal 8 karakter.");
        if (!password.matches(".*[A-Z].*"))
            throw new AuthException("Password harus ada huruf besar.");
        if (!password.matches(".*[0-9].*"))
            throw new AuthException("Password harus ada angka.");

        // Cek duplikat
        if (akunRepo.existsByUsername(username))
            throw new AuthException("Username '" + username + "' sudah dipakai.");
        if (akunRepo.existsByEmail(email))
            throw new AuthException("Email '" + email + "' sudah terdaftar.");

        // Hash password & simpan
        String hashed = BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
        Akun akun = new Akun();
        akun.setUsername(username);
        akun.setEmail(email);
        akun.setPassword(hashed);
        akunRepo.save(akun);

        return akun;
    }

    public String login(String usernameOrEmail, String password) throws SQLException {

        // Cari akun berdasarkan username atau email
        Optional<Akun> opt = akunRepo.findByUsername(usernameOrEmail);
        if (opt.isEmpty()) {
            opt = akunRepo.findByEmail(usernameOrEmail);
        }

        // Kalau tidak ketemu
        Akun akun = opt.orElseThrow(() ->
                new AuthException("Username/email atau password salah."));

        // Verifikasi password
        BCrypt.Result result = BCrypt.verifyer()
                .verify(password.toCharArray(), akun.getPassword());
        if (!result.verified) {
            throw new AuthException("Username/email atau password salah.");
        }

        // Generate JWT token
        String token = Jwts.builder()
                .setSubject(String.valueOf(akun.getId_user()))
                .claim("username", akun.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXP))
                .signWith(signingKey)
                .compact();

        return token;
    }

    public int validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.parseInt(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new AuthException("Token sudah kadaluarsa. Silakan login ulang.");
        } catch (JwtException e) {
            throw new AuthException("Token tidak valid.");
        }
    }

    public Akun getAkunFromToken(String token) throws SQLException {
        int id_user = validateToken(token);
        return akunRepo.findById(id_user)
                .orElseThrow(() -> new AuthException("Akun tidak ditemukan."));
    }

}