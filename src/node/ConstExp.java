package node;

import tool.filewritter;

public class ConstExp extends node{
    public AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }
    // ConstExp -> AddExp
    public void printToFile() {
        addExp.printToFile();
        filewritter.printGrammer("ConstExp");
    }
}
