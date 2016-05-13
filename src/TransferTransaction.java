import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *
 * A transaction for transferring funds from one cashaccount to another
 */
public class TransferTransaction extends Transaction implements Serializable {


    private String source;
    private String destination;
    private double amount;

    private String transactionTime;

    public TransferTransaction(String source, String destination, double amount) {
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    @Override
    public void runTransaction(Portfolio portfolio) {
        portfolio.transferFunds(source, destination, amount);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        transactionTime = df.format(new Date());
    }

    @Override
    public void undoTransaction(Portfolio portfolio) {
        portfolio.transferFunds(destination, source, amount);
    }


    @Override
    public String getTime() {
        return transactionTime;
    }

    @Override
    public String toString() {
        return "Transferred $" + String.format("%.2f", amount) + " from " + source + " to " + destination + " ~" + getTime();
    }
}
