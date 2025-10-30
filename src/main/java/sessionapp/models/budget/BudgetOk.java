package sessionapp.models.budget;

import sessionapp.models.Money;

public record BudgetOk(String category, Money remaining) implements BudgetSpendingResult {}
