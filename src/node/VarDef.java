package node;

import lexer.token;
import tool.filewritter;

import java.util.List;

public class VarDef {
    public token ident;

    public List<ConstExp> constExps;
    public InitVal initVal;
    // VarDef -> Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public VarDef(token ident, List<ConstExp> constExps,  InitVal initVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.initVal = initVal;
    }

    public void printToFile() {
        filewritter.printToken("IDENFR",ident.value);

        for (ConstExp constExp : constExps) {
            filewritter.printToken("LBRACK","[");
            constExp.printToFile();
            filewritter.printToken("RBRACK","]");
        }
        if (initVal != null) {
            filewritter.printToken("ASSIGN","=");
            initVal.printToFile();
        }
        filewritter.printGrammer("VarDef");

    }
}
