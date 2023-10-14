package node;
import tool.filewritter;
public class Decl {

    public ConstDecl constDecl;
    public VarDecl varDecl;

    public Decl(ConstDecl constDecl, VarDecl varDecl) {
        this.constDecl = constDecl;
        this.varDecl = varDecl;
    }

    public void printToFile() {
        if (constDecl != null) {
            constDecl.printToFile();
        } else  {
            varDecl.printToFile();
        }
    }
}
