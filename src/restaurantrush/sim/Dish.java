package restaurantrush.sim;

public class Dish {
    public final String id;
    public final String name;
    public final int priceCents;
    public final int prepUnits;

    public Dish(String id, String name, int priceCents, int prepUnits) {
        if (id == null || id.isBlank() || name == null || name.isBlank()
                || priceCents <= 0 || prepUnits <= 0) {
            throw new IllegalArgumentException("Dish details must be valid and positive.");
        }
        this.id = id;
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }
}
