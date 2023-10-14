package node;
import tool.filewritter;


import java.util.List;

//常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
//基本类型 BType → 'int' // 存在即可
public class ConstDecl {
    public List<ConstDef> constDefs;
    public ConstDecl(List<ConstDef> constDefs) {
        this.constDefs = constDefs;

    }
    // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
    public void printToFile() {
        filewritter.printToken("CONSTTK", "const");
        filewritter.printToken("INTTK", "int");
        for (ConstDef constDef : constDefs) {
            constDef.printToFile();
            //如果不是最后一个，就打印逗号
            if (constDefs.indexOf(constDef) != constDefs.size() - 1)
                filewritter.printToken("COMMA", ",");
        }
        filewritter.printToken("SEMICN", ";");
        filewritter.printGrammer("ConstDecl");
    }
}
