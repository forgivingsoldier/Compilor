package node;

import tool.filewritter;

public class LAndExp extends node {
    public EqExp eqExp;
    public LAndExp lAndExp;
    public int yesLabel;
    public int noLabel;
    public int nextLabel;

    public LAndExp(EqExp eqExp, LAndExp lAndExp) {
        this.eqExp = eqExp;
        this.lAndExp = lAndExp;
    }

    public void printToFile() {
        if (lAndExp != null) {
            lAndExp.printToFile();
            filewritter.printToken("AND", "&&");
            eqExp.printToFile();
        }
        else {
            eqExp.printToFile();
        }
        filewritter.printGrammer("LAndExp");
    }
}
