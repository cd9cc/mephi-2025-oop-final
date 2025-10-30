package sessionapp.utis.printers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import sessionapp.models.Money;
import sessionapp.models.budget.AlmostOverspent;
import sessionapp.models.budget.BudgetOk;
import sessionapp.models.budget.BudgetSpendingResult;
import sessionapp.models.budget.Overspent;

public class PrettyPrinter {
  public static String printMoney(Money money) {
    var res =
        BigDecimal.valueOf(money.getAmount())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    var formatter = NumberFormat.getCurrencyInstance(Locale.of("RU", "RU"));
    return formatter.format(res);
  }

  public static String printMapOfSpendingRes(Map<String, BudgetSpendingResult> map) {
    var sb = new StringBuilder();
    map.forEach(
        (k, v) -> {
          sb.append(k);
          sb.append(": ");
          switch (v) {
            case BudgetOk ok -> {
              sb.append(printMoney(ok.remaining()));
            }
            case AlmostOverspent ao -> {
              sb.append("-");
              sb.append(printMoney(ao.remaining()));
              sb.append(" (бюджет скоро закончится)");
            }
            case Overspent os -> {
              sb.append("-");
              sb.append(printMoney(os.overspent()));
              sb.append(" (бюджет превышен!)");
            }
          }
          sb.append('\n');
        });
    return sb.toString();
  }

  public static String printMapOfMoney(Map<String, Money> map) {
    var sb = new StringBuilder();
    map.forEach(
        (k, v) -> {
          sb.append(k);
          sb.append(": ");
          sb.append(printMoney(v));
          sb.append('\n');
        });
    return sb.toString();
  }

  public static String mkString(Collection<String> collection) {
    return collection.stream().reduce("", (a, b) -> a + ", " + b);
  }
}
