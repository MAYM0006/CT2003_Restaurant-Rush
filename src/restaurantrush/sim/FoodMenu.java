package restaurantrush.sim;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class FoodMenu {
    private final List<Dish> dishes;

    public FoodMenu(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) throw new IllegalArgumentException("Menu cannot be empty.");
        this.dishes = Collections.unmodifiableList(new ArrayList<>(dishes));
    }

    public static FoodMenu opening() {
        return new FoodMenu(List.of(
            new Dish("salad", "Salad", 900, 1),
            new Dish("burger", "Burger", 1500, 1),
            new Dish("pasta", "Pasta", 1800, 2)
        ));
    }

    public List<Dish> dishes() { return dishes; }
    public Dish get(int index) { return dishes.get(Math.floorMod(index, dishes.size())); }
}
