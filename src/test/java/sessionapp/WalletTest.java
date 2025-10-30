package sessionapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sessionapp.models.Money;
import sessionapp.models.TransactionType;
import sessionapp.models.Wallet;
import sessionapp.models.budget.AlmostOverspent;
import sessionapp.models.budget.BudgetOk;
import sessionapp.models.budget.Overspent;

public class WalletTest {

  private Wallet wallet;
  private final String testCat = "test";

  @BeforeEach
  public void setup() {
    wallet = Wallet.create(UUID.randomUUID());
  }

  @Test
  public void addIncome() {
    var rand = new Random();
    var totalIncomeExpected = Money.zero();

    for (int i = 0; i < 10; i++) {
      var testIncome = Money.of(rand.nextLong(100, 100_000)).get();
      totalIncomeExpected = totalIncomeExpected.plus(testIncome);
      wallet.addIncome(testIncome, testCat);
    }

    var totalIncomeByCat = wallet.sumOfTransactionsInCategory(TransactionType.INCOME);
    assertEquals(totalIncomeExpected.getAmount(), totalIncomeByCat.get(testCat).getAmount());
  }

  @Test
  public void addExpense() {
    var rand = new Random();
    var totalExpensesExpected = Money.zero();

    for (int i = 0; i < 10; i++) {
      var testIncome = Money.of(rand.nextLong(100, 100_000)).get();
      totalExpensesExpected = totalExpensesExpected.plus(testIncome);
      wallet.addIncome(testIncome, testCat);
    }

    var totalExpenseByCat = wallet.sumOfTransactionsInCategory(TransactionType.INCOME);
    assertEquals(totalExpensesExpected.getAmount(), totalExpenseByCat.get(testCat).getAmount());
  }

  @Test
  public void setBudget() {
    var testSum = Money.of("1234,56").get();
    wallet.setBudget(testCat, testSum);
    var activeBudgets = wallet.budgets();
    assertTrue(activeBudgets.containsKey(testCat));
    assertEquals(activeBudgets.get(testCat).getAmount(), testSum.getAmount());
  }

  @Test
  public void budgetSpendingOk() {
    var testSum = Money.of(30_000).get();
    var testSum1 = Money.of(30_000).get();
    var testLimit = Money.of(100_000).get();
    wallet.setBudget(testCat, testLimit);
    wallet.addExpense(testSum, testCat);
    wallet.addExpense(testSum1, testCat);
    var spending = wallet.budgetSpending(List.of(testCat));
    assertTrue(spending.containsKey(testCat));
    var spendingRes = spending.get(testCat);
    assertInstanceOf(BudgetOk.class, spendingRes);
    var remainingSum = ((BudgetOk) spendingRes).remaining();
    var expectedRemaining = testLimit.minus(testSum1).minus(testSum);
    assertEquals(remainingSum.getAmount(), expectedRemaining.getAmount());
  }

  @Test
  public void budgetSpendingAlmostOverspent() {
    var testSum = Money.of(30_000).get();
    var testSum1 = Money.of(60_000).get();
    var testLimit = Money.of(100_000).get();
    wallet.setBudget(testCat, testLimit);
    wallet.addExpense(testSum, testCat);
    wallet.addExpense(testSum1, testCat);
    var spending = wallet.budgetSpending(List.of(testCat));
    assertTrue(spending.containsKey(testCat));
    var spendingRes = spending.get(testCat);
    assertInstanceOf(AlmostOverspent.class, spendingRes);
    var remainingSum = ((AlmostOverspent) spendingRes).remaining();
    var expectedRemaining = testLimit.minus(testSum1).minus(testSum);
    assertEquals(remainingSum.getAmount(), expectedRemaining.getAmount());
  }

  @Test
  public void budgetSpendingOverspent() {
    var testSum = Money.of(70_000).get();
    var testSum1 = Money.of(60_000).get();
    var testLimit = Money.of(100_000).get();
    wallet.setBudget(testCat, testLimit);
    wallet.addExpense(testSum, testCat);
    wallet.addExpense(testSum1, testCat);
    var spending = wallet.budgetSpending(List.of(testCat));
    assertTrue(spending.containsKey(testCat));
    var spendingRes = spending.get(testCat);
    assertInstanceOf(Overspent.class, spendingRes);
    var overspentSum = ((Overspent) spendingRes).overspent();
    var expectedOverspent = testSum.plus(testSum1).minus(testLimit);
    assertEquals(overspentSum.getAmount(), expectedOverspent.getAmount());
  }
}
