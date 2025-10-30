package sessionapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import sessionapp.models.Money;
import sessionapp.models.budget.AlmostOverspent;
import sessionapp.models.budget.BudgetOk;
import sessionapp.models.budget.BudgetSpendingResult;
import sessionapp.models.budget.Overspent;

public class BudgetSpendingResultTest {
  @Test
  public void overspent() {
    var limit = Money.of(100_000).get();
    var spent = Money.of(120_000).get();
    var result = BudgetSpendingResult.of("test", spent, limit);
    assertInstanceOf(Overspent.class, result);
    var expectedDiff = Money.of(20_000).get();
    var diff = ((Overspent) result).overspent();
    assertEquals(expectedDiff.getAmount(), diff.getAmount());
  }

  @Test
  public void almostOverspent() {
    var limit = Money.of(100_000).get();
    var spent = Money.of(90_000).get();
    var result = BudgetSpendingResult.of("test", spent, limit);
    assertInstanceOf(AlmostOverspent.class, result);
    var expectedDiff = Money.of(10_000).get();
    var diff = ((AlmostOverspent) result).remaining();
    assertEquals(expectedDiff.getAmount(), diff.getAmount());
  }

  @Test
  public void budgetOk() {
    var limit = Money.of(123_456).get();
    var spent = Money.of(20_000).get();
    var result = BudgetSpendingResult.of("test", spent, limit);
    assertInstanceOf(BudgetOk.class, result);
    var expectedDiff = Money.of(103_456).get();
    var diff = ((BudgetOk) result).remaining();
    assertEquals(expectedDiff.getAmount(), diff.getAmount());
  }
}
