package node;

import lexer.token;
import tool.filewritter;

public class MulExp extends node{
    public UnaryExp unaryExp;
    public token operator;
    public MulExp mulExp;

    public MulExp(UnaryExp unaryExp, token operator, MulExp mulExp) {
        this.unaryExp = unaryExp;
        this.operator = operator;
        this.mulExp = mulExp;
    }


    public void printToFile() {
        if (operator != null) {
            mulExp.printToFile();
            filewritter.printToken(operator.type.toString(), operator.value);
            unaryExp.printToFile();
        }
        else{
            unaryExp.printToFile();
        }
        filewritter.printGrammer("MulExp");
    }
}
