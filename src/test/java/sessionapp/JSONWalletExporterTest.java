package sessionapp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sessionapp.models.Money;
import sessionapp.models.TransactionType;
import sessionapp.models.Wallet;
import sessionapp.services.JSONWalletExporter;

public class JSONWalletExporterTest {
  private Wallet wallet;
  private final String testCat = "test";

  @BeforeEach
  public void setup() {
    wallet = Wallet.create(UUID.randomUUID());
  }

  @Test
  public void importExport() throws IOException {
    var exporter = new JSONWalletExporter();
    var rand = new Random();
    for (int i = 0; i < 10; i++) {
      var testIncome = Money.of(rand.nextLong(100, 100_000)).get();
      wallet.addIncome(testIncome, testCat);
    }

    var json = exporter.export(wallet);
    var importedWallet = exporter.importWallet(json);

    assertEquals(wallet.id(), importedWallet.id());
    assertArrayEquals(
        wallet.budgets().keySet().toArray(), importedWallet.budgets().keySet().toArray());

    var incomeInSourceWalletSum =
        wallet.sumOfTransactionsInCategory(TransactionType.INCOME).get(testCat);
    var incomeInImportedWalletSum =
        importedWallet.sumOfTransactionsInCategory(TransactionType.INCOME).get(testCat);
    assertEquals(incomeInSourceWalletSum.getAmount(), incomeInImportedWalletSum.getAmount());
  }
}
