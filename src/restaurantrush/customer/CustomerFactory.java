package restaurantrush.customer;

/** Creates scheduled customer types using a reproducible, deterministic rule. */
public class CustomerFactory {
    /**
     * @param turn scheduled arrival turn
     * @return a customer with a turn-derived identifier and deterministic type
     */
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
