package restaurantrush.domain;

/** Two menu items sold together at 90% of their combined price. */
public class ComboMeal implements MenuSelection {
    private final MenuItem item1;
    private final MenuItem item2;
    private final int priceCents;
    private final int requiredPrepUnits;

    /**
     * Creates a 10%-discounted combination, adding both preparation requirements.
     * @param item1 first existing menu item
     * @param item2 second existing menu item
     */
    public ComboMeal(MenuItem item1, MenuItem item2) {
        this.item1 = item1;
        this.item2 = item2;
        this.requiredPrepUnits = item1.getRequiredPrepUnits() + item2.getRequiredPrepUnits();
        int combinedBaseCents = item1.getPriceCents() + item2.getPriceCents();
        this.priceCents = (int) Math.round(combinedBaseCents * 0.90);
    }

    public String getName() { return item1.getName() + " + " + item2.getName() + " Combo"; }
    public int getPriceCents() { return priceCents; }
    public int getRequiredPrepUnits() { return requiredPrepUnits; }
}
