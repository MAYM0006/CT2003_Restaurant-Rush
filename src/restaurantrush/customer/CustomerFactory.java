package restaurantrush.customer;

public class CustomerFactory {
    public static Customer createCustomerForTurn(int turn) {
        return new RegularCustomer("CUST-" + turn);
    }
}
