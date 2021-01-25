package com.ulashchick.dashboard.auth.helpers;


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {

  private PasswordHelper() {
  }

  public static String hashPassword(@Nonnull String rawPassword) {
    final SecureRandom random = new SecureRandom();
    final byte[] salt = new byte[16];
    random.nextBytes(salt);

    try {
      final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      final KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, 65536, 128);
      final byte[] hash = factory.generateSecret(spec).getEncoded();

      return Arrays.toString(hash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Invalid hashing algorithm", e);
    }
  }

}
