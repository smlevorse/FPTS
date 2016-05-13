import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Joey Bovio
 *         <p>
 *         Holds watchlist equities and allows for the addition and removal of watchlist equities.
 */
public class Watchlist implements Serializable {

    public ArrayList<WatchlistEquity> watchlistEquities = new ArrayList<>();

    /**
     * Create an instance of a watchlist.
     */
    public Watchlist() {

    }

    /**
     * Add a new watchlist equity.
     *
     * @param ticker      The equity ticker.
     * @param highTrigger The high trigger price for the equity.
     * @param lowTrigger  The low trigger price for the equity.
     */
    public void addWatchlistEquity(String ticker, double highTrigger, double lowTrigger) {
        WatchlistEquity watchEquity;
        // Check for a watchlist equity that already has the specified ticker
        if ((watchEquity = getWatchEquityByTicker(ticker)) != null) {
            watchEquity.setLowTrigger(lowTrigger);
            watchEquity.setHighTrigger(highTrigger);
            return;
        }

        // Create a new watchlist equity and add it to the list of watchlist equities
        watchEquity = new WatchlistEquity(ticker, highTrigger, lowTrigger);
        watchlistEquities.add(watchEquity);
    }

    /**
     * Remove a watchlist equity from the watchlist.
     *
     * @param ticker The ticker of the watchlist equity being removed.
     */
    public void removeWatchlistEquity(String ticker) {
        Iterator<WatchlistEquity> watchlistIterator = watchlistEquities.iterator();
        while (watchlistIterator.hasNext()) {
            WatchlistEquity watchEquity = watchlistIterator.next();
            if (watchEquity.getTicker().equals(ticker)) {
                watchlistIterator.remove();
                return;
            }
        }
    }

    /**
     * Call the checkTriggers method for all watchlist equities in order to check the high and low triggers to the
     * current equity price.
     */
    public void update() {
        for (WatchlistEquity watchEquity : watchlistEquities) {
            watchEquity.checkTriggers();
        }
    }

    /**
     * Get a watchlist equity by providing the equity ticker.
     *
     * @param ticker The equity ticker.
     * @return The watchlist equity with the provided ticker.
     */
    public WatchlistEquity getWatchEquityByTicker(String ticker) {
        for (WatchlistEquity watchEquity : watchlistEquities) {
            if (watchEquity.getTicker().equals(ticker)) {
                return watchEquity;
            }
        }
        return null;
    }
}
