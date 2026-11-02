package restaurantrush.staff;

public class Cashier extends Staff {
    public Cashier() { super("Cashier", "Automated Finance Staff"); }
    public void processPayment() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Cashier has already processed payment.");
        }
        useAction();
    }
}
