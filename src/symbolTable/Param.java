package symbolTable;

public class Param extends Symbol{
    public int dimension;
    public Param(String name, int dimension) {
        super(name);
        this.dimension = dimension;
    }
}
