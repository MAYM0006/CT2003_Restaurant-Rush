package restaurantrush.ui;

import restaurantrush.controller.GameController;
import restaurantrush.customer.Customer;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;
import restaurantrush.exception.InvalidGameActionException;
import restaurantrush.ui.ActionEnums.WaiterAction;
import restaurantrush.ui.ActionEnums.ChefAction;
import restaurantrush.ui.ActionEnums.HostAction;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Command-line interface for collecting manager choices and presenting game state.
 * The controller is responsible for all gameplay rules and validation.
 */
public class RestaurantRushCLI {
    /**
     * Launches an 18-turn interactive game using standard input and output.
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        GameController controller = new GameController();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("  WELCOME TO CT2003 RESTAURANT RUSH (STAGE 3)     ");
        System.out.println("==================================================");

        while (controller.getCurrentTurn() <= GameController.MAX_TURNS) {
            // Happy Hour is a once-per-game decision at the start of a turn.
            // Ask BEFORE startTurnStep1 so recovery applies on the activation turn.
            if (!promptHappyHour(scanner, controller)) return;
            controller.startTurnStep1();
            displayStartOfTurn(controller);

            boolean validInputs = false;
            while (!validInputs) {
                try {
                    System.out.println("\n--- ENTER MANAGER CHOICES FOR TURN " + controller.getCurrentTurn() + " ---");

                    System.out.print("Host Action [0: Wait, 1: Seat Customer]: ");
                    int hostOpt = Integer.parseInt(scanner.nextLine().trim());
                    if (hostOpt != 0 && hostOpt != 1) {
                        throw new InvalidGameActionException("Host action must be 0 or 1.");
                    }
                    HostAction hostChoice = new HostAction(HostAction.Type.WAIT, 0);
                    if (hostOpt == 1) {
                        System.out.print("Select Table (1 or 2): ");
                        int tNum = Integer.parseInt(scanner.nextLine().trim());
                        hostChoice = new HostAction(HostAction.Type.SEAT, tNum);
                    }

                    System.out.print("Waiter Action [0: Wait, 1: Seat Customer, 2: Take Order, 3: Serve Order]: ");
                    int waiterOpt = Integer.parseInt(scanner.nextLine().trim());
                    if (waiterOpt < 0 || waiterOpt > 3) {
                        throw new InvalidGameActionException("Waiter action must be 0, 1, 2 or 3.");
                    }
                    WaiterAction waiterChoice = new WaiterAction(WaiterAction.Type.WAIT, 0);
                    if (waiterOpt > 0) {
                        System.out.print("Select Table (1 or 2): ");
                        int tNum = Integer.parseInt(scanner.nextLine().trim());
                        WaiterAction.Type type = WaiterAction.Type.WAIT;
                        if (waiterOpt == 1) type = WaiterAction.Type.SEAT;
                        else if (waiterOpt == 2) type = WaiterAction.Type.TAKE_ORDER;
                        else if (waiterOpt == 3) type = WaiterAction.Type.SERVE;
                        waiterChoice = new WaiterAction(type, tNum);
                    }

                    System.out.print("Chef Action [0: Wait, 1: Cook Order]: ");
                    int chefOpt = Integer.parseInt(scanner.nextLine().trim());
                    if (chefOpt != 0 && chefOpt != 1) {
                        throw new InvalidGameActionException("Chef action must be 0 or 1.");
                    }
                    ChefAction chefChoice = new ChefAction(ChefAction.Type.WAIT, 0);
                    if (chefOpt == 1) {
                        System.out.print("Select Table (1 or 2): ");
                        int tNum = Integer.parseInt(scanner.nextLine().trim());
                        chefChoice = new ChefAction(ChefAction.Type.COOK, tNum);
                    }

                    controller.executeRemainingSteps(waiterChoice, chefChoice, hostChoice);
                    // The Controller records the actual order chosen by the customer.
                    // Show it in the same turn, even if that customer subsequently abandons.
                    Order newlyPlaced = controller.getOrderPlacedThisTurn();
                    if (newlyPlaced != null) {
                        System.out.printf("Order placed at Table %d: %s (Prep units: %d, Bill: $%.2f)%n",
                                waiterChoice.getTableNumber(), newlyPlaced.getName(),
                                newlyPlaced.getRequiredPrepUnits(), newlyPlaced.getBillCents() / 100.0);
                    }
                    System.out.printf("Turn %d complete | Served: %d | Unhappy departures: %d | Revenue: $%.2f | Avg satisfaction: %s%n",
                            controller.getCurrentTurn() - 1,
                            controller.getServedCount(),
                            controller.getAbandonedCustomers().size(),
                            controller.getTotalRevenueDollars(),
                            controller.getPaidCustomers().isEmpty() ? "N/A" : String.format("%.1f", controller.getAverageSatisfaction()));
                    validInputs = true;
                } catch (InvalidGameActionException e) {
                    System.out.println("\n[INVALID MOVE REJECTED]: " + e.getMessage());
                    System.out.println("Turn capacity retained. Please re-enter choices.");
                } catch (NoSuchElementException e) {
                    System.err.println("\n[ERROR]: Input ended before the game was complete.");
                    return;
                } catch (NumberFormatException e) {
                    System.out.println("\n[ERROR]: Invalid input format. Please enter numeric choices.");
                }
            }
        }

        displayEndGameResults(controller);
    }

    private static boolean promptHappyHour(Scanner scanner, GameController controller) {
        if (!controller.canActivateHappyHour()) return true;
        while (true) {
            System.out.print("Activate Happy Hour at start of turn " + controller.getCurrentTurn()
                    + "? [0: No, 1: Yes (once per game, 2 turns)]: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) return true;
                if (choice == 1) {
                    controller.activateHappyHour();
                    return true;
                }
                System.out.println("[ERROR]: Enter 0 or 1.");
            } catch (NoSuchElementException e) {
                System.err.println("\n[ERROR]: Input ended before the game was complete.");
                return false;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR]: Invalid input format. Please enter 0 or 1.");
            } catch (InvalidGameActionException e) {
                System.out.println("[INVALID MOVE REJECTED]: " + e.getMessage());
            }
        }
    }

    private static void displayStartOfTurn(GameController controller) {
        System.out.println("\n==================================================");
        System.out.printf(" TURN %d / %d | Revenue: $%.2f%n",
                controller.getCurrentTurn(), GameController.MAX_TURNS, controller.getTotalRevenueDollars());
        System.out.printf(" Served: %d | Unhappy departures: %d | Paid: %d | Avg Satisfaction: %s%n",
                controller.getServedCount(),
                controller.getAbandonedCustomers().size(),
                controller.getPaidCustomers().size(),
                controller.getPaidCustomers().isEmpty() ? "N/A" : String.format("%.1f", controller.getAverageSatisfaction()));
        if (controller.isHappyHourActive()) {
            System.out.println(" Happy Hour ACTIVE (" + controller.getHappyHourTurnsRemaining() + " turn(s) remaining)");
        } else if (controller.isHappyHourUsed()) {
            System.out.println(" Happy Hour already used");
        }
        System.out.println("==================================================");

        System.out.println("Waiting Queue (" + controller.getWaitingQueue().size() + "):");
        for (Customer c : controller.getWaitingQueue()) {
            System.out.println("  -> " + c);
        }

        System.out.println("Tables:");
        for (Table t : controller.getTables()) {
            if (!t.isOccupied()) {
                System.out.println("  Table " + t.getTableNumber() + ": [EMPTY]");
            } else {
                Customer c = t.getCustomer();
                Order o = t.getOrder();
                String orderInfo = (o == null) ? "No Order Placed" :
                        String.format("%s [%s] (Prep: %d/%d, Bill: $%.2f)",
                                o.getName(), o.getStatus(), o.getUnitsDone(),
                                o.getRequiredPrepUnits(), o.getBillCents() / 100.0);
                System.out.printf("  Table %d: %s | Order: %s%n", t.getTableNumber(), c, orderInfo);
            }
        }
    }

    private static void displayEndGameResults(GameController controller) {
        System.out.println("\n==================================================");
        System.out.println("               GAME OVER RESULTS                  ");
        System.out.println("==================================================");
        System.out.printf(" Total Revenue: $%.2f (Target: $45.00)%n", controller.getTotalRevenueDollars());
        System.out.printf(" Paid Customers: %d (Target: 4)%n", controller.getPaidCustomers().size());
        System.out.printf(" Average Satisfaction: %s (Target: 60.0)%n",
                controller.getPaidCustomers().isEmpty()
                        ? "N/A"
                        : String.format("%.1f", controller.getAverageSatisfaction()));
        System.out.printf(" Abandoned Customers: %d%n", controller.getAbandonedCustomers().size());
        System.out.println("--------------------------------------------------");
        if (controller.isVictory()) {
            System.out.println(" >>> VICTORY! All targets achieved! <<<");
        } else {
            System.out.println(" >>> DEFEAT! Failed to meet target metrics. <<<");
        }
        System.out.println("==================================================");
    }
}
