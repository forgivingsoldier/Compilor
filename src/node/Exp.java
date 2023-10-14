package node;

import tool.filewritter;

public class Exp extends node{
    public AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    public void printToFile() {
        addExp.printToFile();
        filewritter.printGrammer("Exp");
    }
}
