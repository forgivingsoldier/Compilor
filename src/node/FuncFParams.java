package node;

import lexer.token;
import tool.filewritter;

import java.util.List;
//函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内重复多次
public class FuncFParams {
    public List<FuncFParam> funcFParams;

    public FuncFParams(List<FuncFParam> funcFParams) {
        this.funcFParams = funcFParams;
    }

    public void printToFile() {
        for (FuncFParam funcFParamNode : funcFParams) {
            funcFParamNode.printToFile();
            if (funcFParams.indexOf(funcFParamNode) != funcFParams.size() - 1)
                filewritter.printToken("COMMA", ",");
        }
        filewritter.printGrammer("FuncFParams");
    }
}
