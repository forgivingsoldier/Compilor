package node;

import tool.filewritter;

public class LOrExp extends node{
    public LAndExp lAndExp;
    public LOrExp lOrExp;
    public int yesLabel;
    public int noLabel;
    public int nextLabel;

    public LOrExp(LAndExp lAndExp, LOrExp lOrExp) {
        this.lAndExp = lAndExp;
        this.lOrExp = lOrExp;
    }

    public void printToFile() {


        if (lOrExp != null) {
            lOrExp.printToFile();
            filewritter.printToken("OR","||");
            lAndExp.printToFile();
        }
        else {
            lAndExp.printToFile();
        }
        filewritter.printGrammer("LOrExp");
    }
}
