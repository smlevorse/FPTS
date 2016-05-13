import java.io.Serializable;


/**
 * @author Paul Hulbert
 *         <p>
 *         A Cash Account users can incorporate into their portfolio to buy and sell
 *         holdings with.
 */
public class CashAccount implements Serializable {

    public String accountName;  // Account name
    public String dateCreated;  // Date the account was created
    public double cashAmount;   // Ammount of cash in the account

    /**
     * Create cash account
     */
    public CashAccount(String accountName, String dateCreated, double cashAmount) {
        this.accountName = accountName;
        this.dateCreated = dateCreated;
        this.cashAmount = cashAmount;
    }

    /* Getters and setters */

    public double getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getDateCreated() {
        return dateCreated;
    }
}
