package node;

import tool.filewritter;

public class BlockItem {
    public ConstDecl constDecl;
    public VarDecl varDecl;
    public Stmt stmt;

    public BlockItem(ConstDecl constDecl, VarDecl varDecl, Stmt stmt) {
        this.constDecl = constDecl;
        this.varDecl = varDecl;
        this.stmt = stmt;
    }

    public void printToFile() {
        if (constDecl != null) {
            constDecl.printToFile();
        } else if (varDecl != null) {
            varDecl.printToFile();
        } else if (stmt != null) {
            stmt.printToFile();
        }
    }
}
