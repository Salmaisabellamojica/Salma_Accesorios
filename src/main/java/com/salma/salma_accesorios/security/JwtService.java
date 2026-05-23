package com.salma.salma_accesorios.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:salma-accesorios-dev-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserDetails userDetails) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", userDetails.getUsername());
            payload.put("roles", userDetails.getAuthorities().stream().map(Object::toString).toList());
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("exp", Instant.now().plusSeconds(expirationSeconds).getEpochSecond());

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signature = sign(encodedHeader + "." + encodedPayload);
            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el JWT", ex);
        }
    }

    public String extractUsername(String token) {
        return claims(token).get("sub").toString();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Map<String, Object> claims = claims(token);
        long exp = ((Number) claims.get("exp")).longValue();
        return userDetails.getUsername().equals(claims.get("sub")) && exp > Instant.now().getEpochSecond();
    }

    private Map<String, Object> claims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                throw new IllegalArgumentException("JWT invalido");
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("JWT invalido", ex);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }
}
