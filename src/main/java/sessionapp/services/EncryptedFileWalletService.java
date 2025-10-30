package sessionapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sessionapp.contracts.WalletService;
import sessionapp.models.Wallet;
import sessionapp.models.context.EncryptedFileWalletServiceContext;
import sessionapp.utis.Config;
import sessionapp.utis.CryptoUtils;

public class EncryptedFileWalletService
    implements WalletService<EncryptedFileWalletServiceContext> {
  private final Config config;
  private final Map<UUID, Wallet> wallets = new HashMap<>();

  public EncryptedFileWalletService(Config config) {
    this.config = config;
  }

  public Wallet generateWallet() {
    var walletId = UUID.randomUUID();
    var wallet = Wallet.create(walletId);
    wallets.put(walletId, wallet);
    return wallet;
  }

  public Wallet getWalletById(EncryptedFileWalletServiceContext context, UUID id)
      throws IOException {
    if (wallets.containsKey(id)) {
      return wallets.get(id);
    } else {
      var walletPath = config.getWalletsDirectory().resolve(id.toString());

      var cipherText = Files.readAllBytes(walletPath);
      try {
        var plainText = CryptoUtils.decryptAES(cipherText, context.secret());
        var json = new String(plainText, StandardCharsets.UTF_8);
        var mapper = new ObjectMapper();
        var wallet = mapper.readValue(json, Wallet.class);
        wallets.put(wallet.id(), wallet);
        return wallet;
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void saveWallet(EncryptedFileWalletServiceContext context, Wallet wallet)
      throws IOException {
    wallets.put(wallet.id(), wallet);
    var mapper = new ObjectMapper();
    var json = mapper.writeValueAsString(wallet);
    var jsonBytes = json.getBytes(StandardCharsets.UTF_8);
    byte[] walletEncrypted;
    try {
      walletEncrypted = CryptoUtils.encryptAES(jsonBytes, context.secret());
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
    var walletPath = config.getWalletsDirectory().resolve(wallet.id().toString());
    if (!Files.exists(config.getWalletsDirectory()))
      Files.createDirectories(config.getWalletsDirectory());
    Files.write(walletPath, walletEncrypted);
  }
}
