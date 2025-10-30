package sessionapp.models.context;

import sessionapp.contracts.WalletService;

public record EncryptedFileWalletServiceContext(String secret)
    implements Context<WalletService<EncryptedFileWalletServiceContext>> {}
