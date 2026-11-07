package restaurantrush.staff;

/** Automatically processes at most one eligible payment per turn. */
public class Cashier extends Staff {
    public Cashier() { super("Cashier", "Automated Finance Staff"); }
    public void processPayment() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Cashier has already processed payment.");
        }
        useAction();
    }
}
