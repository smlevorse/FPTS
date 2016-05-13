import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Paul Hubert
 * @contributors Sean Levorse
 * <p>
 * Holding abstract class for the system
 */
public abstract class Holding implements Serializable, HoldingIterator {

    public String identifier;  // the ticker or id of the holding
    public double unitsOwned;  // how many units of the holding are owned
    public String name;
    public ArrayList<String> indexSectorNames;  //the Indexes or Sectors the holding belongs to or is
    protected double pricePerUnit; // price per holding
    protected int loc;  // location of the iterator 'cursor'

    /**
     * @param pricePerUnit unit price
     * @param identifier   ticker or index name
     * @param unitsOwned   how many units owned
     */
    public Holding(double pricePerUnit, String identifier, double unitsOwned) {
        this.pricePerUnit = pricePerUnit;
        this.identifier = identifier;
        this.unitsOwned = unitsOwned;
        name = identifier;
        indexSectorNames = new ArrayList<>();
        loc = 0;
    }

    /**
     * Get the unit price of this Holding
     *
     * @return price
     */
    public abstract Double getUnitPrice();


    public abstract void updateUnitPrice(Double newPrice);

    /**
     * @return a copy of this holding object in a different address in memory
     */
    public abstract Holding clone();

    //region Holding Iterator stuff

    /**
     * @return Returns the first item in the iterator
     */
    public String first() {
        loc = 0;
        return name;
    }

    /**
     * @return Returns the next item in the iterator
     */
    public String next() {
        loc++;
        return getCurrent();
    }

    ;

    /**
     * @return If the iterator is at the last item in the list
     */
    public boolean isDone() {
        return loc >= indexSectorNames.size() + 2;
    }

    /**
     * @return The current item the iterator is looking at
     */
    public String getCurrent() {
        if (loc == 0) {
            return name;
        } else if (loc == 1) {
            return identifier;
        } else if (loc < indexSectorNames.size() + 2) {
            return indexSectorNames.get(loc - 2);
        } else {
            return null;
        }
    }

    ;
}
