package symbolTable;

public class VarSym extends Symbol {
    public int dimension;
    public VarSym(String name, int dimension) {
        super(name);
        this.dimension = dimension;
    }
}
