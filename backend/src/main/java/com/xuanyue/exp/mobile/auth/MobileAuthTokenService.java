package com.xuanyue.exp.mobile.auth;

import com.xuanyue.exp.mobile.config.JwtProperties;
import com.xuanyue.exp.mobile.entity.MbAuthRefreshToken;
import com.xuanyue.exp.mobile.repository.MbAuthRefreshTokenRepository;
import com.xuanyue.exp.system.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class MobileAuthTokenService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_STATUS = "status";
    private static final String CLAIM_ORG = "org";
    private static final String CLAIM_CLIENT = "client";
    private static final String CLIENT_MOBILE = "mobile";

    private final JwtProperties jwtProperties;
    private final MbAuthRefreshTokenRepository refreshTokenRepository;
    private final SecretKey secretKey;

    public MobileAuthTokenService(JwtProperties jwtProperties,
                                  MbAuthRefreshTokenRepository refreshTokenRepository) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.secretKey = buildKey(jwtProperties.getSecret());
    }

    public String createAccessToken(SysUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenSeconds() * 1000L);
        return Jwts.builder()
                .setSubject(user.getUserId())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(CLAIM_ROLE, user.getUserRoleId())
                .claim(CLAIM_STATUS, user.getStatus())
                .claim(CLAIM_ORG, user.getRootOrgId())
                .claim(CLAIM_CLIENT, CLIENT_MOBILE)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public MobileAuthUser parseAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseClaimsJws(token.trim());
            Claims claims = parsed.getBody();
            if (!CLIENT_MOBILE.equals(claims.get(CLAIM_CLIENT, String.class))) {
                return null;
            }
            return new MobileAuthUser(
                    claims.getSubject(),
                    claims.get(CLAIM_ROLE, String.class),
                    claims.get(CLAIM_STATUS, String.class),
                    claims.get(CLAIM_ORG, String.class),
                    null,
                    null
            );
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    @Transactional
    public String issueRefreshToken(SysUser user, String deviceId) {
        String resolvedDeviceId = resolveDeviceId(deviceId);
        refreshTokenRepository.deleteByUserIdAndDeviceId(user.getUserId(), resolvedDeviceId);

        String rawToken = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        Date expire = new Date(now.getTime() + jwtProperties.getRefreshTokenSeconds() * 1000L);

        MbAuthRefreshToken entity = new MbAuthRefreshToken();
        entity.setUserId(user.getUserId());
        entity.setDeviceId(resolvedDeviceId);
        entity.setTokenHash(hashToken(rawToken));
        entity.setExpireTime(expire);
        entity.setRevoked(false);
        entity.setCreatedAt(now);
        entity.setLastUsedAt(now);
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public Optional<String> validateRefreshToken(String rawToken, String deviceId) {
        if (!StringUtils.hasText(rawToken)) {
            return Optional.empty();
        }
        String hash = hashToken(rawToken.trim());
        MbAuthRefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).orElse(null);
        if (stored == null || Boolean.TRUE.equals(stored.getRevoked())) {
            return Optional.empty();
        }
        if (stored.getExpireTime() != null && stored.getExpireTime().before(new Date())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            return Optional.empty();
        }
        String resolvedDeviceId = resolveDeviceId(deviceId);
        if (!resolvedDeviceId.equals(stored.getDeviceId())) {
            return Optional.empty();
        }
        stored.setLastUsedAt(new Date());
        refreshTokenRepository.save(stored);
        return Optional.of(stored.getUserId());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hashToken(rawToken.trim()))
                .ifPresent(row -> {
                    row.setRevoked(true);
                    refreshTokenRepository.save(row);
                });
    }

    public long getAccessTokenSeconds() {
        return jwtProperties.getAccessTokenSeconds();
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String resolveDeviceId(String deviceId) {
        if (StringUtils.hasText(deviceId)) {
            return deviceId.trim();
        }
        return "unknown-device";
    }

    private static SecretKey buildKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
