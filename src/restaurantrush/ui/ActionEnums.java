package restaurantrush.ui;

public class ActionEnums {
    public static class WaiterAction {
        public enum Type { WAIT, SEAT, TAKE_ORDER, SERVE }
        private final Type type;
        private final int tableNumber;

        public WaiterAction(Type type, int tableNumber) {
            this.type = type;
            this.tableNumber = tableNumber;
        }

        public Type getType() { return type; }
        public int getTableNumber() { return tableNumber; }
    }

    public static class ChefAction {
        public enum Type { WAIT, COOK }
        private final Type type;
        private final int tableNumber;

        public ChefAction(Type type, int tableNumber) {
            this.type = type;
            this.tableNumber = tableNumber;
        }

        public Type getType() { return type; }
        public int getTableNumber() { return tableNumber; }
    }
    public static class HostAction {
        public enum Type { WAIT, SEAT }
        private final Type type;
        private final int tableNumber;

        public HostAction(Type type, int tableNumber) {
            this.type = type;
            this.tableNumber = tableNumber;
        }

        public Type getType() { return type; }
        public int getTableNumber() { return tableNumber; }
    }
}
