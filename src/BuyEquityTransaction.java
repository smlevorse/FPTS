import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 *         <p>
 *         Concrete command that handles buying equities and allows for redo/undo./
 */
public class BuyEquityTransaction extends Transaction implements Serializable {

    private String ID;  // Unique Identifier
    private double quantity;    // The number of the equity purchased

    private boolean uniqueEquity = false;   // If this equity is unique to the portfolio

    private String datePurchased = "";  // The date the transaction occurred
    private double pricePerShare = 0.0; // Price per share of the equity

    private String nameOfEquity = "";   // Equity name

    private String cashAccount = null;  // The name of cash account to transfer funds from

    /**
     * Create the transaction
     *
     * @param ID       the Equity's id
     * @param quantity the amount
     */
    public BuyEquityTransaction(String ID, double quantity) {
        this.ID = ID;
        this.quantity = quantity;
    }

    /**
     * Create transaction with cash account
     */
    public BuyEquityTransaction(String ID, double quantity, String cashAccountName) {
        this.ID = ID;
        this.quantity = quantity;
        cashAccount = cashAccountName;
    }

    /**
     * Buys a quantity of a given equity into the user's account
     */
    public BuyEquityTransaction(String ID, double quantity, String datePurchased, double pricePerShare, String nameOfEquity) {
        this.ID = ID;
        this.quantity = quantity;
        this.datePurchased = datePurchased;
        this.pricePerShare = pricePerShare;
        this.nameOfEquity = nameOfEquity;

        uniqueEquity = true;
    }

    /**
     * Buys a quantity of a given equity into the user's account
     */
    public BuyEquityTransaction(String ID, double quantity, String datePurchased, double pricePerShare, String nameOfEquity,
                                String cashAccountName) {
        this.ID = ID;
        this.quantity = quantity;
        this.datePurchased = datePurchased;
        this.pricePerShare = pricePerShare;
        this.nameOfEquity = nameOfEquity;
        cashAccount = cashAccountName;
        uniqueEquity = true;
    }

    @Override
    /**
     * Execute the transaction and buy the appropriate equity/equities
     */
    public void runTransaction(Portfolio portfolio) {
        // If the equity is not unique, add the value to the current holding
        if (!uniqueEquity) {
            portfolio.buy(ID, quantity);
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            datePurchased = df.format(new Date());

        } else {

            portfolio.buy(ID, quantity, datePurchased, pricePerShare, nameOfEquity);


        }
        // Get price of share
        for (Holding holding : portfolio.getHoldings()) {
            if (holding.identifier.equals(ID)) {
                pricePerShare = holding.getUnitPrice();
            }
        }

        if (cashAccount != null) {
            portfolio.withdrawal(this.cashAccount, pricePerShare * quantity);
        }
    }

    @Override
    /**
     * Undo the buy transaction by selling the same amount of equities at
     * their current price in the equities array
     */
    public void undoTransaction(Portfolio portfolio) {
        for (Holding holding : portfolio.getHoldings()) {
            if (holding.identifier.equals(ID)) {
                pricePerShare = holding.getUnitPrice();
            }
        }
        if (!uniqueEquity) {
            portfolio.sell(ID, quantity);
        } else {

            portfolio.sell(ID, quantity);


        }

        if (cashAccount != null) {
            portfolio.deposit(this.cashAccount, pricePerShare * quantity);
        }
    }


    @Override
    public String getTime() {
        return datePurchased;
    }


    @Override
    public String toString() {
        return "<html>Bought " + (int)quantity + " shares of <b>" + ID + "</b> ~" + getTime();
    }
}
