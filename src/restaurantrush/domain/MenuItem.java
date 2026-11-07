package restaurantrush.domain;

public enum MenuItem implements MenuSelection {
    SALAD("Salad", 900, 1),
    BURGER("Burger", 1500, 1),
    PASTA("Pasta", 1800, 2);

    private final String name;
    private final int priceCents;
    private final int requiredPrepUnits;

    MenuItem(String name, int priceCents, int requiredPrepUnits) {
        this.name = name;
        this.priceCents = priceCents;
        this.requiredPrepUnits = requiredPrepUnits;
    }

    public String getName() { return name; }
    public int getPriceCents() { return priceCents; }
    public int getRequiredPrepUnits() { return requiredPrepUnits; }
}
