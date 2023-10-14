package node;
//常量初值 ConstInitVal → ConstExp| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值

import tool.filewritter;

import java.util.List;

public class ConstInitVal extends node{
    public ConstExp constExp;
    public List<ConstInitVal> constInitVals;

    public ConstInitVal(ConstExp constExp, List<ConstInitVal> constInitVals) {
        this.constExp = constExp;
        this.constInitVals = constInitVals;
    }
    // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public void printToFile() {
        if (constExp != null) {
            constExp.printToFile();
        } else {
            filewritter.printToken("LBRACE", "{");
            for (ConstInitVal constInitVal : constInitVals) {
                constInitVal.printToFile();
                if (constInitVals.indexOf(constInitVal) != constInitVals.size() - 1)
                    filewritter.printToken("COMMA", ",");
            }
            filewritter.printToken("RBRACE", "}");
        }
        filewritter.printGrammer("ConstInitVal");
    }
}
