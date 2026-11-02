package restaurantrush.staff;

public class Chef extends Staff {
    public Chef() { super("Chef", "Kitchen Staff"); }
    public void cook() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Chef has already used action capacity.");
        }
        useAction();
    }
}
