package com.quizduel.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizduel.exception.ApiException;

/** Minimal HS256 JSON Web Token implementation (no extra library needed). Tokens last 7 days. */
@Service
public class JwtService {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final long TTL_SECONDS = 7L * 24 * 3600;
    private static final String HEADER = B64.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtService(@Value("${app.jwt-secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(long userId, String role) {
        long now = Instant.now().getEpochSecond();
        String payload = B64.encodeToString(
                ("{\"id\":" + userId + ",\"role\":\"" + role + "\",\"iat\":" + now + ",\"exp\":" + (now + TTL_SECONDS) + "}")
                        .getBytes(StandardCharsets.UTF_8));
        return HEADER + "." + payload + "." + B64.encodeToString(hmac(HEADER + "." + payload));
    }

    public CurrentUser verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("malformed");
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, B64D.decode(parts[2]))) throw new IllegalArgumentException("bad signature");
            JsonNode claims = mapper.readTree(B64D.decode(parts[1]));
            if (claims.get("exp").asLong() < Instant.now().getEpochSecond()) throw new IllegalArgumentException("expired");
            return new CurrentUser(claims.get("id").asLong(), claims.get("role").asText());
        } catch (Exception e) {
            throw ApiException.unauthorized("Invalid or expired token");
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
