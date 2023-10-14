package node;

import lexer.token;
import tool.filewritter;

import java.util.List;

public class ConstDef {
    public token ident;
    public List<ConstExp> constExps;
    public ConstInitVal constInitVal;

    public ConstDef(token ident, List<ConstExp> constExps, ConstInitVal constInitVal) {
        this.ident = ident;
        this.constExps = constExps;
        this.constInitVal = constInitVal;
    }

    //// ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
    public void printToFile() {
        filewritter.printToken("IDENFR", ident.value);
        if (constExps != null)
            for (ConstExp constExp : constExps) {
                filewritter.printToken("LBRACK", "[");
                constExp.printToFile();
                filewritter.printToken("RBRACK", "]");
            }
        filewritter.printToken("ASSIGN", "=");
        constInitVal.printToFile();
        filewritter.printGrammer("ConstDef");

    }
}
