package restaurantrush.customer;

/** Lifecycle states of a customer, including paid and abandoned outcomes. */
public enum CustomerState {
    ARRIVING,
    WAITING,
    SEATED,
    SERVED,
    EATING,
    READY_TO_PAY,
    PAID,
    ABANDONED
}
