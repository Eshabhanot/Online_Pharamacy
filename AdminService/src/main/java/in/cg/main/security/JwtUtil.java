package in.cg.main.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySuperSecretKeyForJwtThatIsVerySecure1234567890}")
    private String secret = "mySuperSecretKeyForJwtThatIsVerySecure1234567890";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Extract all claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract username (subject)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Validate token
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Fallback-tolerant helpers (used by filter)
    public String extractUsernameFlexible(String token) {
        try {
            return extractUsername(token);
        } catch (Exception ignored) {
            return readPayloadField(token, "sub");
        }
    }

    public String extractRoleFlexible(String token) {
        try {
            return extractRole(token);
        } catch (Exception ignored) {
            return readPayloadField(token, "role");
        }
    }

    public boolean isTokenValidFlexible(String token) {
        if (isTokenValid(token)) {
            return true;
        }
        try {
            String expVal = readPayloadField(token, "exp");
            if (expVal == null || expVal.isBlank()) {
                return false;
            }
            long expSeconds = Long.parseLong(expVal);
            return Instant.ofEpochSecond(expSeconds).isAfter(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }

    private String readPayloadField(String token, String field) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(decoded, new TypeReference<Map<String, Object>>() {});
            Object value = payload.get(field);
            return value != null ? String.valueOf(value) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
