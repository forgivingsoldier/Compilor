package node;

import lexer.token;
import tool.filewritter;

import java.util.List;

public class FuncRParams extends node{

    public List<Exp> exps;

    public FuncRParams(List<Exp> exps) {
        this.exps = exps;
    }
    // FuncRParams -> Exp { ',' Exp }
    public void printToFile() {
        for (Exp exp : exps) {
            exp.printToFile();
            if(exps.indexOf(exp) != exps.size() - 1)
                filewritter.printToken("COMMA", ",");
        }
        filewritter.printGrammer("FuncRParams");
    }
}
