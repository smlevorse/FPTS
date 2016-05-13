/**
 * @author Sean Levorse
 *         <p>
 *         An iterator to move through holdings in a simple defined manner.
 */
public interface HoldingIterator {

    /**
     * @return Returns the first item in the iterator
     */
    public String first();

    /**
     * @return Returns the next item in the iterator
     */
    public String next();

    /**
     * @return If the iterator is at the last item in the list
     */
    public boolean isDone();

    /**
     * @return The current item the iterator is looking at
     */
    public String getCurrent();
}
