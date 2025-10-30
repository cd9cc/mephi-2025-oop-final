package sessionapp.models.budget;

import sessionapp.models.Money;

public sealed interface BudgetSpendingResult permits BudgetOk, AlmostOverspent, Overspent {
  String category();

  static BudgetSpendingResult of(String category, Money spent, Money budget) {
    return of(category, spent, budget, 0.8f);
  }

  static BudgetSpendingResult of(String category, Money spent, Money budget, Float alertThreshold) {
    if (spent.isGreater(budget)) {
      var diff = spent.minus(budget);
      return new Overspent(category, diff);
    } else {
      var remaining = budget.minus(spent);
      var rate = 1f - ((float) remaining.getAmount() / budget.getAmount());
      if (rate >= alertThreshold) {
        return new AlmostOverspent(category, remaining);
      } else {
        return new BudgetOk(category, remaining);
      }
    }
  }
}
