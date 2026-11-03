package restaurantrush.domain;

public class Order {
    // Stage 1 lifecycle: cooking -> ready -> served -> paid (or cancelled on abandonment).
    public enum Status { PREPARING, READY, SERVED, PAID, CANCELLED }

    private final MenuItem menuItem;
    private int unitsDone;
    private Status status;

    public Order(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.unitsDone = 0;
        this.status = Status.PREPARING;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getUnitsDone() { return unitsDone; }
    public Status getStatus() { return status; }

    public void advancePrep() {
        if (status != Status.PREPARING) {
            throw new IllegalStateException("Only an order being prepared can be cooked.");
        }
        unitsDone++;
        if (unitsDone >= menuItem.getRequiredPrepUnits()) {
            status = Status.READY;
        }
    }

    public boolean isReady() {
        return status == Status.READY;
    }

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
