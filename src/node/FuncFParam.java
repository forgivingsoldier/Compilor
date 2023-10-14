package node;

import lexer.token;
import tool.filewritter;

import java.util.List;

//函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
public class FuncFParam {

    public token ident;
    public boolean isLrack;
    public List<ConstExp> constExps;

    public FuncFParam(token ident, boolean isLrack,List<ConstExp> constExps) {
        this.ident = ident;
        this.isLrack = isLrack;
        this.constExps = constExps;
    }

    public void printToFile() {
        filewritter.printToken("INTTK","int");
        filewritter.printToken("IDENFR",ident.value);
        if (isLrack) {
            filewritter.printToken("LBRACK","[");
            filewritter.printToken("RBRACK","]");
        }
        for (ConstExp constExp : constExps) {
            filewritter.printToken("LBRACK","[");
            constExp.printToFile();
            filewritter.printToken("RBRACK","]");
        }
        filewritter.printGrammer("FuncFParam");

    }
}
