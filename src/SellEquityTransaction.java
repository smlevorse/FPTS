import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *
 * A transaction object representing the sale of an equity
 */
public class SellEquityTransaction extends Transaction implements Serializable {
    private String ID;
    private double quantity;


    private String transactionTime;

    private String cashAccount = null;


    public SellEquityTransaction(String ID, double quantity) {
        this.ID = ID;
        this.quantity = quantity;
    }

    public SellEquityTransaction(String ID, double quantity, String cashAccount) {
        this.ID = ID;
        this.quantity = quantity;
        this.cashAccount = cashAccount;
    }

    @Override
    public void runTransaction(Portfolio portfolio) {

        double pricePerShare = 0;
        for (Holding holding : portfolio.getHoldings()) {
            if (holding.identifier.equals(ID)) {
                pricePerShare = holding.getUnitPrice();
            }
        }
        if (cashAccount != null && !cashAccount.equals("External transaction")) {
            portfolio.deposit(this.cashAccount, pricePerShare * quantity);
        }


        portfolio.sell(ID, quantity);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        transactionTime = df.format(new Date());
    }

    @Override
    public void undoTransaction(Portfolio portfolio) {
        portfolio.buy(ID, quantity);

        double pricePerShare = 0;
        for (Holding holding : portfolio.getHoldings()) {
            if (holding.identifier.equals(ID)) {
                pricePerShare = holding.getUnitPrice();
            }
        }
        if (cashAccount != null && !cashAccount.equals("External transaction")) {
            portfolio.withdrawal(this.cashAccount, pricePerShare * quantity);
        }
    }


    @Override
    public String getTime() {
        return transactionTime;
    }

    @Override
    public String toString() {
        return "<html>Sold " + (int)quantity + " shares of <b>" + ID + "</b> ~" + getTime();
    }
}
