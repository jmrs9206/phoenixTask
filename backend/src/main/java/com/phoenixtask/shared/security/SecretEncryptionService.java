package com.phoenixtask.shared.security;

import com.phoenixtask.shared.error.ValidationException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretEncryptionService {

  private static final Logger log = LoggerFactory.getLogger(SecretEncryptionService.class);
  private static final String PREFIX = "enc:v1:";
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;

  private final SecureRandom secureRandom = new SecureRandom();
  private final SecretKey secretKey;
  private final boolean enabled;

  public SecretEncryptionService(
      @Value("${phoenixtask.security.encryption-key:}") String keyValue
  ) {
    if (keyValue == null || keyValue.isBlank()) {
      this.secretKey = null;
      this.enabled = false;
      log.warn("No encryption key configured. Git webhook secrets will be stored in plaintext.");
      return;
    }
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(keyValue.trim());
    } catch (IllegalArgumentException ex) {
      throw new IllegalStateException("Invalid encryption key: expected base64-encoded 32-byte value");
    }
    if (decoded.length != 32) {
      throw new IllegalStateException("Invalid encryption key length: expected 32 bytes");
    }
    this.secretKey = new SecretKeySpec(decoded, "AES");
    this.enabled = true;
  }

  public String encrypt(String plaintext) {
    if (plaintext == null) {
      return null;
    }
    if (!enabled) {
      return plaintext;
    }
    try {
      byte[] iv = new byte[IV_BYTES];
      secureRandom.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      buffer.put(iv);
      buffer.put(ciphertext);
      String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
      return PREFIX + encoded;
    } catch (Exception ex) {
      throw new ValidationException("Unable to encrypt secret");
    }
  }

  public String decryptIfNeeded(String value) {
    if (value == null) {
      return null;
    }
    if (!value.startsWith(PREFIX)) {
      return value;
    }
    if (!enabled) {
      throw new ValidationException("Encryption key not configured for encrypted secret");
    }
    String encoded = value.substring(PREFIX.length());
    try {
      byte[] payload = Base64.getUrlDecoder().decode(encoded);
      ByteBuffer buffer = ByteBuffer.wrap(payload);
      byte[] iv = new byte[IV_BYTES];
      buffer.get(iv);
      byte[] ciphertext = new byte[buffer.remaining()];
      buffer.get(ciphertext);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new ValidationException("Unable to decrypt secret");
    }
  }

  public boolean isEnabled() {
    return enabled;
  }
}
