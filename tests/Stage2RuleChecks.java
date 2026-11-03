import restaurantrush.customer.Customer;
import restaurantrush.customer.CustomerFactory;
import restaurantrush.customer.CustomerState;
import restaurantrush.customer.CriticCustomer;
import restaurantrush.customer.RegularCustomer;
import restaurantrush.customer.VIPCustomer;
import restaurantrush.domain.BillCalculator;
import restaurantrush.domain.MenuItem;

/** Deterministic checks for the required Stage 2 customer behaviours. */
public class Stage2RuleChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        for (int turn : new int[] {1, 4, 10, 16}) {
            check(CustomerFactory.createCustomerForTurn(turn) instanceof RegularCustomer,
                    "Expected Regular customer at turn " + turn);
        }

        Customer vip = CustomerFactory.createCustomerForTurn(7);
        check(vip instanceof VIPCustomer, "Turn 7 should create VIPCustomer");
        check(vip.getId().equals("CUST-7"), "VIP ID mismatch");
        check(vip.getDecayRate() == 5, "VIP decay must be 5");
        check(vip.getDiscountRate() == 0.10, "VIP discount must be 10%");
        check(vip.chooseFood().getMenuItem() == MenuItem.BURGER, "VIP should choose Burger");
        check(BillCalculator.calculateBill(1500, vip) == 1350, "VIP Burger should cost 1350 cents");
        vip.setStatus(CustomerState.WAITING);
        vip.decaySatisfaction();
        check(vip.getSatisfaction() == 95, "VIP should lose 5 satisfaction");

        Customer critic = CustomerFactory.createCustomerForTurn(13);
        check(critic instanceof CriticCustomer, "Turn 13 should create CriticCustomer");
        check(critic.getDecayRate() == 12, "Critic decay must be 12");
        check(critic.getDiscountRate() == 0.0, "Critic pays full price");
        check(critic.chooseFood().getMenuItem() == MenuItem.SALAD, "Critic should choose Salad");
        check(BillCalculator.calculateBill(900, critic) == 900, "Critic Salad should cost 900 cents");
        critic.setStatus(CustomerState.WAITING);
        critic.decaySatisfaction();
        check(critic.getSatisfaction() == 88, "Critic should lose 12 satisfaction");

        Customer regular = CustomerFactory.createCustomerForTurn(1);
        check(regular.getDecayRate() == 8, "Regular decay must remain 8");
        check(regular.getDiscountRate() == 0.0, "Regular pays full price");
        check(BillCalculator.calculateBill(1800, regular) == 1800,
                "Regular Pasta should cost 1800 cents");

        System.out.println("Stage 2 rule checks: PASS (factory, polymorphism, discounts, preferences)");
    }
}
