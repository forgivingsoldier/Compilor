package llvm;

import java.util.Map;

public class SymbolTable {
    public Map<String, Symbol> table;
    public Integer level;
    public SymbolTable(Map<String, Symbol> table, Integer level) {
        this.table = table;
        this.level = level;
    }

    public void putSymbol(String value, Symbol symbol) {
        table.put(value, symbol);
    }

}
