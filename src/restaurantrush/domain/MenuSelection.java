package restaurantrush.domain;

/** A menu choice, whether a single item or a discounted combination. */
public interface MenuSelection {
    String getName();
    int getPriceCents();
    int getRequiredPrepUnits();
}
