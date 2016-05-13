/**
 * @author Paul Hulbert
 *
 * A transaction for keeping track of a transaction history
 */

public abstract class Transaction {

    public boolean undoable = true;

    public abstract void runTransaction(Portfolio portfolio);

    public abstract void undoTransaction(Portfolio portfolio);

    public abstract String getTime();
}
