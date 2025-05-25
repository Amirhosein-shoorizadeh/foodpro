package util;

import dao.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "your-256-bits-ecretkey-1234567890"; // کلید مخفی (در عمل باید امن باشد)
    private static final long EXPIRATION_TIME = 3600 * 1000;

    public static String generateToken(String phone) {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public static String validateToken(String token) {
        if (TokenDao.isRevoked(token)) return null;

        try {
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime getExpirationDate(String token) {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        Key key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

        Date expiration = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

}