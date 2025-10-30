package sessionapp.models;

import static sessionapp.utis.CryptoUtils.*;

import java.security.GeneralSecurityException;
import java.util.UUID;

public record User(String username, byte[] password, byte[] salt, UUID walletId) {

  public boolean checkPassword(String providedPassword) throws GeneralSecurityException {
    var providedHash = hashPassword(providedPassword, salt);
    return compareHashes(password, providedHash);
  }

  public static User create(String username, byte[] password, byte[] salt, Wallet wallet) {
    return new User(username, password, salt, wallet.id());
  }

  public UUID getWalletId() {
    return walletId;
  }
}
