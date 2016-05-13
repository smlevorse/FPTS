/**
 * @author David Egan
 *
 * Define interface for a Visitable element per the Visitor design pattern
 */
public interface Visitable {
    public void accept(Visitor visitor);
}
