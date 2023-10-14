package node;

import tool.filewritter;

import java.util.List;

public class InitVal extends node{
    public Exp exp;
    public List<InitVal> initVals;

    public InitVal(Exp exp, List<InitVal> initVals) {
        this.exp = exp;
        this.initVals = initVals;
    }
    //// InitVal -> Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public void printToFile() {
        if (exp != null) {
            exp.printToFile();
        } else {
            filewritter.printToken("LBRACE","{");
            for (InitVal initVal : initVals) {
                initVal.printToFile();
                if(initVals.indexOf(initVal) != initVals.size()-1)
                    filewritter.printToken("COMMA",",");
            }
            filewritter.printToken("RBRACE","}");
        }
        filewritter.printGrammer("InitVal");
    }
}
