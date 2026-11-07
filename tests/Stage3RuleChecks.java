import restaurantrush.controller.GameController;
import restaurantrush.customer.Customer;
import restaurantrush.customer.CustomerFactory;
import restaurantrush.customer.CustomerState;
import restaurantrush.domain.BillCalculator;
import restaurantrush.domain.ComboMeal;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;
import restaurantrush.exception.InvalidGameActionException;
import restaurantrush.ui.ActionEnums.WaiterAction;
import restaurantrush.ui.ActionEnums.ChefAction;
import restaurantrush.ui.ActionEnums.HostAction;

/** Standalone Stage 3 requirements checks; no external test library required. */
public class Stage3RuleChecks {
    private static final WaiterAction WW = new WaiterAction(WaiterAction.Type.WAIT, 0);
    private static final ChefAction CW = new ChefAction(ChefAction.Type.WAIT, 0);
    private static final HostAction HW = new HostAction(HostAction.Type.WAIT, 0);

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static WaiterAction w(WaiterAction.Type type, int table) { return new WaiterAction(type, table); }
    private static ChefAction c(ChefAction.Type type, int table) { return new ChefAction(type, table); }
    private static HostAction h(HostAction.Type type, int table) { return new HostAction(type, table); }
    private static void turn(GameController game, WaiterAction waiter, ChefAction chef, HostAction host) {
        game.startTurnStep1();
        game.executeRemainingSteps(waiter, chef, host);
    }

    private static void expectInvalid(Runnable operation, String message) {
        try {
            operation.run();
            throw new AssertionError("Expected invalid action: " + message);
        } catch (InvalidGameActionException expected) {
            // No turn should be consumed by the rejected operation.
        }
    }

    public static void main(String[] args) {
        testComboMealSelectionPreparationAndPayment();
        testHostSeatingAndInvalidActionAtomicity();
        testHappyHourOnceAndRecovery();
        testPriceLockingAndDiscountOrder();
        testHappyHourDoesNotRetroactivelyDiscount();
        testHostWaiterChefSameTurn();
        testAbandonmentStates();
        System.out.println("Stage 3 rule checks: PASS (combo, host, Happy Hour, pricing, lifecycle)");
    }

    private static void testComboMealSelectionPreparationAndPayment() {
        Customer regular = CustomerFactory.createCustomerForTurn(4);
        check(regular.chooseFood().getSelection() instanceof ComboMeal, "Turn 4 regular must choose a combo");
        ComboMeal combo = (ComboMeal) regular.chooseFood().getSelection();
        check(combo.getPriceCents() == 2160, "Salad+Burger combo must cost 2160 cents");
        check(combo.getRequiredPrepUnits() == 2, "Salad+Burger combo requires two prep units");
        check(BillCalculator.calculateBill(combo.getPriceCents(), regular, true) == 1728,
                "Combo price must receive the additional Happy Hour discount");
        check(new ComboMeal(MenuItem.SALAD, MenuItem.PASTA).getPriceCents() == 2430,
                "Salad+Pasta combo should cost 2430 cents");
        check(new ComboMeal(MenuItem.SALAD, MenuItem.PASTA).getRequiredPrepUnits() == 3,
                "Salad+Pasta combo requires three prep units");
        check(CustomerFactory.createCustomerForTurn(1).chooseFood().getMenuItem() == MenuItem.PASTA,
                "Other regular customers should still choose Pasta");

        GameController game = new GameController();
        turn(game, WW, CW, HW); // 1
        turn(game, WW, CW, HW); // 2
        turn(game, WW, CW, HW); // 3
        turn(game, w(WaiterAction.Type.SEAT, 1), CW, HW); // 4: seat oldest CUST-1
        turn(game, WW, CW, h(HostAction.Type.SEAT, 2)); // 5: host seats CUST-4
        turn(game, w(WaiterAction.Type.TAKE_ORDER, 2), c(ChefAction.Type.COOK, 2), HW); // 6
        Order placed = game.getTables().get(1).getOrder();
        check(placed.getSelection() instanceof ComboMeal, "Actual order must hold ComboMeal");
        check(placed.getStatus() == Order.Status.PREPARING && placed.getUnitsDone() == 1,
                "Combo should need more than one Chef action");
        check(placed.getBillCents() == 2160, "Combo bill must be locked when ordered");
        turn(game, WW, c(ChefAction.Type.COOK, 2), HW); // 7
        check(placed.isReady(), "Combo becomes READY after two prep units");
        turn(game, w(WaiterAction.Type.SERVE, 2), CW, HW); // 8
        check(placed.getStatus() == Order.Status.SERVED, "Combo must be served");
        turn(game, WW, CW, HW); // 9: ready to pay
        check(game.getPaidCustomers().isEmpty(), "No same-turn payment");
        turn(game, WW, CW, HW); // 10: pay
        check(placed.getStatus() == Order.Status.PAID, "Combo must be payable");
        check(game.getTotalRevenueDollars() == 21.60, "Combo payment must generate $21.60");
    }

    private static void testHostSeatingAndInvalidActionAtomicity() {
        GameController game = new GameController();
        for (int t = 1; t <= 3; t++) turn(game, WW, CW, HW);
        game.startTurnStep1(); // Turn 4: two waiting customers
        check(game.getWaitingQueue().size() == 2, "Two waiting customers expected by turn 4");
        expectInvalid(() -> game.executeRemainingSteps(
                w(WaiterAction.Type.SEAT, 1), CW, h(HostAction.Type.SEAT, 1)),
                "Host and Waiter cannot seat the same table");
        check(game.getCurrentTurn() == 4 && game.getWaitingQueue().size() == 2,
                "Rejected action must not advance turn or change the queue");
        game.executeRemainingSteps(w(WaiterAction.Type.SEAT, 2), CW, h(HostAction.Type.SEAT, 1));
        check(game.getTables().get(0).isOccupied() && game.getTables().get(1).isOccupied(),
                "Host and Waiter should seat two customers in one turn");
        check(game.getWaitingQueue().isEmpty(), "Both queued customers should be seated");
    }

    private static void testHappyHourOnceAndRecovery() {
        GameController game = new GameController();
        turn(game, WW, CW, HW); // Turn 1: CUST-1 waiting, loses 8
        check(game.getWaitingQueue().peek().getSatisfaction() == 92, "Waiting decay must apply");
        game.activateHappyHour(); // Beginning of turn 2
        check(game.isHappyHourUsed() && game.canActivateHappyHour() == false, "Happy Hour marked used");
        game.startTurnStep1();
        check(game.getWaitingQueue().peek().getSatisfaction() == 100,
                "Activation turn must restore 15 before actions");
        check(game.getHappyHourTurnsRemaining() == 2, "Initially two active turns");
        expectInvalid(game::activateHappyHour, "Cannot reactivate mid-turn");
        game.executeRemainingSteps(WW, CW, HW);
        check(game.getHappyHourTurnsRemaining() == 1, "One active turn remains");
        game.startTurnStep1(); // Turn 3
        check(game.getWaitingQueue().peek().getSatisfaction() == 100,
                "Second active turn must restore 15 before actions");
        game.executeRemainingSteps(WW, CW, HW);
        check(!game.isHappyHourActive() && game.getHappyHourTurnsRemaining() == 0,
                "Happy Hour ends after exactly two turns");
        expectInvalid(game::activateHappyHour, "Cannot reactivate after expiry");
        game.startTurnStep1(); // Turn 4
        check(game.getWaitingQueue().peek().getSatisfaction() == 92,
                "No satisfaction recovery after Happy Hour expires");
        game.executeRemainingSteps(WW, CW, HW);

        // Seated customers still waiting for food recover, paid customers do not.
        GameController seated = new GameController();
        turn(seated, w(WaiterAction.Type.SEAT, 1), CW, HW);
        Customer customer = seated.getTables().get(0).getCustomer();
        check(customer.getSatisfaction() == 92, "Seated customer loses satisfaction");
        seated.activateHappyHour();
        seated.startTurnStep1();
        check(customer.getSatisfaction() == 100, "Seated customer must recover on activation turn");
        seated.executeRemainingSteps(WW, CW, HW);
    }

    private static void testPriceLockingAndDiscountOrder() {
        Customer vip = CustomerFactory.createCustomerForTurn(7);
        check(BillCalculator.calculateBill(1500, vip, true) == 1080,
                "VIP $15 Burger + Happy Hour should be $10.80");
        check(BillCalculator.calculateBill(2160, vip, true) == 1555,
                "Combo, then VIP and Happy Hour discounts: 2160 * .9 * .8 rounded = 1555");
        check(BillCalculator.calculateBill(1500, vip) == 1350,
                "Stage 2 billing overload must still work");
        Order immutable = new Order(MenuItem.BURGER);
        immutable.lockBill(1080, true);
        try {
            immutable.lockBill(1500, false);
            throw new AssertionError("A locked bill should not be overwritten");
        } catch (IllegalStateException expected) { }
        check(immutable.getBillCents() == 1080 && immutable.isHappyHourDiscountApplied(),
                "Bill and Happy Hour eligibility remain locked");

        GameController game = new GameController();
        turn(game, w(WaiterAction.Type.SEAT, 1), CW, HW); // 1
        game.activateHappyHour();
        turn(game, w(WaiterAction.Type.TAKE_ORDER, 1), c(ChefAction.Type.COOK, 1), HW); // 2
        Order placed = game.getTables().get(0).getOrder();
        check(placed.getBillCents() == 1440 && placed.isHappyHourDiscountApplied(),
                "Order taken during Happy Hour is immediately locked at $14.40");
        turn(game, WW, c(ChefAction.Type.COOK, 1), HW); // 3, HH expires
        turn(game, w(WaiterAction.Type.SERVE, 1), CW, HW); // 4
        turn(game, WW, CW, HW); // 5
        turn(game, WW, CW, HW); // 6: payment
        check(placed.getStatus() == Order.Status.PAID && game.getTotalRevenueDollars() == 14.40,
                "Payment after expiry uses the previously locked Happy Hour price");
    }

    private static void testHappyHourDoesNotRetroactivelyDiscount() {
        GameController game = new GameController();
        turn(game, w(WaiterAction.Type.SEAT, 1), CW, HW); // 1
        turn(game, w(WaiterAction.Type.TAKE_ORDER, 1), c(ChefAction.Type.COOK, 1), HW); // 2
        Order before = game.getTables().get(0).getOrder();
        check(before.getBillCents() == 1800 && !before.isHappyHourDiscountApplied(),
                "Order outside Happy Hour locked at full price");
        turn(game, WW, c(ChefAction.Type.COOK, 1), HW); // 3
        turn(game, w(WaiterAction.Type.SERVE, 1), CW, HW); // 4
        turn(game, WW, CW, HW); // 5: customer becomes READY_TO_PAY
        game.activateHappyHour(); // Before turn 6, the exact turn the order is paid
        turn(game, WW, CW, HW); // 6
        check(before.getBillCents() == 1800, "Activating Happy Hour cannot reprice existing orders");
        check(before.getStatus() == Order.Status.PAID && game.getTotalRevenueDollars() == 18.0,
                "Earlier order paid during Happy Hour must retain its original full price");
    }
    private static void testHostWaiterChefSameTurn() {
        GameController game = new GameController();
        game.startTurnStep1();
        game.executeRemainingSteps(w(WaiterAction.Type.TAKE_ORDER, 1),
                c(ChefAction.Type.COOK, 1), h(HostAction.Type.SEAT, 1));
        Order placed = game.getOrderPlacedThisTurn();
        check(placed != null && placed.getName().equals("Pasta"),
                "Host seating must let Waiter order in the same turn");
        check(placed.getUnitsDone() == 1 && game.getTables().get(0).isOccupied(),
                "Chef must be able to prepare that newly taken order in the same turn");
    }

    private static void testAbandonmentStates() {
        GameController waiting = new GameController();
        turn(waiting, WW, CW, HW); // CUST-1 begins waiting
        Customer waitCustomer = waiting.getWaitingQueue().peek();
        for (int i = 2; i <= 13; i++) turn(waiting, WW, CW, HW);
        check(waitCustomer.getStatus() == CustomerState.ABANDONED,
                "Unseated customer must explicitly enter ABANDONED state");
        check(waiting.getAbandonedCustomers().contains(waitCustomer),
                "Abandoned waiting customer must be recorded");

        GameController seated = new GameController();
        turn(seated, w(WaiterAction.Type.SEAT, 1), CW, HW);
        Customer seatCustomer = seated.getTables().get(0).getCustomer();
        turn(seated, w(WaiterAction.Type.TAKE_ORDER, 1), CW, HW);
        Order unfinished = seated.getTables().get(0).getOrder();
        for (int i = 3; i <= 13; i++) turn(seated, WW, CW, HW);
        check(seatCustomer.getStatus() == CustomerState.ABANDONED,
                "Seated customer must explicitly enter ABANDONED state");
        check(unfinished.getStatus() == Order.Status.CANCELLED,
                "Abandoned customer must leave order cancelled");
        check(!seated.getTables().get(0).isOccupied(),
                "Table must be freed on abandonment");
    }

}
