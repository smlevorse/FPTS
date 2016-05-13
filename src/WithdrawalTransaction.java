import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *
 * A transaction object for withdrawing funds from a cash account
 */
public class WithdrawalTransaction extends Transaction implements Serializable {

    private String name;
    private double amount;

    private String transactionTime;

    public WithdrawalTransaction(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    public void runTransaction(Portfolio portfolio) {
        portfolio.withdrawal(name, amount);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        transactionTime = df.format(new Date());
    }

    @Override
    public void undoTransaction(Portfolio portfolio) {
        portfolio.deposit(name, amount);
    }


    @Override
    public String getTime() {
        return transactionTime;
    }

    @Override
    public String toString() {
        return "Withdrew $" + String.format("%.2f", amount) + " from " + name + " ~" + getTime();
    }
}
