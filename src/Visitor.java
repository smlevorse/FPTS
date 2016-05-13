/**
 * @author David Egan
 *
 * Define interface for a Visitor
 */
public interface Visitor {
    public void visit(Equity eq);

    public void visit(Holding holding);

    public void visit(IndexSectorShare iss);
}
