package node;
import lexer.token;
import tool.filewritter;

import java.util.List;

public class LVal extends node{
    public token ident;
    public List<Exp> exps;

    public LVal(token ident, List<Exp> exps) {
        this.ident = ident;
        this.exps = exps;
    }
    // // LVal -> Ident {'[' Exp ']'}
    public void printToFile() {
        filewritter.printToken(ident.type.toString(), ident.value);
        for (Exp exp : exps) {
            filewritter.printToken("LBRACK", "[");
            exp.printToFile();
            filewritter.printToken("RBRACK", "]");
        }
        filewritter.printGrammer("LVal");
    }
}
