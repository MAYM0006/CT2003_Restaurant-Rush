package restaurantrush.controller;

import restaurantrush.customer.Customer;
import restaurantrush.customer.CustomerFactory;
import restaurantrush.customer.CustomerState;
import restaurantrush.domain.BillCalculator;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;
import restaurantrush.exception.InvalidGameActionException;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Host;
import restaurantrush.staff.Waiter;
import restaurantrush.ui.ActionEnums.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameController {
    public static final int MAX_TURNS = 18;
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private int currentTurn;
    private int totalRevenueCents;
    private int happyHourTurnsRemaining;
    private boolean happyHourUsed;
    private boolean turnStarted;

    private final List<Table> tables;
    private final Queue<Customer> waitingQueue;
    private final List<Customer> paidCustomers;
    private final List<Customer> abandonedCustomers;
    // FIFO order of customers who became READY_TO_PAY in earlier turns.
    private final Queue<Table> paymentQueue;
    private int servedCount;

    private final Waiter waiter;
    private final Chef chef;
    private final Host host;
    private final Cashier cashier;

    public GameController() {
        this.currentTurn = 1;
        this.totalRevenueCents = 0;
        this.happyHourTurnsRemaining = 0;
        this.happyHourUsed = false;
        this.turnStarted = false;

        this.tables = List.of(new Table(1), new Table(2));
        this.waitingQueue = new LinkedList<>();
        this.paidCustomers = new ArrayList<>();
        this.abandonedCustomers = new ArrayList<>();
        this.paymentQueue = new LinkedList<>();
        this.servedCount = 0;

        this.waiter = new Waiter();
        this.chef = new Chef();
        this.host = new Host();
        this.cashier = new Cashier();
    }

    public void startTurnStep1() {
        if (turnStarted || currentTurn > MAX_TURNS) {
            throw new InvalidGameActionException("Cannot start this turn again or start after the game ends.");
        }
        turnStarted = true;
        resetStaffTurnCapacity();
        step1_ArrivalsAndHappyHour();
    }

    /** Retain the Stage 1/2 API; Stage 3's additional Host action defaults to WAIT. */
    public void executeRemainingSteps(WaiterAction waiterChoice, ChefAction chefChoice) {
        executeRemainingSteps(waiterChoice, chefChoice, new HostAction(HostAction.Type.WAIT, 0));
    }

    public void executeRemainingSteps(WaiterAction waiterChoice, ChefAction chefChoice, HostAction hostChoice) {
        if (!turnStarted) {
            throw new InvalidGameActionException("Start the turn before submitting staff actions.");
        }
        validateAllActions(waiterChoice, chefChoice, hostChoice);
        step2And3_ExecuteStaffActions(waiterChoice, chefChoice, hostChoice);
        step4_EatingAndPayment();
        step5_WaitingAndAbandonment();
        step6_AdvanceTurn();
    }

    private void resetStaffTurnCapacity() {
        waiter.resetTurn();
        chef.resetTurn();
        host.resetTurn();
        cashier.resetTurn();
    }

     private void step1_ArrivalsAndHappyHour() {
        if (happyHourTurnsRemaining > 0) {
            for (Customer c : waitingQueue) {
                c.restoreSatisfaction(15);
            }
            for (Table t : tables) {
                if (t.isOccupied() && t.getCustomer().getStatus() == CustomerState.SEATED) {
                    t.getCustomer().restoreSatisfaction(15);
                }
            }
        }
        for (int turn : ARRIVAL_TURNS) {
            if (currentTurn == turn) {
                Customer newCustomer = spawnCustomerForTurn(currentTurn);
                newCustomer.setStatus(CustomerState.WAITING);
                waitingQueue.add(newCustomer);
                break;
            }
        }
    }

    private void validateAllActions(WaiterAction waiterChoice, ChefAction chefChoice, HostAction hostChoice) {
        int simulatedQueueSize = waitingQueue.size();
        Table t1 = getTableByNumber(1);
        Table t2 = getTableByNumber(2);

        boolean t1Occupied = t1.isOccupied();
        boolean t2Occupied = t2.isOccupied();
        boolean t1HasOrder = t1.isOccupied() && t1.getOrder() != null;
        boolean t2HasOrder = t2.isOccupied() && t2.getOrder() != null;

        if (hostChoice != null && hostChoice.getType() == HostAction.Type.SEAT) {
            if (!host.isActionAvailable()) {
                throw new InvalidGameActionException("Host has no remaining action capacity.");
            }
            if (simulatedQueueSize == 0) {
                throw new InvalidGameActionException("Host cannot seat: Waiting queue is empty.");
            }
            int tNum = hostChoice.getTableNumber();
            if ((tNum == 1 && t1Occupied) || (tNum == 2 && t2Occupied)) {
                throw new InvalidGameActionException("Host cannot seat: Table " + tNum + " is already occupied.");
            }

            simulatedQueueSize--;
            if (tNum == 1) t1Occupied = true;
            if (tNum == 2) t2Occupied = true;
        }

        if (waiterChoice != null && waiterChoice.getType() != WaiterAction.Type.WAIT) {
            if (!waiter.isActionAvailable()) {
                throw new InvalidGameActionException("Waiter has no remaining action capacity.");
            }
            Table target = getTableByNumber(waiterChoice.getTableNumber());
            boolean targetOccupied = (waiterChoice.getTableNumber() == 1) ? t1Occupied : t2Occupied;
            boolean targetHasOrder = (waiterChoice.getTableNumber() == 1) ? t1HasOrder : t2HasOrder;

            switch (waiterChoice.getType()) {
                case SEAT:
                    if (simulatedQueueSize == 0) {
                        throw new InvalidGameActionException("Waiter cannot seat: Waiting queue is empty.");
                    }
                    if (targetOccupied) {
                        throw new InvalidGameActionException("Waiter cannot seat: Table " + target.getTableNumber() + " is occupied.");
                    }
                    break;
                case TAKE_ORDER:
                    if (!targetOccupied) {
                        throw new InvalidGameActionException("Waiter cannot take order: Table " + target.getTableNumber() + " is empty.");
                    }
                    if (targetHasOrder) {
                        throw new InvalidGameActionException("Waiter cannot take order: Table " + target.getTableNumber() + " already has an order.");
                    }
                    if (target.getCustomer() != null && target.getCustomer().getStatus() != CustomerState.SEATED) {
                        throw new InvalidGameActionException("Waiter cannot take order: Customer is not seated and waiting to order.");
                    }
                    if (waiterChoice.getTableNumber() == 1) t1HasOrder = true;
                    if (waiterChoice.getTableNumber() == 2) t2HasOrder = true;
                    break;
                case SERVE:
                    if (!targetOccupied || !targetHasOrder) {
                        throw new InvalidGameActionException("Waiter cannot serve: No order on Table " + target.getTableNumber());
                    }
                    if (target.getCustomer().getStatus() != CustomerState.SEATED) {
                        throw new InvalidGameActionException("Waiter cannot serve: Customer on Table " + target.getTableNumber() + " has already been served.");
                    }
                    if (!target.getOrder().isReady()) {
                        throw new InvalidGameActionException("Waiter cannot serve: Order on Table " + target.getTableNumber() + " is not READY yet.");
                    }
                    break;
            }
        }

        if (chefChoice != null && chefChoice.getType() == ChefAction.Type.COOK) {
            if (!chef.isActionAvailable()) {
                throw new InvalidGameActionException("Chef has no remaining cooking capacity.");
            }
            Table target = getTableByNumber(chefChoice.getTableNumber());
            boolean targetHasOrder = (chefChoice.getTableNumber() == 1) ? t1HasOrder : t2HasOrder;

            if (!targetHasOrder) {
                throw new InvalidGameActionException("Chef cannot cook: No active order on Table " + target.getTableNumber());
            }

            if (target.getOrder() != null && target.getOrder().getStatus() != Order.Status.PREPARING) {
                throw new InvalidGameActionException("Chef cannot cook: Order on Table " + target.getTableNumber() + " is no longer being prepared.");
            }
        }
    }

    private void step2And3_ExecuteStaffActions(WaiterAction waiterChoice, ChefAction chefChoice, HostAction hostChoice) {
        if (hostChoice != null && hostChoice.getType() == HostAction.Type.SEAT) {
            Table targetTable = getTableByNumber(hostChoice.getTableNumber());
            Customer c = waitingQueue.poll();
            targetTable.assignCustomer(c);
            c.setStatus(CustomerState.SEATED);
            host.seatExtra();
        }

        if (waiterChoice != null && waiterChoice.getType() != WaiterAction.Type.WAIT) {
            Table targetTable = getTableByNumber(waiterChoice.getTableNumber());
            switch (waiterChoice.getType()) {
                case SEAT:
                    Customer c = waitingQueue.poll();
                    targetTable.assignCustomer(c);
                    c.setStatus(CustomerState.SEATED);
                    break;
                case TAKE_ORDER:
                    Order newOrder = targetTable.getCustomer().chooseFood(); //Customer.java declared the abstract method, chooseFood(). VIPCustomer, CriticCustomer and Regular override chooseFood() with their own implementation. GameController calls chooseFood() without checking which customer subclass it is dealing with because Java uses dynamic binding to execute the implementation belonging to the actual customer object. The same method call supports different customer behaviours without need if-else statements and without needing to check the customer type.
                    boolean discounted = isHappyHourActive();
                    int lockedBill = BillCalculator.calculateBill(
                            newOrder.getBasePriceCents(), targetTable.getCustomer(), discounted);
                    newOrder.lockBill(lockedBill, discounted);
                    targetTable.assignOrder(newOrder);
                    break;
                case SERVE:
                    targetTable.getOrder().markServed();
                    targetTable.getCustomer().setStatus(CustomerState.SERVED);
                    servedCount++;
                    break;
            }
            waiter.performTask();
        }

        if (chefChoice != null && chefChoice.getType() == ChefAction.Type.COOK) {
            Table targetTable = getTableByNumber(chefChoice.getTableNumber());
            targetTable.getOrder().advancePrep();
            chef.cook();
        }
    }

    private void step4_EatingAndPayment() {
        // Pay only customers who were READY_TO_PAY before this turn's eating step.
        // The FIFO queue enforces oldest-ready-first and prevents same-turn payment.
        if (cashier.isActionAvailable() && !paymentQueue.isEmpty()) {
            Table t = paymentQueue.remove();
            Customer c = t.getCustomer();
            Order o = t.getOrder();
            int finalBillCents = o.getBillCents();

            cashier.processPayment();
            o.markPaid();
            c.setStatus(CustomerState.PAID);
            totalRevenueCents += finalBillCents;
            paidCustomers.add(c);
            t.clearTable();
        }

        // A newly served customer eats; an eating customer becomes ready to pay.
        // Newly READY_TO_PAY customers are queued for a subsequent turn.
        for (Table t : tables) {
            if (t.isOccupied()) {
                Customer c = t.getCustomer();
                if (c.getStatus() == CustomerState.SERVED) {
                    c.setStatus(CustomerState.EATING);
                } else if (c.getStatus() == CustomerState.EATING) {
                    c.setStatus(CustomerState.READY_TO_PAY);
                    paymentQueue.add(t);
                }
            }
        }
    }

    private void step5_WaitingAndAbandonment() {
        Queue<Customer> remainingQueue = new LinkedList<>();
        while (!waitingQueue.isEmpty()) {
            Customer c = waitingQueue.poll();
            c.decaySatisfaction();
            if (c.getSatisfaction() <= 0) {
                abandonedCustomers.add(c);
            } else {
                remainingQueue.add(c);
            }
        }
        waitingQueue.addAll(remainingQueue);

        for (Table t : tables) {
            if (t.isOccupied()) {
                Customer c = t.getCustomer();
                if (c.getStatus() == CustomerState.WAITING || c.getStatus() == CustomerState.SEATED) {
                    c.decaySatisfaction();
                    if (c.getSatisfaction() <= 0) {
                        abandonedCustomers.add(c);
                        if (t.getOrder() != null) {
                            t.getOrder().cancel();
                        }
                        t.clearTable();
                    }
                }
            }
        }
    }

    private void step6_AdvanceTurn() {
        if (happyHourTurnsRemaining > 0) {
            happyHourTurnsRemaining--;
        }

//1 Nov: Previousy, currentTurn incremented when it was less than MAX__TURNS(18).
//At turn 18, the condition became false, leaving currentTurn stuck at 18.
//Fix: Allow unconditional increment so that the game loop can exit when currentTurns becomes 19.
        currentTurn++;
        turnStarted = false;
    }

    /** Activate at the beginning of a turn, BEFORE the turn-start satisfaction recovery. */
    public void activateHappyHour() {
        if (turnStarted || currentTurn > MAX_TURNS) {
            throw new InvalidGameActionException("Happy Hour can only be activated before a turn starts.");
        }
        if (happyHourUsed) {
            throw new InvalidGameActionException("Happy Hour can only be used once per game.");
        }
        happyHourUsed = true;
        happyHourTurnsRemaining = 2;
    }

    public boolean canActivateHappyHour() { return !happyHourUsed && !turnStarted && currentTurn <= MAX_TURNS; }
    public boolean isHappyHourUsed() { return happyHourUsed; }

    private Table getTableByNumber(int tableNumber) {
        return tables.stream()
                .filter(t -> t.getTableNumber() == tableNumber)
                .findFirst()
                .orElseThrow(() -> new InvalidGameActionException("Invalid Table Number: " + tableNumber));
    }

    private Customer spawnCustomerForTurn(int turn) {
        return CustomerFactory.createCustomerForTurn(turn);
    }

    public int getCurrentTurn() { return currentTurn; }
    public double getTotalRevenueDollars() { return totalRevenueCents / 100.0; }
    public Queue<Customer> getWaitingQueue() { return waitingQueue; }
    public List<Table> getTables() { return tables; }
    public List<Customer> getPaidCustomers() { return paidCustomers; }
    public List<Customer> getAbandonedCustomers() { return abandonedCustomers; }
    public int getServedCount() { return servedCount; }
    public boolean isHappyHourActive() { return happyHourTurnsRemaining > 0; }
    public int getHappyHourTurnsRemaining() { return happyHourTurnsRemaining; }

    public double getAverageSatisfaction() {
        if (paidCustomers.isEmpty()) return 0.0;
        int sum = 0;
        for (Customer c : paidCustomers) {
            sum += c.getSatisfaction();
        }
        return (double) sum / paidCustomers.size();
    }

    public boolean isVictory() {
        return paidCustomers.size() >= 4 && getTotalRevenueDollars() >= 45.0 && getAverageSatisfaction() >= 60.0;
    }
}
