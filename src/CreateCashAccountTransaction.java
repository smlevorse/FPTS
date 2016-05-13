import java.io.Serializable;


/**
 * @author Paul Hulbert
 *         <p>
 *         A transaction that creates a cash account for a portfolio.
 */
public class CreateCashAccountTransaction extends Transaction implements Serializable {

    private String name;
    private double amount;
    private String dateCreated;

    /**
     * Build the transaction
     */
    public CreateCashAccountTransaction(String name, double amount, String dateCreated) {
        this.name = name;
        this.amount = amount;
        this.dateCreated = dateCreated;
    }

    @Override
    /**
     * Create the cash account in the system
     */
    public void runTransaction(Portfolio portfolio) {
        portfolio.createCashAccount(name, amount, dateCreated);
    }

    @Override
    /**
     * Undo the cash account creation by deleting it.  This does not relocate the money
     * it simply removes the cash account and its properties from the system.
     */
    public void undoTransaction(Portfolio portfolio) {
        portfolio.deleteCashAccount(name);
    }

    @Override
    public String getTime() {
        return dateCreated;
    }

    @Override
    public String toString() {
        return "<html>Created cash account <b>" + name + "</b> containing $" + String.format("%.2f", amount) + " ~" + getTime();
    }
}
