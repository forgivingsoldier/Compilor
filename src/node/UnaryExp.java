package node;

import lexer.token;
import tool.filewritter;
//一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp  3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况

public class UnaryExp extends node{
    public PrimaryExp primaryExp = null;
    public token ident = null;
    public FuncRParams funcRParams = null;
    public UnaryOp unaryOp = null;
    public UnaryExp unaryExp = null;

    public UnaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
    }

    public UnaryExp(token ident, FuncRParams funcRParams) {
        this.ident = ident;
        this.funcRParams = funcRParams;

    }

    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }
    // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public void printToFile() {
        if(primaryExp != null){
            primaryExp.printToFile();
        }else if(ident != null){
            filewritter.printToken(ident.type.toString(), ident.value);
            filewritter.printToken("LPARENT", "(");
            if(funcRParams != null){
                funcRParams.printToFile();
            }
            filewritter.printToken("RPARENT", ")");
        }else if(unaryOp != null){
            unaryOp.printToFile();
            unaryExp.printToFile();
        }
        filewritter.printGrammer("UnaryExp");
    }
}
