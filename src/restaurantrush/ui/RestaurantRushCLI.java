package restaurantrush.ui;

import restaurantrush.controller.GameController;
import restaurantrush.customer.Customer;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;
import restaurantrush.exception.InvalidGameActionException;

import restaurantrush.ui.ActionEnums.WaiterAction;
import restaurantrush.ui.ActionEnums.ChefAction;
import restaurantrush.ui.ActionEnums.HostAction;

import java.util.Scanner;


public class RestaurantRushCLI {
    public static void main(String[] args) {
        GameController controller = new GameController();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("  WELCOME TO CT2003 RESTAURANT RUSH (STAGE 3)     ");
        System.out.println("==================================================");

        while (controller.getCurrentTurn() <= GameController.MAX_TURNS) {
            controller.startTurnStep1();
            displayStartOfTurn(controller);

            boolean validInputs = false;
            WaiterAction waiterChoice = new WaiterAction(WaiterAction.Type.WAIT, 0);
            ChefAction chefChoice = new ChefAction(ChefAction.Type.WAIT, 0);

            while (!validInputs) {
                try {
                    System.out.println("\n--- ENTER MANAGER CHOICES FOR TURN " + controller.getCurrentTurn() + " ---");
                    
                    System.out.print("Waiter Action [0: Wait, 1: Seat Customer, 2: Take Order, 3: Serve Order]: ");
                    int waiterOpt = Integer.parseInt(scanner.nextLine().trim());
                    if (waiterOpt < 0 || waiterOpt > 3) {
                        throw new InvalidGameActionException("Waiter action must be 0, 1, 2 or 3.");
                    }
                    if (waiterOpt > 0) {
                        System.out.print("Select Table (1 or 2): ");
                        int tNum = Integer.parseInt(scanner.nextLine().trim());
                        WaiterAction.Type wType = WaiterAction.Type.WAIT;
                        if (waiterOpt == 1) wType = WaiterAction.Type.SEAT;
                        else if (waiterOpt == 2) wType = WaiterAction.Type.TAKE_ORDER;
                        else if (waiterOpt == 3) wType = WaiterAction.Type.SERVE;
                        waiterChoice = new WaiterAction(wType, tNum);
                    } else {
                        waiterChoice = new WaiterAction(WaiterAction.Type.WAIT, 0);
                    }

                    System.out.print("Chef Action [0: Wait, 1: Cook Order]: ");
                    int chefOpt = Integer.parseInt(scanner.nextLine().trim());
                    if (chefOpt != 0 && chefOpt != 1) {
                        throw new InvalidGameActionException("Chef action must be 0 or 1.");
                    }
                    if (chefOpt == 1) {
                        System.out.print("Select Table (1 or 2): ");
                        int tNum = Integer.parseInt(scanner.nextLine().trim());
                        chefChoice = new ChefAction(ChefAction.Type.COOK, tNum);
                    } else {
                        chefChoice = new ChefAction(ChefAction.Type.WAIT, 0);
                    }

                    controller.executeRemainingSteps(waiterChoice, chefChoice);
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
                } catch (java.util.NoSuchElementException e) {
                    // Scripted input may end early; do not retry forever at end-of-file.
                    System.err.println("\n[ERROR]: Input ended before the game was complete.");
                    return;
                } catch (NumberFormatException e) {
                    System.out.println("\n[ERROR]: Invalid input format. Please enter numeric choices.");
                }
            }
        }

        displayEndGameResults(controller);
    }

    private static void displayStartOfTurn(GameController controller) {
        System.out.println("\n==================================================");
        System.out.printf(" TURN %d / %d | Revenue: $%.2f%n",
                controller.getCurrentTurn(),
                GameController.MAX_TURNS,
                controller.getTotalRevenueDollars());
        System.out.printf(" Served: %d | Unhappy departures: %d | Paid: %d | Avg Satisfaction: %s%n",
                controller.getServedCount(),
                controller.getAbandonedCustomers().size(),
                controller.getPaidCustomers().size(),
                controller.getPaidCustomers().isEmpty() ? "N/A" : String.format("%.1f", controller.getAverageSatisfaction()));
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
                        String.format("%s [%s] (Prep: %d/%d)", o.getMenuItem().getName(), o.getStatus(), o.getUnitsDone(), o.getMenuItem().getRequiredPrepUnits());
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
        System.out.printf(" Average Satisfaction: %.1f (Target: 60.0)%n", controller.getAverageSatisfaction());
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
