package symbolTable;

public class ConstSym extends Symbol{
    public int dimension;
    public ConstSym(String name, int dimension) {
        super(name);
        this.dimension = dimension;
    }
}
