package node;

import lexer.token;
import tool.filewritter;

public class AddExp extends node{
    public MulExp mulExp;
    public token operator;
    public AddExp addExp;

    public AddExp(MulExp mulExp, token operator, AddExp addExp) {
        super();
        this.mulExp = mulExp;
        this.operator = operator;
        this.addExp = addExp;
    }

    public void printToFile() {
        if (operator != null) {
            addExp.printToFile();
            filewritter.printToken(operator.type.toString(), operator.value);
            mulExp.printToFile();
        }
        else{
            mulExp.printToFile();
        }
        filewritter.printGrammer("AddExp");
    }
}
