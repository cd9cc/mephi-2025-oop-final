package sessionapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import sessionapp.contracts.WalletExporter;
import sessionapp.models.Wallet;

public class JSONWalletExporter implements WalletExporter<String> {
  @Override
  public String export(Wallet w) throws IOException {
    var mapper = new ObjectMapper();
    return mapper.writeValueAsString(w);
  }

  @Override
  public Wallet importWallet(String arg) throws IOException {
    var mapper = new ObjectMapper();
    return mapper.readValue(arg, Wallet.class);
  }
}
