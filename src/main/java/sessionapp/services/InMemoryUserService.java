package sessionapp.services;

import static sessionapp.utis.CryptoUtils.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import sessionapp.contracts.UserService;
import sessionapp.exceptions.AuthenticationFailedException;
import sessionapp.exceptions.BadConfigurationException;
import sessionapp.exceptions.UserAlreadyExistsException;
import sessionapp.models.User;
import sessionapp.utis.Config;

public class InMemoryUserService implements UserService {

  private final Map<String, User> users;
  private final Config config;
  private final EncryptedFileWalletService walletService;

  public InMemoryUserService(Config config, EncryptedFileWalletService walletService)
      throws BadConfigurationException {
    this.config = config;
    this.walletService = walletService;
    var filePath = config.getUsersFilePath();
    try {
      if (Files.exists(filePath)) {
        users = loadUsersFromFile(filePath);
      } else {
        users = new HashMap<>();
        Files.writeString(filePath, "{}");
      }
    } catch (IOException e) {
      throw new BadConfigurationException(e);
    }
  }

  public User registerUser(String username, String password) throws UserAlreadyExistsException {
    if (users.containsKey(username)) {
      throw new UserAlreadyExistsException(username);
    }

    try {
      var salt = generateSalt();
      var passwordHash = hashPassword(password, salt);
      var wallet = walletService.generateWallet();
      var user = User.create(username, passwordHash, salt, wallet);
      users.put(username, user);
      saveUsersToFile(config.getUsersFilePath(), users);
      return user;
    } catch (GeneralSecurityException | IOException e) {
      throw new BadConfigurationException(e);
    }
  }

  public User authenticateAsUser(String username, String password)
      throws AuthenticationFailedException {
    var user = users.get(username);
    if (user == null) {
      throw new AuthenticationFailedException(username);
    }
    boolean ok;
    try {
      ok = user.checkPassword(password);
    } catch (GeneralSecurityException e) {
      throw new BadConfigurationException(e);
    }
    if (ok) {
      return user;
    } else {
      throw new AuthenticationFailedException(username);
    }
  }

  private Map<String, User> loadUsersFromFile(Path filePath) throws IOException {
    var usersJson = Files.readString(filePath);
    var mapper = new ObjectMapper();
    return mapper.readValue(usersJson, new TypeReference<>() {});
  }

  private void saveUsersToFile(Path filePath, Map<String, User> users) throws IOException {
    var mapper = new ObjectMapper();
    var usersJson = mapper.writeValueAsString(users);
    Files.writeString(filePath, usersJson);
  }
}
