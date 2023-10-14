package symbolTable;

import java.util.HashMap;

public class SymbolTableItem {
    public HashMap<String, Symbol> Symbols= new HashMap<>();
    public boolean isFunc ;
    public String returnType;
    public SymbolTableItem(boolean isFunc, String returnType) {
        this.isFunc = isFunc;
        this.returnType = returnType;
    }
    public void addSymbol(Symbol symbol){
        Symbols.put(symbol.name,symbol);
    }

}
