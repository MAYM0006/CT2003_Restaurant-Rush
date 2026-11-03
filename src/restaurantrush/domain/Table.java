package restaurantrush.domain;

import restaurantrush.customer.Customer;

public class Table {
    private final int tableNumber;
    private Customer occupiedBy;
    private Order order;

    public Table(int tableNumber) {
        this.tableNumber = tableNumber;
        this.occupiedBy = null;
        this.order = null;
    }

    public int getTableNumber() { return tableNumber; }
    public Customer getCustomer() { return occupiedBy; }
    public Order getOrder() { return order; }
    public boolean isOccupied() { return occupiedBy != null; }

    public void assignCustomer(Customer customer) { this.occupiedBy = customer; }
    public void assignOrder(Order order) { this.order = order; }
    public void clearTable() {
        this.occupiedBy = null;
        this.order = null;
    }
}
