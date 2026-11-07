package restaurantrush.staff;

/** Handles one seating, ordering, or serving task per turn. */
public class Waiter extends Staff {
    public Waiter() { super("Waiter", "Floor Staff"); }
    public void performTask() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Waiter has already used action capacity.");
        }
        useAction();
    }
}