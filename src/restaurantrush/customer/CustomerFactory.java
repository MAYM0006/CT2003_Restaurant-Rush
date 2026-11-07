package restaurantrush.customer;

public class CustomerFactory {
    public static Customer createCustomerForTurn(int turn) {
        String customerId = "CUST-" + turn;
        switch (turn) {
            // A regular customer at turn 4 showcases the Stage 3 ComboMeal.
            case 4: return new RegularCustomer(customerId, true);
            case 7: return new VIPCustomer(customerId);
            case 13: return new CriticCustomer(customerId);
            default: return new RegularCustomer(customerId);
        }
    }
}
