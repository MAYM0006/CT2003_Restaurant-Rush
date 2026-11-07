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

// 1 Nov: Could not compile because GameController.java calls c.restoreSatsifaction(15) but Customer.java does not define a restoreSatsifaction() method. This is the fix
public void restoreSatisfaction(int amount) {
    if (status == CustomerState.WAITING || status == CustomerState.SEATED) {
        this.satisfaction = Math.min(100, this.satisfaction + amount); //This is to ensure in the case of HappyHour, we do not have a satisfaction rate exceeding 100.
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
