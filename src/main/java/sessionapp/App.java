package sessionapp;

import java.io.IOException;
import java.util.Scanner;
import sessionapp.controllers.CliController;
import sessionapp.exceptions.BadConfigurationException;
import sessionapp.services.EncryptedFileWalletService;
import sessionapp.services.InMemoryUserService;
import sessionapp.utis.Config;

public class App {
  public static void main(String[] args) {
    try {
      var config = Config.load();
      var walletService = new EncryptedFileWalletService(config);
      var userService = new InMemoryUserService(config, walletService);
      var scanner = new Scanner(System.in);
      var cliController = new CliController(config, scanner, userService, walletService);

      cliController.start();
      while (true) {
        try {
          cliController.handleInput();
        } catch (Exception ignored) {
          break;
        }
      }
    } catch (BadConfigurationException e) {
      System.out.println("Приложение некорректно сконфигурировано");
      System.exit(1);
    } catch (IOException e) {
      System.out.println("Не удалось прочитать файл конфигурации");
      System.exit(1);
    } catch (Exception e) {
      System.out.println("Непредвиденное исключение: " + e.getMessage());
      System.exit(1);
    }
  }
}
