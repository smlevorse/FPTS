/**
 * @author Steven Teplica
 *         <p>
 *         Each node to be graphed on the simulation graph
 */

public class GraphScore {

    double val;  // value of the node
    int days;   // amount of days the node spans

    /**
     * Create a GraphScore instance.
     */
    public GraphScore(double val, int days) {
        this.val = val;
        this.days = days;
    }

}
