package sessionapp.contracts;

import java.io.IOException;
import sessionapp.models.Wallet;

public interface WalletExporter<T> {
  T export(Wallet w) throws IOException;

  Wallet importWallet(T arg) throws IOException;
}
