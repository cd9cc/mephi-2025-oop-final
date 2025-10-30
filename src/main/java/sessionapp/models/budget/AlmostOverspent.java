package sessionapp.models.budget;

import sessionapp.models.Money;

public record AlmostOverspent(String category, Money remaining) implements BudgetSpendingResult {}
