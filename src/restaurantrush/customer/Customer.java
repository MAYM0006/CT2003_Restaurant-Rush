package restaurantrush.customer;

import restaurantrush.domain.Order;

public abstract class Customer {
    private final String id;
    private int satisfaction;
    private CustomerState status;

    public Customer(String id) {
        this.id = id;
        this.satisfaction = 100;
        this.status = CustomerState.ARRIVING;
    }

    public String getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerState getStatus() { return status; }
    public void setStatus(CustomerState status) { this.status = status; }

    public void decaySatisfaction() {
        if (status == CustomerState.WAITING || status == CustomerState.SEATED) {
            this.satisfaction = Math.max(0, this.satisfaction - getDecayRate());
        }
    }

    /** Recover satisfaction for customers still waiting for a table or their food. */
    public void restoreSatisfaction(int amount) {
        if (amount > 0 && (status == CustomerState.WAITING || status == CustomerState.SEATED)) {
            this.satisfaction = Math.min(100, this.satisfaction + amount);
        }
    }

    public abstract int getDecayRate();
    public abstract double getDiscountRate();
    public abstract Order chooseFood();

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Sat: %d | Status: %s", 
                getClass().getSimpleName(), id, satisfaction, status);
    }
}
