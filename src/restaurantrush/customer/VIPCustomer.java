package restaurantrush.customer;

import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;

/** VIP customer with slower dissatisfaction and a 10% bill discount. */
public class VIPCustomer extends Customer { //I am establishing an inheritance relationship. VIPCustomer inherits the accessible behaviour of Customer, including getId(), getSatisfaction(), setStatus() and decaySatisfaction(). This is to avoid duplication those methods in every customer subclass.
    public VIPCustomer(String id) { super(id); } //super(id) calls the superclass constructor. This is how a VIP customer gets the inherited starting state. The ID is supplied by the caller, the satisfaction is initialised at 100, status initialised to ARRIVING. Note that the VIP construction invokes the parent constructor to initialise the inherited state, not that constructors get inherited. 
//Without super(id), Java would look for a no-argument constructor in Customer class. However, in customer class, we declared public Customer(String id) (in other words, it NEEDS a String argument to be supplied). Thus Java wont be able to find the required superclass constructor and compilation fails. 
    @Override public int getDecayRate() { return 5; }
    @Override public double getDiscountRate() { return 0.10; }
    @Override public Order chooseFood() { return new Order(MenuItem.BURGER); } //Overriding because we want the VIP specific implementation of the abstract methods from Customer. This is method overriding -- the parent specifies what behaviours all customers provide while subclasses decide how those behaviours differ.
} //We implemented deterministic food preferences through method overriding to keep the simulation predictable and testable. We recognise fixed preferences are a simplification, adn a future version could a random selection instead.