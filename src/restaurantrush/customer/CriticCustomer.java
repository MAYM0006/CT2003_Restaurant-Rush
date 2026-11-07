package restaurantrush.customer;

import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;

/** Critic customer with faster dissatisfaction and a Salad preference. */
public class CriticCustomer extends Customer { //CriticCustomer inherits the accessible behaviour of Customer, including getId, getSatisfaction, setStatus and decaySatisfaction.
    public CriticCustomer(String id) { super(id); } //Invokes the superclass constructor. Critic Customer get its inherited starting state such as ID supplied by the calller, satisfaction at 100 and status as arriving. Note that constructors are not inherited, the CriticCustomer constructor explicitly invokves the parent constructor to initialise the inherited state.
    @Override public int getDecayRate() { return 12; }
    @Override public double getDiscountRate() { return 0.0; }
    @Override public Order chooseFood() { return new Order(MenuItem.SALAD); } //These are method overriding. It is to provide the Critic Customer specific implementation of the abstract methods from Customer.
}
