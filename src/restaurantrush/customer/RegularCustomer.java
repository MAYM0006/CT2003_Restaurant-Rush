package restaurantrush.customer;

import restaurantrush.domain.ComboMeal;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;

public class RegularCustomer extends Customer {
    private final boolean prefersCombo;

    public RegularCustomer(String id) { this(id, false); }

    public RegularCustomer(String id, boolean prefersCombo) {
        super(id);
        this.prefersCombo = prefersCombo;
    }

    @Override public int getDecayRate() { return 8; }
    @Override public double getDiscountRate() { return 0.0; }

    @Override
    public Order chooseFood() {
        if (prefersCombo) {
            return new Order(new ComboMeal(MenuItem.SALAD, MenuItem.BURGER));
        }
        return new Order(MenuItem.PASTA);
    }
}
