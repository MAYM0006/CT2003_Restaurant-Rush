package restaurantrush.staff;

/** Shared identity and one-action-per-turn capacity for restaurant staff. */
public abstract class Staff {
    private final String name;
    private final String role;
    private boolean actionAvailable;

    public Staff(String name, String role) {
        this.name = name;
        this.role = role;
        this.actionAvailable = true;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public boolean isActionAvailable() { return actionAvailable; }
    public void useAction() { this.actionAvailable = false; }
    /** Restores the member's capacity at the start of a new turn. */
    public void resetTurn() { this.actionAvailable = true; }
}
