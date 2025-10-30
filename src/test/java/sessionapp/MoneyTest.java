package sessionapp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sessionapp.models.Money;

public class MoneyTest {

  @Test
  public void testParseStringWithDecimals() {
    var expect = Money.of(12345L);
    var result = Money.of("123,45");
    assertEquals(expect.get().getAmount(), result.get().getAmount());
  }

  @Test
  public void testParseStringWithDecimals2() {
    var expect = Money.of(12345L);
    var result = Money.of("123.45");
    assertEquals(expect.get().getAmount(), result.get().getAmount());
  }

  @Test
  public void testParseStringWithDecimals3() {
    var result = Money.of("1,123.45");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseStringWithDecimals4() {
    var expect = Money.of(55L);
    var result = Money.of("0.55");
    assertEquals(expect.get().getAmount(), result.get().getAmount());
  }

  @Test
  public void testParseStringWithExceedingDecimals() {
    var result = Money.of("123,456");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testParseStringWithoutDecimals() {
    var expect = Money.of(12300L);
    var result = Money.of("123");
    assertEquals(expect.get().getAmount(), result.get().getAmount());
  }

  @Test
  public void testParseStringWithoutDecimals2() {
    var result = Money.of("1,123");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testPlusFailsOnOverflow() {
    var a = Money.of(Long.MAX_VALUE).get();
    var b = Money.of(12345L).get();
    assertThrows(
        ArithmeticException.class,
        () -> {
          a.plus(b);
        });
  }

  @Test
  public void testFailsOnNegAmount() {
    var result = Money.of(-100L);
    assertTrue(result.isEmpty());
  }
}
