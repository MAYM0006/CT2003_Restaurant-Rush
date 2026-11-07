package restaurantrush.domain;

import restaurantrush.customer.Customer;

public class BillCalculator {
    /** Retain the Stage 2 API for its regression checks and callers. */
    public static int calculateBill(int basePriceCents, Customer customer) {
        return calculateBill(basePriceCents, customer, false);
    }

    /** Combo price is already discounted; apply VIP, then Happy Hour, rounding once. */
    public static int calculateBill(int basePriceCents, Customer customer, boolean isHappyHour) {
        double price = basePriceCents * (1.0 - customer.getDiscountRate());
        if (isHappyHour) {
            price *= 0.80;
        }
        return (int) Math.round(price);
    }
}
