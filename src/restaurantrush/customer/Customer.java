package restaurantrush.customer; //package: Groups the class under the restaurantrush.customer namespace

import restaurantrush.domain.Order; //import: Allows this file to refer to the Order class from another package

public abstract class Customer { //public: Makes the class accessible from other packages. "abstract" Prevents direct instantiation and permits abstract methods. "class" Defines the common blueprint for customer objects
    private final String id; //"id" Customer identifier that cannot be reassigned after initialization. These are private to restrict direct access to the customer's internal attributes (encapsulation). Additionally, it is "final" because once the id is assigned, it cannot be reassigned.
    private int satisfaction; //current satisifaction score
    private CustomerState status; //customer's current lifecycle state. For example, CUST-7, 100 satisfaction and status is ARRIVING.

    public Customer(String id) {
        this.id = id; //Assigns the supplied customer identifier. "this" refers to the current object.
        this.satisfaction = 100; //Gives the customer full initial satisfaction
        this.status = CustomerState.ARRIVING; //CustomerState.ARRIVING sets the initial lifecycle state.
    }

    public String getId() { return id; }
    public int getSatisfaction() { return satisfaction; } //An accessor returns information about an object, such as getSatisfaction().
    public CustomerState getStatus() { return status; }
    public void setStatus(CustomerState status) { this.status = status; } //A mutator modifies an object's state, such as setStatus().

    public void decaySatisfaction() {
        if (status == CustomerState.WAITING || status == CustomerState.SEATED) {
            this.satisfaction = Math.max(0, this.satisfaction - getDecayRate());
        } //Satsifaction decreases when the customer is either waiting for a table or seated but not yet served, per assignment requirements. If the customer is already served, ready_to_pay or paid, the satisfaction will not reduce. Further, we used Math.max to prevent satisfaction from becoming negative. We also did no hardcode the -8 satisfaction penalty. See getDecayRate() below.
    }

    public abstract int getDecayRate(); //These abstract methods specify what behaviours concrete customer subclasses must implement, without defining those behaviours in the parent class. The responsibility of determining how much satisfaction Reg vs VIP vs Critic loses belongs to the subclasses. In VIPCustomer.java, the getDecayRate() is 5 and in CriticCustomer.java, the getDecayRate() is 12 (i.e., VIPCustomer and CriticCustomer extends Customer (inheritance)). Subclasses do not need to rewrite decaySatisfaction, they inherit the shared method from Customer and provide their own implementation of getDecayRate() (i.e., the inherited decaySatisfaction() calls the correct getDecayRate() based on the actual customer object (runtime polymorphism)).
    public abstract double getDiscountRate();
    public abstract Order chooseFood();

    @Override
    public String toString() { //Overrides toString() inherited from Java'sObject class
        return String.format("[%s] ID: %s | Sat: %d | Status: %s", 
                getClass().getSimpleName(), id, satisfaction, status); //Insers values into a formatted string, %s for strings and %d for integers. Then getClass().getSimpleName() is to obtain actual runtime class name, such as VIPCustomer.
    }
}
