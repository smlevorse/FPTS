import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *         <p>
 *         A transaction that deletes a cash account for a portfolio.
 */
public class DeleteCashAccountTransaction extends Transaction implements Serializable {

    private String name;  // The accounts name
    private double amount;  // Amount in the cash account
    private String dateCreated;  //Date of the accounts creation
    private String timeDeleted;  //When the account was removed

    /**
     * Create a transaction to delete a given transaction.
     */
    public DeleteCashAccountTransaction(String name) {
        this.name = name;
    }

    @Override
    /**
     * Remove a cash account while storing its amount and dateCreated at its
     * removal.
     */
    public void runTransaction(Portfolio portfolio) {
        // Find the cash account in the portfolio
        for (CashAccount cashAccount : portfolio.getCashAccounts()) {
            if (cashAccount.getAccountName().equals(name)) {
                amount = cashAccount.getCashAmount();
                dateCreated = cashAccount.dateCreated;
                break;
            }
        }

        // Delete the cash account and record transaction history
        portfolio.deleteCashAccount(name);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        timeDeleted = df.format(new Date());
    }

    @Override
    /**
     * Undo transction by recreating a cash account identical to the original
     * at its removal
     */
    public void undoTransaction(Portfolio portfolio) {
        portfolio.createCashAccount(name, amount, dateCreated);
    }

    @Override
    public String getTime() {
        return timeDeleted;
    }

    @Override
    public String toString() {
        return "<html>Deleted cash account <b>" + name + "</b> ~" + getTime();
    }
}
