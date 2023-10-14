package node;

import lexer.token;
import tool.filewritter;

public class EqExp extends node {
    public RelExp relExp;
    public token operator;
    public EqExp eqExp;

    public EqExp(RelExp relExp, token operator, EqExp eqExp) {
        this.relExp = relExp;
        this.operator = operator;
        this.eqExp = eqExp;
    }

    // // RelExp | EqExp ('==' | '!=') RelExp
    public void printToFile() {
        if (operator != null) {
            eqExp.printToFile();
            filewritter.printToken(operator.type.toString(), operator.value);
            relExp.printToFile();
        }
        else {
            relExp.printToFile();
        }
        filewritter.printGrammer("EqExp");
    }
}
