package restaurantrush.domain;

public class Order {
    private final MenuItem menuItem;
    private int unitsDone;

    public Order(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.unitsDone = 0;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getUnitsDone() { return unitsDone; }

    public void advancePrep() {
        if (!isReady()) {
            this.unitsDone++;
        }
    }

    public boolean isReady() {
        return unitsDone >= menuItem.getRequiredPrepUnits();
    }
}
