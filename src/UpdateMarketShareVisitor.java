/**
 * @author David Egan
 *
 * Visit each holding contained in an indexSectorShare and calculate the final
 * unit price of that share
 */
public class UpdateMarketShareVisitor implements Visitor {

    private Double marketShareUnitPrice = 0.0;
    private int totalAmountEquities = 0;

    @Override
    public void visit(Equity eq) {
        marketShareUnitPrice += eq.getUnitPrice();
        totalAmountEquities += 1;
    }

    @Override
    public void visit(Holding holding) {
        //System.out.println("visiting holding");
    }

    @Override
    public void visit(IndexSectorShare iss) {
        marketShareUnitPrice += iss.getUnitPrice();
        totalAmountEquities += 1;

    }

    public Double getMarketShareUnitPrice() {
        return marketShareUnitPrice / totalAmountEquities;
    }
}
