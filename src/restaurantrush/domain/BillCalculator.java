package restaurantrush.domain;

import restaurantrush.customer.Customer;

public class BillCalculator {
    public static int calculateBill(int basePriceCents, Customer customer) {
        // Stage 2: use polymorphism to apply customer-specific discounts.
        // Calculations stay in cents, rounded to the nearest whole cent.
        return (int) Math.round(basePriceCents * (1.0 - customer.getDiscountRate()));
    }
}
