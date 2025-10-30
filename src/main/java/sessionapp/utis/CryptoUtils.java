package sessionapp.utis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class CryptoUtils {
  public static byte[] generateSalt() {
    var salt = new byte[32];
    var rng = new SecureRandom();
    rng.nextBytes(salt);
    return salt;
  }

  public static byte[] hashPassword(String password, byte[] salt) throws GeneralSecurityException {
    var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    var iterations = 80000;
    var keyLength = 512;
    var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
    var key = skf.generateSecret(spec);
    return key.getEncoded();
  }

  /** Сравнение со статичным временем выполнения */
  public static boolean compareHashes(byte[] arr1, byte[] arr2) {
    if (arr1 == null || arr2 == null) return false;
    if (arr1.length != arr2.length) return false;
    var result = 0;
    for (int i = 0; i < arr1.length; i++) {
      result |= arr1[i] ^ arr2[i];
    }
    return result == 0;
  }

  private static final String CIPHER = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int SECRET_KEY_SIZE_LENGTH = 256;
  private static final int SECRET_KEY_ITERATIONS = 100_000;
  private static final int SECRET_KEY_SALT_LENGTH = 16;

  public static byte[] encryptAES(byte[] plainText, String password)
      throws GeneralSecurityException, IOException {
    var secretKeySaltPair = deriveKey(password);
    var secretKey = secretKeySaltPair.secretKey();
    var secretKeySalt = secretKeySaltPair.salt();
    byte[] iv = new byte[GCM_IV_LENGTH];
    SecureRandom.getInstanceStrong().nextBytes(iv);
    var cipher = Cipher.getInstance(CIPHER);
    var spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
    var ciphertext = cipher.doFinal(plainText);
    var out = new ByteArrayOutputStream();
    out.write(secretKeySalt);
    out.write(iv);
    out.write(ciphertext);
    return out.toByteArray();
  }

  public static byte[] decryptAES(byte[] input, String password) throws GeneralSecurityException {
    var secretKeySalt = Arrays.copyOfRange(input, 0, SECRET_KEY_SALT_LENGTH);
    var secretKey = deriveKey(password, secretKeySalt);
    var iv =
        Arrays.copyOfRange(input, SECRET_KEY_SALT_LENGTH, SECRET_KEY_SALT_LENGTH + GCM_IV_LENGTH);
    var cipherText =
        Arrays.copyOfRange(input, SECRET_KEY_SALT_LENGTH + GCM_IV_LENGTH, input.length);
    var cipher = Cipher.getInstance(CIPHER);
    var spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
    return cipher.doFinal(cipherText);
  }

  private static SecretKeySaltPair deriveKey(String password) throws GeneralSecurityException {
    byte[] salt = new byte[SECRET_KEY_SALT_LENGTH];
    SecureRandom sr = new SecureRandom();
    sr.nextBytes(salt);
    PBEKeySpec spec =
        new PBEKeySpec(password.toCharArray(), salt, SECRET_KEY_ITERATIONS, SECRET_KEY_SIZE_LENGTH);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
    byte[] keyBytes = skf.generateSecret(spec).getEncoded();
    var secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
    return new SecretKeySaltPair(secretKey, salt);
  }

  private static SecretKey deriveKey(String password, byte[] salt) throws GeneralSecurityException {
    PBEKeySpec spec =
        new PBEKeySpec(password.toCharArray(), salt, SECRET_KEY_ITERATIONS, SECRET_KEY_SIZE_LENGTH);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
    byte[] keyBytes = skf.generateSecret(spec).getEncoded();
    return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
  }

  private record SecretKeySaltPair(SecretKey secretKey, byte[] salt) {}
}
