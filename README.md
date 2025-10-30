#### Сборка
В качестве системы автоматизации сборки используется Gradle. В проекте реализована автоматическая сборка с помощью GitHub Actions. Настройки находятся в файле .github/workflows/gradle.yml

Применение форматирования
```
./gradlew spotlessApply
```

Запуск тестов
```
./gradlew test
```

Проверка форматирования
```
./gradlew spotlessCheck
```

Сборка проекта (включает проверку форматирования и запуск тестов)
```
./gradlew jar
```

#### Зависимости
В проекте используются следующие внешние библиотеки: \
- jackson-databind https://central.sonatype.com/artifact/com.fasterxml.jackson.core/jackson-databind - для сериализации/десериализации JSON
- spotless-plugin-gradle https://central.sonatype.com/artifact/com.diffplug.spotless/spotless-plugin-gradle - форматтер/линтер
- junit-jupiter https://central.sonatype.com/artifact/org.mockito/junit-jupiter - фреймворк для юнит-тестов
- junit-bom https://central.sonatype.com/artifact/org.junit/junit-bom - набор зависимостей для junit-jupiter

#### Структура проекта
```
└───sessionapp
    │   App.java
    │
    ├───contracts
    │       UserService.java
    │       WalletExporter.java
    │       WalletService.java
    │
    ├───controllers
    │       CliController.java
    │
    ├───exceptions
    │       AuthenticationFailedException.java
    │       BadConfigurationException.java
    │       NegativeMoneyException.java
    │       UserAlreadyExistsException.java
    │       NotAuthenticatedException.java
    ├───models
    │   │   Money.java
    │   │   Transaction.java
    │   │   TransactionType.java
    │   │   User.java
    │   │   Wallet.java
    │   │
    │   ├───budget
    │   │       AlmostOverspent.java
    │   │       BudgetOk.java
    │   │       BudgetSpendingResult.java
    │   │       Overspent.java
    │   │
    │   ├───context
    │   │       Context.java
    │   │       EncryptedFileWalletServiceContext.java
    │   │
    │   └───validated
    │           Invalid.java
    │           Valid.java
    │           Validated.java
    │
    ├───services
    │       EncryptedFileWalletService.java
    │       InMemoryUserService.java
    │       JSONWalletExporter.java
    │
    └───utis
        │   Config.java
        │   CryptoUtils.java
        │
        ├───printers
        │       PrettyPrinter.java
        │
        └───validators
                CategoryNameValidator.java
                UserNameValidator.java
                UserPasswordValidator.java
```

##### App.java
основной исполняемый класс приложения
##### contracts
Содержит интерфейсы
- UserService - сервис пользователей
- WalletService - сервис кошельков
- WalletExporter<T> - сервис для сериализации/десериализации кошельков в произвольном формате
##### controllers
Содержит контроллеры
- CliContoller - контроллер для работы с вводом/выводом через командную строку
##### exceptions
Содержит собственные исключения
- AuthenticationFailedException - ошибка аутентификации пользователя
- BadConfigurationException - ошибка конфигурации приложения
- NegativeMoneyException - возникновения объекта Money с отрицательной денежной суммой
- UserAlreadyExistsException - попытка зарегистрировать уже существующего пользователя
- NotAuthenticatedException - пользователь не прошёл аутентификацию
##### models
Содержит модели и DTO приложения
- Money - всегда положительная сумма денег
- Transaction - денежная операция, транзакция
- TransactionType - перечисление типов транзакций, доход или расход
- User - пользователь приложения, содержит данные для авторизации пользователя по паролю, а также уникальный идентификатор пользовательского кошелька. Пароль хранится в захешированном виде
- Wallet - кошелёк пользователя, содержит данные о доходах и расходах, а также методы для работы с ними
- budget/BudgetSpendingResult - интерфейс, алгебраический тип данных, отражающий результат выполнения операции учёта расхода. Возможны варианты BudgetOk, AlmostOverspent, Overspent
- budget/BudgetOk - учёт расхода не привёл к перерасходу бюджета
- budget/AlmostOverspent - учёт расхода не привёл к перерасходу бюджета, но лимит почти исчерпан
- budget/Overspent - учёт расхода привёл к перерасходу бюджета
- context/Context - интерфейс, контекст выполнения приложения (по аналогии с ctx.Context из Go)
- context/EncryptedFileWalletServiceContext - контекст, совместимый с EncryptedFileWalletService
- validated/Validated - интерфейс, алгебраический тип данных, отражающий результат абстрактной валидации
- validated/Valid - результат успешной валидации
- validated/Invalid - результат неуспешной валидации, содержащий человекочитаемое сообщение о всех ошибках валидации
##### services
Содержит конкретные реализации интерфейсов сервисов
- EncryptedFileWalletService - реализация WalletService, хранящая данные в оперативной памяти и сохраняющая данные о кошельках в отдельные зашифрованные файлы, используя пользовательский пароль в качестве источника ключа шифрования
- InMemoryUserService - реализация UserService хранящая данные в оперативной памяти и сохраняющая эти данные на диск в формате JSON
- JSONWalletExporter - реализация WalletExporter, сериализующая/десериализующая кошельки в формате JSON
##### utils
Содержит различные утилиты
- Config - объект, хранящий конфигурацию приложения
- CryptoUtils - утилиты для работы с криптографией
- printers/PrettyPrinter - утилита для форматирования различных типов в человекочитаемый формат
- validators/CategoryNameValidator - валидатор имён категорий
- validators/UserNameValidator - валидатор пользовательских имён
- validators/UserPasswordValidator - валидатор пользовательски паролей

#### Команды для работы с приложением
- help - выводит список поддерживаемых команд с их кратким описанием
- login - запускает процесс аутентификации по логину и паролю
- register - запускает процесс регистрации по логину и паролю
- exit - закрывает приложение, сохраняет статус открытого кошелька
- add_expense - запускает процесс добавления расходной транзакции
- add_income - запускает процесс добавления доходной транзации
- show_statistics - отображает статистику по доходам и расходам, сводку по текущим остаткам бюджета, а также уведомления о превышении или скором превышении бюджета
- set_budget - запускает процесс установки бюджета для определённой категории
- get_budget - отображает остаток текущего бюджета по одной, нескольким или всем категориям, для которых установлен бюджет. В случае, если в запросе передана несуществующая категория, пользователю выводится предупреждение об этом
- export_wallet - экспортирует данные кошелька в формате JSON
- import_wallet - импортирует данные, выгруженные командой export_wallet в текущий активный кошелёк

#### Возможности приложения

1. Авторизация пользователей по логину и паролю, пароли хранятся в зашифрованном виде в соответствии с актуальными рекомендациями NIST. Поддерживается работа нескольких пользователей
2. Работа через консольный пользовательский интерфейс
3. Учёт доходов и расходов. Операции сохраняются на носитель в момент добавления, а также в момент выхода из приложения
4. Кошелёк пользователя привязан к авторизованному пользователю и хранит все операции. Пользовательский баланс представлен как агрегация всех операций
5. Возможность создать бюджет для произвольной категории
6. Возможность получить текущий остаток бюджета по каждой категории, сводку по оставшемуся бюджету для каждой категории, а также список доходов и расходов
7. Уведомление о превышении или скором исчерпании бюджета при добавлении операции расхода
8. Файлы пользовательских кошельков хранятся на диске в зашифрованном виде и расшифровываются в момент аутентификации пользователя. Все изменения сохраняются в реальном времени
9. Валидация ввода пользователя
10. Возможность получить сводку остатков бюджета по одной, нескольким или всем категориям
11. Возможность редактирования бюджетов и категорий
12. Экспорт/импорт кошельков в формате JSON
