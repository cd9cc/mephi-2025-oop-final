package sessionapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static sessionapp.TestUtils.deleteRecursively;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sessionapp.models.Money;
import sessionapp.models.TransactionType;
import sessionapp.models.context.EncryptedFileWalletServiceContext;
import sessionapp.services.EncryptedFileWalletService;
import sessionapp.utis.Config;

public class EncryptedFileWalletServiceTest {

  private EncryptedFileWalletService walletService;
  private Config config;
  private final String password = "test-password";
  private final String testCat = "test";

  @BeforeEach
  public void setup() throws IOException {
    Files.writeString(Path.of("config.properties"), "");
    config = Config.load();
    Files.deleteIfExists(config.getUsersFilePath());
    deleteRecursively(config.getWalletsDirectory());
    walletService = new EncryptedFileWalletService(config);
  }

  @AfterEach
  public void cleanup() throws IOException {
    Files.deleteIfExists(Path.of("config.properties"));
    walletService = null;
    Files.deleteIfExists(config.getUsersFilePath());
    deleteRecursively(config.getWalletsDirectory());
  }

  @Test
  public void generateWallet() {
    var wallet = walletService.generateWallet();
    assertNotNull(wallet);
    assertNotNull(wallet.id());
    assertNotNull(wallet.budgets());
    assertNotNull(wallet.transactions());
  }

  @Test
  public void saveAndRestoreWallet() throws IOException {
    var wallet = walletService.generateWallet();
    var context = new EncryptedFileWalletServiceContext(password);
    walletService.saveWallet(context, wallet);
    var walletServiceWithoutCache = new EncryptedFileWalletService(config);
    var restoredWallet = walletServiceWithoutCache.getWalletById(context, wallet.id());
    assertNotNull(restoredWallet);
    assertEquals(wallet.id(), restoredWallet.id());
  }

  @Test
  public void saveAndRestoreWallet2() throws IOException {
    var wallet = walletService.generateWallet();
    var rand = new Random();

    for (int i = 0; i < 10; i++) {
      var testIncome = Money.of(rand.nextLong(100, 100_000)).get();
      wallet.addIncome(testIncome, testCat);
    }

    var context = new EncryptedFileWalletServiceContext(password);
    walletService.saveWallet(context, wallet);

    var walletServiceWithoutCache = new EncryptedFileWalletService(config);
    var restoredWallet = walletServiceWithoutCache.getWalletById(context, wallet.id());

    var incomeInSourceWallet =
        wallet.sumOfTransactionsInCategory(TransactionType.INCOME).get(testCat);
    var incomeRestoredWallet =
        restoredWallet.sumOfTransactionsInCategory(TransactionType.INCOME).get(testCat);
    assertEquals(incomeInSourceWallet.getAmount(), incomeRestoredWallet.getAmount());
  }

  @Test
  public void saveAndRestoreWallet3() throws IOException {
    var wallet = walletService.generateWallet();
    var rand = new Random();
    var testCats = List.of("test1", "test2", "test3", "test4");
    testCats.forEach(
        cat -> {
          var limit = Money.of(rand.nextLong(100, 100_000)).get();
          wallet.setBudget(cat, limit);
        });

    var context = new EncryptedFileWalletServiceContext(password);
    walletService.saveWallet(context, wallet);

    var walletServiceWithoutCache = new EncryptedFileWalletService(config);
    var restoredWallet = walletServiceWithoutCache.getWalletById(context, wallet.id());

    assertEquals(wallet.budgets().keySet(), restoredWallet.budgets().keySet());
    wallet
        .budgets()
        .forEach(
            (key, value) -> {
              var restoredValue = restoredWallet.budgets().get(key);
              assertEquals(value.getAmount(), restoredValue.getAmount());
            });
  }
}
