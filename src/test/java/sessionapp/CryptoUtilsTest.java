package sessionapp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import sessionapp.utis.CryptoUtils;

public class CryptoUtilsTest {
  @RepeatedTest(10)
  public void encryptAESCanBeDecrypted() throws GeneralSecurityException, IOException {
    var random = new Random();
    var password = "krjgklrehgskldgjirsejgierslgsdfkghsdfkg";
    var arrSize = random.nextInt(1_048_576, 8_388_608);
    var plainText = new byte[arrSize];
    random.nextBytes(plainText);
    var cipherText = CryptoUtils.encryptAES(plainText, password);
    var plainTextDecrypted = CryptoUtils.decryptAES(cipherText, password);
    assertArrayEquals(plainText, plainTextDecrypted);
  }

  @RepeatedTest(10)
  public void compareHashesTrueForTheSameArray() {
    var random = new Random();
    var arrSize = random.nextInt(100_000, 10_000_000);
    var randomArr = new byte[arrSize];
    random.nextBytes(randomArr);
    assertTrue(CryptoUtils.compareHashes(randomArr, randomArr));
  }

  @RepeatedTest(10)
  public void compareHashesFalseForNotSameArrays() {
    var random = new Random();
    var arrSize = random.nextInt(100_000, 10_000_000);
    var arr1 = new byte[arrSize];
    random.nextBytes(arr1);
    arr1[0] = 1;
    var arr2 = new byte[arrSize];
    random.nextBytes(arr2);
    arr2[0] = 2;
    assertFalse(CryptoUtils.compareHashes(arr1, arr2));
  }

  @Test
  public void compareHashesDifferentArraySize() {
    var arr1 = new byte[] {1, 2, 3};
    var arr2 = new byte[] {5, 6, 7, 8, 9, 10};
    assertFalse(CryptoUtils.compareHashes(arr1, arr2));
  }
}
