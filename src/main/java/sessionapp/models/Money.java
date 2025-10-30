package sessionapp.models;

import java.math.BigDecimal;
import java.util.Optional;
import sessionapp.exceptions.NegativeMoneyException;

/** для простоты работаем только с рублями */
public final class Money {
  private long amount;

  private Money() {}

  private Money(long amount) {
    this.amount = amount;
  }

  public long getAmount() {
    return amount;
  }

  public Money plus(Money other) {
    var newAmount = Math.addExact(amount, other.amount);
    return new Money(newAmount);
  }

  public Money minus(Money other) {
    if (amount < other.amount)
      throw new NegativeMoneyException("negative amount in Money is forbidden");
    var newAmount = Math.subtractExact(amount, other.amount);
    return new Money(newAmount);
  }

  public boolean isGreater(Money other) {
    return amount > other.amount;
  }

  public boolean isZero() {
    return amount == 0;
  }

  public static Money zero() {
    return new Money(0L);
  }

  public static Optional<Money> of(String s) {
    try {
      var normalizedS = s.replace(',', '.');
      var bd = new BigDecimal(normalizedS).multiply(BigDecimal.valueOf(100));
      return Optional.of(new Money(bd.longValueExact()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static Optional<Money> of(long l) {
    if (l >= 0L) {
      return Optional.of(new Money(l));
    } else {
      return Optional.empty();
    }
  }
}
