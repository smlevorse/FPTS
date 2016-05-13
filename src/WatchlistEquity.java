import java.io.Serializable;

/**
 * @author Joey
 *         <p>
 *         A simplified version of Equity which holds a low and high trigger price.
 */
public class WatchlistEquity implements Serializable {

    private String ticker;
    private double lowTrigger = 0.0;
    private double highTrigger = 0.0;

    private boolean highTripped;
    private boolean lowTripped;

    /**
     * Create an instance of a watchlist equity.
     *
     * @param ticker      The equity ticker.
     * @param highTrigger The high trigger price for the equity.
     * @param lowTrigger  The low trigger price for the equity.
     */
    public WatchlistEquity(String ticker, double highTrigger, double lowTrigger) {
        this.ticker = ticker;
        this.lowTrigger = lowTrigger;
        this.highTrigger = highTrigger;
    }

    /**
     * Check the high and low triggers to the current value of the equity and notify the user appropriately.
     */
    public void checkTriggers() {
        // Check the high trigger
        if (PortfolioAdapter.findEquityByTicker(ticker).pricePerUnit > highTrigger && highTrigger != 0.0) {
            highTripped = true;
        }
        // Check the low trigger
        if (PortfolioAdapter.findEquityByTicker(ticker).pricePerUnit < lowTrigger && lowTrigger != 0.0) {
            lowTripped = true;
        }
    }

    /**
     * Reset the history for if the high and low triggers have been tripped in the past.
     */
    public void resetTripHistory() {
        highTripped = false;
        lowTripped = false;
    }

    /**
     * Get the ticker for the equity.
     *
     * @return The equity ticker.
     */
    public String getTicker() {
        return ticker;
    }

    /**
     * Get the high trigger price for the equity.
     *
     * @return The high trigger price.
     */
    public double getHighTrigger() {
        return highTrigger;
    }

    /**
     * Set the high trigger price of the watchlist equity.
     *
     * @param price The high trigger price of the equity.
     */
    public void setHighTrigger(double price) {
        if (price < 0.0) {
            price = 0.0;
        }
        this.highTrigger = price;
    }

    /**
     * GEt the low trigger price for the equity.
     *
     * @return The low trigger price.
     */
    public double getLowTrigger() {
        return lowTrigger;
    }

    /**
     * Set the low trigger price of the watchlist equity.
     *
     * @param price The low trigger price of the equity.
     */
    public void setLowTrigger(double price) {
        if (price < 0.0) {
            price = 0.0;
        }
        this.lowTrigger = price;
    }

    /**
     * Get whether or not the high trigger has tripped in the past.
     *
     * @return If the high trigger has tripped in the past.
     */
    public boolean hasHighTripped() {
        return highTripped;
    }

    /**
     * Get whether or not the low trigger has tripped in the past.
     *
     * @return If the low trigger has tripped in the past.
     */
    public boolean hasLowTripped() {
        return lowTripped;
    }
}
