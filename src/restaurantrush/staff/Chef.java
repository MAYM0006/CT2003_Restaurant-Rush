package restaurantrush.staff;

/** Advances at most one unit of food preparation per turn. */
public class Chef extends Staff {
    public Chef() { super("Chef", "Kitchen Staff"); }
    public void cook() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Chef has already used action capacity.");
        }
        useAction();
    }
}
