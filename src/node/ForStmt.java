package node;

import tool.filewritter;

//语句 ForStmt → LVal '=' Exp // 存在即可
public class ForStmt {
    public LVal lVal;
    public Exp exp;

    public ForStmt(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    public void printToFile() {
        lVal.printToFile();
        filewritter.printToken("ASSIGN","=");
        exp.printToFile();
        filewritter.printGrammer("ForStmt");
    }
}
