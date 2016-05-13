import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *         <p>
 *         A transaction representing a deposit of money into an account.
 */
public class DepositTransaction extends Transaction implements Serializable {

    private String name;  // name of the account the deposit goes to
    private double amount;  // amount to be deposited

    private String transactionTime; // time the transaction was executed

    public DepositTransaction(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    /**
     * Execute transaction noting what time it was executed in order to undo.
     */
    public void runTransaction(Portfolio portfolio) {
        portfolio.deposit(name, amount);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        transactionTime = df.format(new Date());
    }

    @Override
    /**
     * Undo the transaction
     */
    public void undoTransaction(Portfolio portfolio) {
        portfolio.withdrawal(name, amount);
    }


    @Override
    /**
     * Get time of when the transaction was executed.
     */
    public String getTime() {
        return transactionTime;
    }

    @Override
    public String toString() {
        return "Deposited $" + String.format("%.2f", amount) + " into " + name + " ~" + getTime();
    }
}
