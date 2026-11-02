package restaurantrush.staff;

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
    public void resetTurn() { this.actionAvailable = true; }
}
