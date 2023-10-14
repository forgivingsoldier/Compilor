package symbolTable;

import node.FuncFParam;

import java.util.List;

public class FuncSym extends Symbol {
    public String returnType;
    public int paramNum;
    public List<Param> Params;
    public FuncSym(String name, String returnType, List<Param> Params) {
        super(name);
        this.returnType = returnType;
        this.Params = Params;
        if(Params!=null)
            this.paramNum = Params.size();
        else this.paramNum = 0;
    }
}
