package sessionapp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sessionapp.utis.CryptoUtils.generateSalt;
import static sessionapp.utis.CryptoUtils.hashPassword;

import java.security.GeneralSecurityException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sessionapp.models.User;
import sessionapp.models.Wallet;

public class UserTest {
  @Test
  public void canLoginWithCorrectPassword() throws GeneralSecurityException {
    var testPassword = "test_password";
    var walletId = UUID.randomUUID();
    var salt = generateSalt();
    var wallet = Wallet.create(walletId);
    var passwordHash = hashPassword(testPassword, salt);
    var user = User.create("test", passwordHash, salt, wallet);
    assertTrue(user.checkPassword("test_password"));
  }

  @Test
  public void cantLoginWithIncorrectPassword() throws GeneralSecurityException {
    var testPassword = "test_password";
    var walletId = UUID.randomUUID();
    var salt = generateSalt();
    var wallet = Wallet.create(walletId);
    var passwordHash = hashPassword(testPassword, salt);
    var user = User.create("test", passwordHash, salt, wallet);
    assertFalse(user.checkPassword("it's a lie"));
  }
}
