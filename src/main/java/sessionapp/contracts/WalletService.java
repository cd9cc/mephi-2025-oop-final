package sessionapp.contracts;

import java.io.IOException;
import java.util.UUID;
import sessionapp.models.Wallet;
import sessionapp.models.context.Context;

public interface WalletService<T extends Context<WalletService<T>>> {
  Wallet getWalletById(T context, UUID id) throws IOException;

  void saveWallet(T context, Wallet wallet) throws IOException;
}
