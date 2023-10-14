package node;
import lexer.token;
import tool.filewritter;

import java.util.List;

//变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
public class VarDecl {
    public List<VarDef> varDefs;
    public VarDecl(List<VarDef> varDefs) {
        this.varDefs = varDefs;
    }

    public void printToFile() {
        filewritter.printToken("INTTK","int");
        for (VarDef varDef : varDefs) {
            varDef.printToFile();
            if(varDefs.indexOf(varDef) != varDefs.size()-1)
                filewritter.printToken("COMMA",",");
        }
        filewritter.printToken("SEMICN",";");
        filewritter.printGrammer("VarDecl");
    }
}
