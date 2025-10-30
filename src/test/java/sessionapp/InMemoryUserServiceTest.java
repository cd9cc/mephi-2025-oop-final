package sessionapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sessionapp.services.EncryptedFileWalletService;
import sessionapp.services.InMemoryUserService;
import sessionapp.utis.Config;

public class InMemoryUserServiceTest {

  private EncryptedFileWalletService walletService;
  private InMemoryUserService userService;
  private Config config;

  @BeforeEach
  public void setup() throws IOException {
    Files.writeString(Path.of("config.properties"), "");
    config = Config.load();
    Files.deleteIfExists(config.getUsersFilePath());
    Files.deleteIfExists(config.getWalletsDirectory());
    walletService = new EncryptedFileWalletService(config);
    userService = new InMemoryUserService(config, walletService);
  }

  @AfterEach
  public void cleanup() throws IOException {
    Files.deleteIfExists(Path.of("config.properties"));
    Files.deleteIfExists(config.getUsersFilePath());
    Files.deleteIfExists(config.getWalletsDirectory());
  }

  @Test
  public void registerAndAuthenticateAsUser() {
    var login = "test-user";
    var password = "test-password";
    var registeredUser = userService.registerUser(login, password);
    var loggedInUser = userService.authenticateAsUser(login, password);
    assertNotNull(registeredUser);
    assertNotNull(loggedInUser);
  }

  @Test
  public void registerAndAuthenticateAsUser2() {
    var password = "test-password";
    var ignore = userService.registerUser("test-user-1", password);
    var ignore1 = userService.registerUser("test-user-2", password);
    var ignore2 = userService.registerUser("test-user-3", password);
    var ignore3 = userService.registerUser("test-user-4", password);
    var loggedInUser = userService.authenticateAsUser("test-user-2", password);
    assertNotNull(loggedInUser);
    assertEquals("test-user-2", loggedInUser.username());
  }
}
