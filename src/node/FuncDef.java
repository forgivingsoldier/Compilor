package node;

import lexer.token;
import tool.filewritter;

public class FuncDef {
    public FuncType funcType;
    public token ident;
    public FuncFParams funcFParams;
    public Block block;

    public FuncDef(FuncType funcType, token ident, FuncFParams funcFParams,
                       Block block) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }
    // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    public void printToFile() {
        funcType.printToFile();
        filewritter.printToken("IDENFR", ident.value);
        filewritter.printToken("LPARENT", "(");
        if (funcFParams != null) {
            funcFParams.printToFile();
        }
        filewritter.printToken("RPARENT", ")");
        if(block==null) System.out.println(ident.value);
        block.printToFile();
        filewritter.printGrammer("FuncDef");
    }
}
