package sessionapp.models.budget;

import sessionapp.models.Money;

public record Overspent(String category, Money overspent) implements BudgetSpendingResult {}
