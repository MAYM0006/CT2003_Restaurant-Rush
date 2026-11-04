package restaurantrush.domain;

import restaurantrush.customer.Customer;

public class BillCalculator {
    public static int calculateBill(int basePriceCents, Customer customer) {
        double currentPrice = basePriceCents;
        if (customer.getDiscountRate() > 0) {
            currentPrice = currentPrice * (1.0 - customer.getDiscountRate());
        }
        return (int) Math.round(currentPrice);
    }
}