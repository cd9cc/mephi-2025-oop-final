package sessionapp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import sessionapp.contracts.UserService;
import sessionapp.contracts.WalletService;
import sessionapp.exceptions.AuthenticationFailedException;
import sessionapp.exceptions.NegativeMoneyException;
import sessionapp.exceptions.NotAuthenticatedException;
import sessionapp.exceptions.UserAlreadyExistsException;
import sessionapp.models.Money;
import sessionapp.models.TransactionType;
import sessionapp.models.User;
import sessionapp.models.Wallet;
import sessionapp.models.budget.AlmostOverspent;
import sessionapp.models.budget.BudgetOk;
import sessionapp.models.budget.Overspent;
import sessionapp.models.context.EncryptedFileWalletServiceContext;
import sessionapp.models.validated.Invalid;
import sessionapp.models.validated.Valid;
import sessionapp.services.EncryptedFileWalletService;
import sessionapp.services.JSONWalletExporter;
import sessionapp.utis.Config;
import sessionapp.utis.printers.PrettyPrinter;
import sessionapp.utis.validators.CategoryNameValidator;
import sessionapp.utis.validators.UserNameValidator;
import sessionapp.utis.validators.UserPasswordValidator;

public class CliController {
  private final Config config;
  private final Scanner scanner;
  private final UserService userService;
  private final WalletService<EncryptedFileWalletServiceContext> walletService;
  private User currentUser;
  private Wallet currentWallet;
  private EncryptedFileWalletServiceContext context;

  public CliController(
      Config config,
      Scanner scanner,
      UserService userService,
      EncryptedFileWalletService walletService) {
    this.config = config;
    this.scanner = scanner;
    this.userService = userService;
    this.walletService = walletService;
    this.currentUser = null;
    this.currentWallet = null;
  }

  private final String HELP_CMD = "help";
  private final String AUTHENTICATE_CMD = "login";
  private final String REGISTER_CMD = "register";
  private final String EXIT_CMD = "exit";
  private final String ADD_EXPENSE_CMD = "add_expense";
  private final String ADD_INCOME_CMD = "add_income";
  private final String SHOW_STATISTICS_CMD = "show_statistics";
  private final String SET_BUDGET_CMD = "set_budget";
  private final String GET_BUDGET_CMD = "get_budget";
  private final String EXPORT_WALLET_CMD = "export_wallet";
  private final String IMPORT_WALLET_CMD = "import_wallet";

  public void start() {
    System.out.printf(
        "Введите \"%s\" чтобы получить справку по командам\nВведите \"%s\" чтобы войти в приложение или \"%s\" чтобы зарегистрироваться в качестве нового пользователя:\n",
        HELP_CMD, AUTHENTICATE_CMD, REGISTER_CMD);
  }

  public void handleInput() {
    var input = scanner.next().trim().toLowerCase();
    switch (input) {
      case HELP_CMD -> help();
      case AUTHENTICATE_CMD -> authenticate();
      case REGISTER_CMD -> register();
      case EXIT_CMD -> exit();
      case ADD_EXPENSE_CMD -> addExpense();
      case ADD_INCOME_CMD -> addIncome();
      case SHOW_STATISTICS_CMD -> showStatistics();
      case SET_BUDGET_CMD -> setBudget();
      case GET_BUDGET_CMD -> getBudget();
      case EXPORT_WALLET_CMD -> exportWallet();
      case IMPORT_WALLET_CMD -> importWallet();
      default -> System.out.printf("Print \"%s\"\n", HELP_CMD);
    }
  }

  private void help() {
    var help =
        """
                        Команды:
                        Вход в приложение в качестве пользователя: %s
                        Регистрация нового пользователя: %s
                        Выход из приложения: %s
                        Добавить расход: %s
                        Добавить доход: %s
                        Отобразить статистику доходов/расходов и бюджетов: %s
                        Установить бюджет для категории: %s
                        Получить данные о бюджете для категории или нескольких категорй: %s
                        Экспортировать кошелёк: %s
                        Импортировать кошелёк: %s
                        """;
    System.out.printf(
        help,
        AUTHENTICATE_CMD,
        REGISTER_CMD,
        EXIT_CMD,
        ADD_EXPENSE_CMD,
        ADD_INCOME_CMD,
        SHOW_STATISTICS_CMD,
        SET_BUDGET_CMD,
        GET_BUDGET_CMD,
        EXPORT_WALLET_CMD,
        IMPORT_WALLET_CMD);
  }

  private void authenticate() {
    withExceptionHandler(
        () -> {
          System.out.println("Введите имя пользователя: ");
          var login = scanner.next().trim();
          switch (UserNameValidator.validate(login)) {
            case Valid ignored -> {}
            case Invalid invalid -> {
              System.out.printf("Имя пользователя не прошло валидацию: %s\n", invalid.reason());
              return;
            }
          }

          System.out.println("Введите пароль: ");
          var password = scanner.next().trim();

          var user = userService.authenticateAsUser(login, password);
          context = new EncryptedFileWalletServiceContext(password);
          var wallet = walletService.getWalletById(context, user.getWalletId());
          currentUser = user;
          currentWallet = wallet;
          System.out.printf("Вы вошли в приложение как %s\n", user.username());
        });
  }

  private void register() {
    withExceptionHandler(
        () -> {
          System.out.println("Введите желаемое имя пользователя: ");
          var login = scanner.next().trim();
          switch (UserNameValidator.validate(login)) {
            case Valid ignored -> {}
            case Invalid invalid -> {
              System.out.printf("Имя пользователя не прошло валидацию: %s\n", invalid.reason());
              return;
            }
          }

          System.out.println("Введите желаемый пароль: ");
          var password = scanner.next().trim();

          switch (UserPasswordValidator.validate(password)) {
            case Valid ignored -> {}
            case Invalid invalid -> {
              System.out.printf("Пароль не прошёл валидацию %s\n", invalid.reason());
              return;
            }
          }

          currentUser = userService.registerUser(login, password);
          context = new EncryptedFileWalletServiceContext(password);
          currentWallet = walletService.getWalletById(context, currentUser.getWalletId());
          walletService.saveWallet(context, currentWallet);
          System.out.printf(
              "Пользователь %s успешно создан. Вы вошли в приложение как пользователь %s\n",
              login, login);
        });
  }

  private void addExpense() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println("Введите категорию: ");
          var category = scanner.next().trim();
          System.out.println(
              "Введите сумму в рублях. Укажите копейки после запятой. Например: 42,42. Введите сумму: ");
          var amount = Money.of(scanner.next().trim());
          if (amount.isPresent()) {
            var result = currentWallet.addExpense(amount.get(), category);
            walletService.saveWallet(context, currentWallet);
            System.out.println("Расход учтён");
            if (result.isPresent()) {
              switch (result.get()) {
                case BudgetOk ignored -> {}
                case AlmostOverspent almostOverspent ->
                    System.out.printf(
                        "Бюджет по категории %s почти исчерпан, осталось: %s",
                        category, PrettyPrinter.printMoney(almostOverspent.remaining()));
                case Overspent overspent ->
                    System.out.printf(
                        "Бюджет по категории %s превышен, сумма первышения: %s",
                        category, PrettyPrinter.printMoney(overspent.overspent()));
              }
            }
          } else {
            System.out.println("Не удалось разобрать сумму");
          }
        });
  }

  private void addIncome() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println("Введите категорию: ");
          var category = scanner.next().trim();
          System.out.println(
              "Введите сумму в рублях. Укажите копейки после запятой. Например: 42,42. Введите сумму: ");
          var amount = Money.of(scanner.next().trim());
          if (amount.isPresent()) {
            currentWallet.addIncome(amount.get(), category);
            walletService.saveWallet(context, currentWallet);
            System.out.println("Доход учтён");
          } else {
            System.out.println("Не удалось разобрать сумму");
          }
        });
  }

  private void showStatistics() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println("\n--- Статистика ---");
          var income = currentWallet.sumOfTransactionsOfType(TransactionType.INCOME);
          var expenses = currentWallet.sumOfTransactionsOfType(TransactionType.EXPENSE);
          System.out.println("Всего доходов: " + PrettyPrinter.printMoney(income));
          System.out.println("Всего расходов: " + PrettyPrinter.printMoney(expenses));
          System.out.println();

          var hasOverspent = expenses.isGreater(income);
          if (hasOverspent) System.out.println("Расходы превышают доходы!\n");

          System.out.println("--- Доходы по категориям ---");
          var incomeByCats = currentWallet.sumOfTransactionsInCategory(TransactionType.INCOME);
          System.out.println(PrettyPrinter.printMapOfMoney(incomeByCats));

          System.out.println("--- Расходы по категориям ---");
          var expensesByCats = currentWallet.sumOfTransactionsInCategory(TransactionType.EXPENSE);
          System.out.println(PrettyPrinter.printMapOfMoney(expensesByCats));

          var spending = currentWallet.budgetSpending(null);
          if (!spending.isEmpty()) {
            System.out.println("--- Действующие бюджеты ---");
            System.out.println(PrettyPrinter.printMapOfSpendingRes(spending));
          }
        });
  }

  private void setBudget() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println("Введите категорию: ");
          var category = scanner.next().trim();

          switch (CategoryNameValidator.validate(category)) {
            case Valid ignored -> {}
            case Invalid invalid -> {
              System.out.printf("Категория не прошла валидацию: %s\n", invalid.reason());
              return;
            }
          }

          System.out.println(
              "Введите сумму в рублях. Укажите копейки после запятой. Например: 42,42. Введите сумму: ");
          var amount = Money.of(scanner.next().trim());

          if (amount.isPresent()) {
            currentWallet.setBudget(category, amount.get());
            walletService.saveWallet(context, currentWallet);
            System.out.println("Бюджет установлен");
            if (amount.filter(Money::isZero).isPresent()) {
              System.out.printf("Для категории %s установлен бюджет с лимитом 0!", category);
            }
          } else {
            System.out.println("Не удалось разобрать сумму");
          }
        });
  }

  private void getBudget() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println(
              "Введите категории через запятую или нажмите ввод чтобы отобразить все категории: ");
          var categories =
              Arrays.stream(scanner.next().trim().split(",")).map(String::trim).toList();
          if (categories.isEmpty()) {
            var result = currentWallet.budgetSpending(null);
            System.out.println(PrettyPrinter.printMapOfSpendingRes(result));
          } else {
            var budgets = currentWallet.budgets().keySet();
            var unknownCategories = categories.stream().filter(c -> !budgets.contains(c)).toList();
            if (!unknownCategories.isEmpty()) {
              System.out.printf(
                  "Неизвестные категории: %s \n", PrettyPrinter.mkString(unknownCategories));
              return;
            }
            var result = currentWallet.budgetSpending(categories);
            System.out.println(PrettyPrinter.printMapOfSpendingRes(result));
          }
        });
  }

  private void exportWallet() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          var exportFileName = currentWallet.id() + "_" + System.currentTimeMillis();
          var exportFilePath = config.getExportsDirectory().resolve(exportFileName);
          var exporter = new JSONWalletExporter();
          var json = exporter.export(currentWallet);
          if (!Files.exists(config.getExportsDirectory())) {
            Files.createDirectories(config.getExportsDirectory());
          }
          Files.writeString(exportFilePath, json);
          System.out.printf("Кошелёк экспортирован в файл %s\n", exportFilePath.toAbsolutePath());
        });
  }

  private void importWallet() {
    withExceptionHandler(
        () -> {
          ensureLoggedIn();
          System.out.println("Введите путь к файлу для импорта кошелька: ");
          var importFilePathStr = scanner.next().trim();
          var importFilePath = Paths.get(importFilePathStr);
          if (!Files.exists(importFilePath)) {
            System.out.printf("Файл %s не существует", importFilePath);
            return;
          }
          try {
            var json = Files.readString(importFilePath);
            var importer = new JSONWalletExporter();
            var wallet = importer.importWallet(json);
            walletService.saveWallet(context, wallet);
            System.out.printf("Кошелёк %s успешно импортирован\n", wallet.id());
          } catch (JsonProcessingException e) {
            System.out.println(
                "Формат файла для импорта не соответствует ожидаемому, кошелёк не был импортирован");
          } catch (IOException e) {
            System.out.println("Не удалось прочитать файл, кошелёк не был импортирован");
          }
        });
  }

  private void exit() {
    try {
      System.out.println("выходим из приложения...");
      if (currentWallet != null) {
        walletService.saveWallet(context, currentWallet);
      }
    } catch (Exception e) {
      System.out.println("При выходе произошла непредвиденная ошибка: " + e.getMessage());
    }
    System.exit(0);
  }

  private void ensureLoggedIn() {
    if (currentUser == null) {
      throw new NotAuthenticatedException("Для осуществления операции необходимо авторизоваться");
    }
    if (currentWallet == null) {
      throw new NotAuthenticatedException("Для осуществления операции необходимо авторизоваться");
    }
    if (context == null) {
      throw new NotAuthenticatedException("Для осуществления операции необходимо авторизоваться");
    }
  }

  private void withExceptionHandler(ThrowingRunnable c) {
    try {
      c.run();
    } catch (AuthenticationFailedException e) {
      System.out.println("Неверный пароль или пользователь не существует");
    } catch (UserAlreadyExistsException e) {
      System.out.println("Такой пользователь уже существует");
    } catch (NegativeMoneyException e) {
      System.out.println("Сумма не может быть отрицательной");
    } catch (NotAuthenticatedException e) {
      System.out.printf(
          "Для осуществления операции необходимо авторизоваться с помощью команды %s\n",
          AUTHENTICATE_CMD);
    } catch (Exception e) {
      System.out.println("Произошла непредвиденная ошибка " + e.getMessage());
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
