package restaurantrush.customer;

public class CustomerFactory {
    public static Customer createCustomerForTurn(int turn) {
        String customerId = "CUST-" + turn;
        switch (turn) {
            case 7: return new VIPCustomer(customerId);
            case 13: return new CriticCustomer(customerId);
            default: return new RegularCustomer(customerId);
        }
    }
}
