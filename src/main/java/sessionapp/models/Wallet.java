package sessionapp.models;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import sessionapp.models.budget.BudgetOk;
import sessionapp.models.budget.BudgetSpendingResult;

public record Wallet(UUID id, List<Transaction> transactions, Map<String, Money> budgets) {

  public Money sumOfTransactionsOfType(TransactionType type) {
    return sumOfTransactionsOfType(type, null);
  }

  public Money sumOfTransactionsOfType(TransactionType type, List<String> categories) {
    Predicate<Transaction> predicate;
    if (categories != null) {
      predicate = x -> x.type() == type && categories.contains(x.category());
    } else {
      predicate = x -> x.type() == type;
    }
    return transactions.stream()
        .filter(predicate)
        .map(Transaction::sum)
        .reduce(Money.zero(), Money::plus);
  }

  public Map<String, BudgetSpendingResult> budgetSpending(List<String> categoriesFilter) {
    Predicate<Map.Entry<String, Money>> budgetsPredicate;
    if (categoriesFilter == null) {
      budgetsPredicate = x -> true;
    } else {
      budgetsPredicate = x -> categoriesFilter.contains(x.getKey());
    }
    var expensesByCat = sumOfTransactionsInCategory(TransactionType.EXPENSE);
    return budgets.entrySet().stream()
        .filter(budgetsPredicate)
        .map(
            kvp -> {
              var category = kvp.getKey();
              if (expensesByCat.containsKey(category)) {
                var spent = expensesByCat.get(category);
                return BudgetSpendingResult.of(category, spent, kvp.getValue());
              } else {
                return new BudgetOk(category, kvp.getValue());
              }
            })
        .collect(Collectors.toMap(BudgetSpendingResult::category, x -> x));
  }

  public Map<String, Money> sumOfTransactionsInCategory(TransactionType type) {
    return transactions.stream()
        .filter(t -> t.type() == type)
        .collect(
            Collectors.groupingBy(
                Transaction::category,
                Collectors.mapping(
                    Transaction::sum, Collectors.reducing(Money.zero(), Money::plus))));
  }

  public Optional<BudgetSpendingResult> addExpense(Money sum, String category) {
    var ts = System.currentTimeMillis();
    var transaction = new Transaction(sum, category, TransactionType.EXPENSE, ts);
    transactions.add(transaction);
    if (budgets.containsKey(category)) {
      var expensesAtCategory = sumOfTransactionsOfType(TransactionType.EXPENSE, List.of(category));
      var budgetRes = BudgetSpendingResult.of(category, expensesAtCategory, budgets.get(category));
      return Optional.of(budgetRes);
    } else {
      return Optional.empty();
    }
  }

  public void addIncome(Money sum, String category) {
    var ts = System.currentTimeMillis();
    var transaction = new Transaction(sum, category, TransactionType.INCOME, ts);
    transactions.add(transaction);
  }

  public void setBudget(String category, Money limit) {
    budgets.put(category, limit);
  }

  public static Wallet create(UUID id) {
    return new Wallet(id, new ArrayList<>(), new HashMap<>());
  }
}
