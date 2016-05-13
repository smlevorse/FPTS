/**
 * @author Paul Hulbert
 * @contributors Sean Levorse
 * <p>
 * A representation of a single equity in the stock market.
 */
public class Equity extends Holding implements Visitable, HoldingIterator {

    /**
     * Construct an equity
     *
     * @param pricePerUnit price per one equity
     * @param identifier   ticker
     * @param unitsOwned   how many stocks are owned
     * @param name         the name
     */
    public Equity(double pricePerUnit, String identifier, double unitsOwned, String name) {
        super(pricePerUnit, identifier, unitsOwned);
        this.name = name;
    }

    /**
     * Searches for the price of the Equity and returns it
     *
     * @return the price per share of the equity
     */
    @Override
    public Double getUnitPrice() {

        return pricePerUnit;
    }

    @Override
    public void updateUnitPrice(Double newPrice) {
        this.pricePerUnit = newPrice;
    }

    /**
     * @return a copy of this holding object in a different address in memory
     */
    @Override
    public Holding clone() {
        return new Equity(this.pricePerUnit, this.identifier, this.unitsOwned, this.name);
    }

    @Override
    public String toString() {
        return this.name + " costs " + this.pricePerUnit + " identified by " + this.identifier + " and is part of " +
                this.indexSectorNames + "  ";
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


}
