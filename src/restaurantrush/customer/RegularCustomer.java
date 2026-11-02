package restaurantrush.customer;

import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;

public class RegularCustomer extends Customer {
    public RegularCustomer(String id) { super(id); }
    @Override public int getDecayRate() { return 8; }
    @Override public double getDiscountRate() { return 0.0; }
    @Override public Order chooseFood() { return new Order(MenuItem.PASTA); }
}
