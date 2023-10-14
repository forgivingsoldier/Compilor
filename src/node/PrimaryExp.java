package node;

import tool.filewritter;

//基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
public class PrimaryExp extends node{

    public Exp exp;
    public LVal lVal;
    public Number_ number;

    public PrimaryExp(Exp exp) {
        this.exp = exp;
    }

    public PrimaryExp(LVal lVal) {
        this.lVal= lVal;
    }

    public PrimaryExp(Number_ number) {
        this.number = number;
    }
    // // PrimaryExp -> '(' Exp ')' | LVal | Number
    public void printToFile() {
        if(exp != null){
            filewritter.printToken("LPARENT", "(");
            exp.printToFile();
            filewritter.printToken("RPARENT", ")");
        }else if(lVal != null){
            lVal.printToFile();
        }else if(number != null){
            number.printToFile();
        }
        filewritter.printGrammer("PrimaryExp");
    }

}
