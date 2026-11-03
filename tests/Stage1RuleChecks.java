import restaurantrush.controller.GameController;
import restaurantrush.customer.Customer;
import restaurantrush.customer.CustomerState;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;
import restaurantrush.exception.InvalidGameActionException;
import restaurantrush.ui.ActionEnums.ChefAction;
import restaurantrush.ui.ActionEnums.WaiterAction;

/** Standalone deterministic requirements checks, without external test libraries. */
public class Stage1RuleChecks {
    private static final WaiterAction WAITER_WAIT = new WaiterAction(WaiterAction.Type.WAIT, 0);
    private static final ChefAction CHEF_WAIT = new ChefAction(ChefAction.Type.WAIT, 0);

    public static void main(String[] args) {
        testPaymentDelayAndOrderStatus();
        testDuplicateServeAndOvercookingRejected();
        testAbandonmentCancelsUnpaidOrder();
        System.out.println("Stage 1 rule checks: PASS (3 scenarios)");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void advance(GameController game, WaiterAction.Type waiter, int waiterTable,
                                ChefAction.Type chef, int chefTable) {
        game.executeRemainingSteps(new WaiterAction(waiter, waiterTable),
                                   new ChefAction(chef, chefTable));
    }

    private static GameController setupReadyPasta() {
        GameController game = new GameController();
        game.startTurnStep1(); // Turn 1: seat
        advance(game, WaiterAction.Type.SEAT, 1, ChefAction.Type.WAIT, 0);
        game.startTurnStep1(); // Turn 2: order and cook 1/2
        advance(game, WaiterAction.Type.TAKE_ORDER, 1, ChefAction.Type.COOK, 1);
        game.startTurnStep1(); // Turn 3: cook 2/2 -> READY
        advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.COOK, 1);
        check(game.getTables().get(0).getOrder().getStatus() == Order.Status.READY,
              "Pasta should become READY after two preparation units");
        return game; // Turn 4 is next
    }

    private static void testPaymentDelayAndOrderStatus() {
        GameController game = setupReadyPasta();
        game.startTurnStep1(); // Turn 4: serve
        Table table = game.getTables().get(0);
        Customer customer = table.getCustomer();
        Order order = table.getOrder();
        advance(game, WaiterAction.Type.SERVE, 1, ChefAction.Type.WAIT, 0);
        check(customer.getStatus() == CustomerState.EATING, "Turn 4 should be EATING");
        check(order.getStatus() == Order.Status.SERVED, "Order should be SERVED");
        check(game.getPaidCustomers().isEmpty(), "Cannot pay on serving turn");
        game.startTurnStep1(); // Turn 5: eating completes
        advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.WAIT, 0);
        check(customer.getStatus() == CustomerState.READY_TO_PAY, "Turn 5 should be READY_TO_PAY");
        check(game.getPaidCustomers().isEmpty(), "Cannot pay in READY_TO_PAY transition turn");
        game.startTurnStep1(); // Turn 6: payment
        advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.WAIT, 0);
        check(customer.getStatus() == CustomerState.PAID, "Customer should be PAID on turn 6");
        check(order.getStatus() == Order.Status.PAID, "Order should also be PAID");
        check(!table.isOccupied(), "Table should be freed after payment");
        check(game.getTotalRevenueDollars() == 18.0, "Pasta payment should add $18");
    }

    private static void testDuplicateServeAndOvercookingRejected() {
        GameController game = setupReadyPasta();
        game.startTurnStep1(); // Turn 4
        try {
            advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.COOK, 1);
            throw new AssertionError("Cooking a READY order should be rejected");
        } catch (InvalidGameActionException expected) {
            check(game.getCurrentTurn() == 4, "Rejected cooking must not advance turn");
        }
        advance(game, WaiterAction.Type.SERVE, 1, ChefAction.Type.WAIT, 0);
        game.startTurnStep1(); // Turn 5
        try {
            advance(game, WaiterAction.Type.SERVE, 1, ChefAction.Type.WAIT, 0);
            throw new AssertionError("Serving the same order again should be rejected");
        } catch (InvalidGameActionException expected) {
            check(game.getCurrentTurn() == 5, "Rejected serving must not advance turn");
            check(game.getServedCount() == 1, "Rejected serving must not increment served count");
            check(game.getTables().get(0).getCustomer().getStatus() == CustomerState.EATING,
                  "Rejected serving must not change customer state");
        }
        advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.WAIT, 0);
    }

    private static void testAbandonmentCancelsUnpaidOrder() {
        GameController game = new GameController();
        game.startTurnStep1(); // Turn 1: seat
        advance(game, WaiterAction.Type.SEAT, 1, ChefAction.Type.WAIT, 0);
        game.startTurnStep1(); // Turn 2: take order, never cook
        advance(game, WaiterAction.Type.TAKE_ORDER, 1, ChefAction.Type.WAIT, 0);
        Order order = game.getTables().get(0).getOrder();
        for (int turn = 3; turn <= 13; turn++) {
            game.startTurnStep1();
            advance(game, WaiterAction.Type.WAIT, 0, ChefAction.Type.WAIT, 0);
        }
        check(order.getStatus() == Order.Status.CANCELLED, "Abandonment should cancel unfinished order");
        check(!game.getTables().get(0).isOccupied(), "Abandoned customer frees table");
        check(game.getTotalRevenueDollars() == 0.0, "Abandoned order must not earn revenue");
    }
}
