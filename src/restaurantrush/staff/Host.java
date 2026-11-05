package restaurantrush.staff;

public class Host extends Staff {
    public Host() { super("Host", "Front of House Staff"); }
    public void seatExtra() {
        if (!isActionAvailable()) {
            throw new IllegalStateException("Host has already used action capacity.");
        }
        useAction();
    }
}
