package node;

import lexer.token;
import tool.filewritter;

public class RelExp extends node{
    public AddExp addExp;
    public token operator;
    public RelExp relExp;


    public RelExp(AddExp addExp, token operator, RelExp relExp) {
        this.addExp = addExp;
        this.operator = operator;
        this.relExp = relExp;
    }

    public void printToFile() {
        if (operator != null) {
            relExp.printToFile();
            filewritter.printToken(operator.type.toString(),operator.value);
            addExp.printToFile();
        }
        else {
            addExp.printToFile();
        }
        filewritter.printGrammer("RelExp");
    }
}
