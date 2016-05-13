import java.util.ArrayList;


/**
 * @author Paul Hulbert
 * @contributor Sean Levorse
 * <p>
 * A share which is made up in part by different equities.  Each equity
 * has equal weight in deciding the price of it.
 */
public class IndexSectorShare extends Holding implements Visitable {

    /**
     * Create IndexSectorShare
     */
    public IndexSectorShare(String identifier, double unitsOwned) {
        super(0, identifier, unitsOwned);
        pricePerUnit = getUnitPrice();
    }

    @Override
    /**
     * Calculate price of share by letting a visitor visit all shares that are
     * part of the index/sector and handle calculation.
     */
    public Double getUnitPrice() {

        UpdateMarketShareVisitor visitor = new UpdateMarketShareVisitor();

        ArrayList<Visitable> holdings = new ArrayList<>();
        double total = 0;

        for (Visitable holding : PortfolioAdapter.equities) {
            if (isInIndex((Holding) holding)) {
                holding.accept(visitor);
            }
        }

        return visitor.getMarketShareUnitPrice();
    }

    @Override
    public void updateUnitPrice(Double newPrice) {

    }


    public boolean isInIndex(Holding holding) {
        return holding.indexSectorNames.contains(identifier);
    }

    /**
     * @return a copy of this holding object in a different address in memory
     */
    @Override
    public Holding clone() {
        return new IndexSectorShare(this.identifier, this.unitsOwned);
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
