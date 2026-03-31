package com.phoenixtask.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretHashingService {

  private static final Logger log = LoggerFactory.getLogger(SecretHashingService.class);
  private static final String V2_PREFIX = "v2:";
  private static final String HMAC_ALG = "HmacSHA256";

  private final byte[] pepper;
  private final boolean pepperEnabled;

  public SecretHashingService(
      @Value("${phoenixtask.security.key-pepper:}") String pepperValue
  ) {
    if (pepperValue == null || pepperValue.isBlank()) {
      this.pepper = new byte[0];
      this.pepperEnabled = false;
      log.warn("No key pepper configured. Using legacy SHA-256 hashing for sensitive keys.");
      return;
    }
    byte[] decoded;
    boolean base64 = false;
    try {
      decoded = Base64.getDecoder().decode(pepperValue.trim());
      base64 = true;
    } catch (IllegalArgumentException ex) {
      decoded = pepperValue.getBytes(StandardCharsets.UTF_8);
    }
    if (!base64) {
      log.warn("Key pepper is not base64. Using raw string bytes for HMAC.");
    }
    if (decoded.length < 32) {
      log.warn("Key pepper length is {} bytes. Recommended length is 32 bytes.", decoded.length);
    }
    this.pepper = decoded;
    this.pepperEnabled = true;
  }

  public Optional<String> hmacHex(String raw) {
    if (!pepperEnabled) {
      return Optional.empty();
    }
    return Optional.of(V2_PREFIX + hmacHexValue(raw));
  }

  public Optional<String> hmacBase64(String raw) {
    if (!pepperEnabled) {
      return Optional.empty();
    }
    return Optional.of(V2_PREFIX + hmacBase64Value(raw));
  }

  public String sha256Hex(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash value", ex);
    }
  }

  public String sha256Base64(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash value", ex);
    }
  }

  public String hashHex(String raw) {
    return hmacHex(raw).orElseGet(() -> sha256Hex(raw));
  }

  public String hashBase64(String raw) {
    return hmacBase64(raw).orElseGet(() -> sha256Base64(raw));
  }

  public boolean isPepperEnabled() {
    return pepperEnabled;
  }

  private String hmacHexValue(String raw) {
    return HexFormat.of().formatHex(hmac(raw));
  }

  private String hmacBase64Value(String raw) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(raw));
  }

  private byte[] hmac(String raw) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(pepper, HMAC_ALG));
      return mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash value", ex);
    }
  }
}
