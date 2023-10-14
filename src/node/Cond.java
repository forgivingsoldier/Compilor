package node;

import tool.filewritter;

public class Cond extends node{
    public LOrExp lOrExp;
    public int yesLabel;
    public int noLabel;
    public int nextLabel;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    public void printToFile() {
        lOrExp.printToFile();
        filewritter.printGrammer("Cond");
    }
}
