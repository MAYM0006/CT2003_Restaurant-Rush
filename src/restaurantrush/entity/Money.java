package restaurantrush.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() {}

    public static String dollars(int cents) {
        BigDecimal amount = new BigDecimal(cents).movePointLeft(2);
        return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static int keepPercent(int cents, int percent) {
        BigDecimal amount = new BigDecimal(cents).multiply(new BigDecimal(percent)).divide(new BigDecimal(100), 0, RoundingMode.HALF_UP);
        return amount.intValueExact();
    }
}