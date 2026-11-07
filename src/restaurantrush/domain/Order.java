package restaurantrush.domain;

import java.util.Objects;

/**
 * Tracks preparation and payment states for a menu item or combo.
 * The final bill is locked when the Waiter takes the order.
 */
public class Order {
    // Stage 1 lifecycle: cooking -> ready -> served -> paid (or cancelled on abandonment).
    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }

    private final MenuSelection selection;
    private int unitsDone;
    private Status status;
    private Integer lockedBillCents;
    private boolean happyHourDiscountApplied;

    /**
     * Creates an unprepared order for an individual item or combo.
     * @param selection customer's chosen menu item or combo
     */
    public Order(MenuSelection selection) {
        this.selection = Objects.requireNonNull(selection, "Menu selection is required");
        this.unitsDone = 0;
        this.status = Status.PREPARING;
    }

    public MenuSelection getSelection() { return selection; }
    public String getName() { return selection.getName(); }
    public int getBasePriceCents() { return selection.getPriceCents(); }
    public int getRequiredPrepUnits() { return selection.getRequiredPrepUnits(); }

    // Stage 1/2 compatibility: single-item orders have a MenuItem; combos do not.
    public MenuItem getMenuItem() {
        return selection instanceof MenuItem ? (MenuItem) selection : null;
    }

    public int getUnitsDone() { return unitsDone; }
    public Status getStatus() { return status; }
    public boolean isHappyHourDiscountApplied() { return happyHourDiscountApplied; }

    /** Fix the payable price when the waiter takes the order, not at checkout. */
    public void lockBill(int billCents, boolean happyHourApplied) {
        if (lockedBillCents != null) {
            throw new IllegalStateException("An order's bill cannot be changed after it is taken.");
        }
        if (billCents < 0) {
            throw new IllegalArgumentException("Bill cannot be negative.");
        }
        this.lockedBillCents = billCents;
        this.happyHourDiscountApplied = happyHourApplied;
    }

    /**
     * @return the payable amount in cents, fixed when the order was placed
     * @throws IllegalStateException if no bill has been locked
     */
    public int getBillCents() {
        if (lockedBillCents == null) {
            throw new IllegalStateException("Bill has not been locked at order placement.");
        }
        return lockedBillCents;
    }

    /**
     * Completes one Chef preparation unit and makes the order READY if finished.
     * @throws IllegalStateException if the order is not being prepared
     */
    public void advancePrep() {
        if (status != Status.PREPARING) {
            throw new IllegalStateException("Only an order being prepared can be cooked.");
        }
        unitsDone++;
        if (unitsDone >= selection.getRequiredPrepUnits()) {
            status = Status.READY;
        }
    }

    public boolean isReady() { return status == Status.READY; }

    public void markServed() {
        if (!isReady()) {
            throw new IllegalStateException("Only a READY order can be served.");
        }
        status = Status.SERVED;
    }

    public void markPaid() {
        if (status != Status.SERVED) {
            throw new IllegalStateException("Only a served order can be paid.");
        }
        status = Status.PAID;
    }

    public void cancel() {
        if (status == Status.PAID) {
            throw new IllegalStateException("A paid order cannot be cancelled.");
        }
        status = Status.CANCELLED;
    }
}
