import java.io.Serializable;


/**
 * @author Paul Hulbert
 *
 * Helps record a transaction history for storage in a file
 */
public class StringHistoryTransaction extends Transaction implements Serializable {

    private String details;
    private String date;


    public StringHistoryTransaction(String details, String date) {
        this.details = details;
        this.date = date;
        undoable = false;
    }

    @Override
    public void runTransaction(Portfolio portfolio) {

    }

    @Override
    public void undoTransaction(Portfolio portfolio) {

    }


    @Override
    public String getTime() {
        return date;
    }

    @Override
    public String toString() {
        return details + " ~" + getTime();
    }
}
