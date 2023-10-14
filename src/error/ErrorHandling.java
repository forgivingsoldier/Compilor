package error;

import node.*;
import symbolTable.*;
import tool.*;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandling {
    public CheckError checkError = new CheckError();
    public List<Error> errors = new ArrayList<>();
    public List<SymbolTableItem> symbolTable = new ArrayList<>();

    public SymbolTableItem currentSymbolTableItem = null;
    //循环次数
    public int loopDepth = 0;

    public void addError(Error error) {
        if (errors == null) {
            return;
        }
        //同一行的错误只报告第一个
        for (Error error1 : errors) {
            if (error1.line == error.line) {
                return;
            }
        }
        errors.add(error);
    }

    private static final ErrorHandling errorHandling = new ErrorHandling();

    public static ErrorHandling getErrorHandling() {
        return errorHandling;
    }

    public void checkCompunit(CompUnit compUnit) {
        currentSymbolTableItem = new SymbolTableItem(false, "void");
        symbolTable.add(currentSymbolTableItem);
        for (Decl decl : compUnit.decls) {
            checkDecl(decl);
        }
        for (FuncDef funcDef : compUnit.funcDefs) {
            checkFuncDef(funcDef);
        }
        checkMainFunc(compUnit.mainFuncDef);
        symbolTable.remove(currentSymbolTableItem);
        printToFile();
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
    public void checkMainFunc(MainFuncDef mainFuncDef) {
        currentSymbolTableItem.addSymbol(new FuncSym("main", "int", null));
        currentSymbolTableItem = new SymbolTableItem(true, "int");
        symbolTable.add(currentSymbolTableItem);
        checkBlock(mainFuncDef.block);
        symbolTable.remove(currentSymbolTableItem);
        currentSymbolTableItem = symbolTable.get(symbolTable.size() - 1);
    }

    // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    public void checkFuncDef(FuncDef funcDef) {
        if (checkError.checkB(funcDef.ident.value, symbolTable)) {
            addError(new Error(funcDef.ident.line, "b"));
        }
        if (funcDef.funcFParams == null) {
            FuncSym funcSym = new FuncSym(funcDef.ident.value, funcDef.funcType.type.value, null);
            currentSymbolTableItem.addSymbol(funcSym);
        } else {
           
            List<Param> Params = new ArrayList<>();
            for (FuncFParam funcFParam : funcDef.funcFParams.funcFParams) {
                int dimension = 0;
                if (funcFParam.isLrack)
                    dimension++;
                dimension += funcFParam.constExps.size();
                
                Param param = new Param(funcFParam.ident.value, dimension);
                Params.add(param);
            }
            FuncSym funcSym = new FuncSym(funcDef.ident.value, funcDef.funcType.type.value, Params);
            currentSymbolTableItem.addSymbol(funcSym);
        }

        currentSymbolTableItem = new SymbolTableItem(true, funcDef.funcType.type.value);
        symbolTable.add(currentSymbolTableItem);
        if (funcDef.funcFParams != null) {
            checkFuncFParams(funcDef.funcFParams);
        }
        checkBlock(funcDef.block);
        symbolTable.remove(currentSymbolTableItem);
        currentSymbolTableItem = symbolTable.get(symbolTable.size() - 1);
    }


    // FuncFParams -> FuncFParam { ',' FuncFParam }
    public void checkFuncFParams(FuncFParams funcFParams) {
        for (FuncFParam funcFParam : funcFParams.funcFParams) {
            checkFuncFParam(funcFParam);
        }

    }

    // FuncFParam -> BType Ident [ '[' ']' { '[' ConstExp ']' }]
    public void checkFuncFParam(FuncFParam funcFParam) {
        if (checkError.checkB(funcFParam.ident.value, symbolTable)) {
            addError(new Error(funcFParam.ident.line, "b"));
        }
        if (funcFParam.constExps != null) {
            for (ConstExp constExp : funcFParam.constExps) {
                checkConstExp(constExp);
            }
        }
        //计算维数
        int dimension = 0;
        if (funcFParam.isLrack) {
            dimension += 1;
        }
        if (funcFParam.constExps != null) {
            dimension += funcFParam.constExps.size();
        }

        VarSym arraySym = new VarSym(funcFParam.ident.value, dimension);
        currentSymbolTableItem.addSymbol(arraySym);
    }

    public void checkDecl(Decl decl) {
        if (decl.constDecl != null) {
            checkConstDecl(decl.constDecl);
        } else {
            checkVarDecl(decl.varDecl);
        }
    }

    // VarDecl -> BType VarDef { ',' VarDef } ';'
    public void checkVarDecl(VarDecl varDecl) {
        for (VarDef varDef : varDecl.varDefs) {
            checkVarDef(varDef);
        }
    }

    // VarDef -> Ident { '[' ConstExp ']' } [ '=' InitVal ]
    public void checkVarDef(VarDef varDef) {
        if (checkError.checkB(varDef.ident.value, symbolTable)) {
            errors.add(new Error(varDef.ident.line, "b"));
        }
        if (varDef.constExps != null) {
            for (ConstExp constExp : varDef.constExps) {
                checkConstExp(constExp);
            }
        }
        VarSym varSym = new VarSym(varDef.ident.value, varDef.constExps.size());
        currentSymbolTableItem.addSymbol(varSym);

        if (varDef.initVal != null) {
            checkInitVal(varDef.initVal);
        }
    }

    // InitVal -> Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public void checkInitVal(InitVal initVal) {
        if (initVal.exp != null) {
            checkExp(initVal.exp);
        } else {
            for (InitVal initVal1 : initVal.initVals) {
                checkInitVal(initVal1);
            }
        }

    }


    // // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
    public void checkConstDecl(ConstDecl constDecl) {
        for (ConstDef constDef : constDecl.constDefs) {
            checkConstDef(constDef);
        }
    }

    // // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
    public void checkConstDef(ConstDef constDef) {
        if (checkError.checkB(constDef.ident.value, symbolTable)) {
            addError(new Error(constDef.ident.line, "b"));
        }
        if (constDef.constInitVal.constExp != null) {
            for (ConstExp constExp : constDef.constExps) {
                checkConstExp(constExp);
            }
        }
        ConstSym constSym = new ConstSym(constDef.ident.value, constDef.constExps.size());
        currentSymbolTableItem.addSymbol(constSym);
        checkConstInitVal(constDef.constInitVal);

    }

    //// ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public void checkConstInitVal(ConstInitVal constInitVal) {
        if (constInitVal.constExp != null) {
            checkConstExp(constInitVal.constExp);
        } else {
            for (ConstInitVal constInitVal1 : constInitVal.constInitVals) {
                checkConstInitVal(constInitVal1);
            }
        }
    }

    // Block -> '{' { BlockItem } '}'
    public void checkBlock(Block block) {
        for (BlockItem blockItem : block.blockItems) {
            checkBlockItem(blockItem);
        }
        if (checkError.checkG(symbolTable, block)) {
            addError(new Error(block.rightBrace.line, "g"));
        }

    }

    // BlockItem -> Decl | Stmt
    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem.constDecl != null) {
            checkConstDecl(blockItem.constDecl);
        } else if (blockItem.varDecl != null) {
            checkVarDecl(blockItem.varDecl);
        } else if (blockItem.stmt != null) {
            checkStmt(blockItem.stmt);
        } else {

        }
    }

    // Stmt -> LVal '=' Exp ';'
    //	| [Exp] ';'
    //	| Block
    //	| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //	| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //	| 'break' ';' | 'continue' ';'
    //	| 'return' [Exp] ';'
    //	| LVal '=' 'getint' '(' ')' ';'
    //	| 'printf' '(' FormatString { ',' Exp } ')' ';'
    public void checkStmt(Stmt stmt) {
        Stmt.Type type = stmt.type;
        if (type == Stmt.Type.EXP) {
            if (stmt.exp != null) {
                checkExp(stmt.exp);
            }
        } else if (type == Stmt.Type.BLOCK) {
            currentSymbolTableItem = new SymbolTableItem(false, null);
            symbolTable.add(currentSymbolTableItem);
            checkBlock(stmt.block);
            symbolTable.remove(currentSymbolTableItem);
            currentSymbolTableItem = symbolTable.get(symbolTable.size() - 1);
        } else if (type == Stmt.Type.IF) {
            checkCond(stmt.cond);
            checkStmt(stmt.stmts.get(0));
            if (stmt.stmts.get(1) != null) {
                checkStmt(stmt.stmts.get(1));
            }
        } else if (type == Stmt.Type.FOR) {
            if (stmt.forstmt1 != null) {
                checkForStmt(stmt.forstmt1);
            }
            if (stmt.cond != null) {
                checkCond(stmt.cond);
            }
            if (stmt.forstmt2 != null) {
                checkForStmt(stmt.forstmt2);
            }
            loopDepth += 1;
            checkStmt(stmt.stmt);
            loopDepth -= 1;
        } else if (type == Stmt.Type.BREAK) {
            if (loopDepth == 0) {
                addError(new Error(stmt.breakLine, "m"));
            }
        } else if (type == Stmt.Type.CONTINUE) {
            if (loopDepth == 0) {
                addError(new Error(stmt.continueLine, "m"));
            }
        } else if (type == Stmt.Type.RETURN) {
            if (checkError.checkF(symbolTable, stmt)) {
                addError(new Error(stmt.returnLine, "f"));
            }
            if (stmt.exp != null) {
                checkExp(stmt.exp);
            }
        } else if (type == Stmt.Type.GETINT) {
            if (checkError.checkH(stmt.lVal.ident.value, symbolTable)) {
                addError(new Error(stmt.lVal.ident.line, "h"));
            }
            checkLVal(stmt.lVal);
        } else if (type == Stmt.Type.PRINTF) {
            int ExpNum = stmt.exps.size();
            if (checkError.checkL(stmt.formatString, ExpNum)) {
                addError(new Error(stmt.printfLine, "l"));
            }
            for (Exp exp : stmt.exps) {
                checkExp(exp);
            }
        } else if (type == Stmt.Type.ASSIGN) {
            if (checkError.checkH(stmt.lVal.ident.value, symbolTable)) {
                addError(new Error(stmt.lVal.ident.line, "h"));
            }
            checkLVal(stmt.lVal);
            checkExp(stmt.exp);

        }
    }

    public void checkCond(Cond cond) {
        checkLOrExp(cond.lOrExp);
    }

    // // LOrExp -> LAndExp | LAndExp '||' LOrExp
    public void checkLOrExp(LOrExp lOrExp) {
        checkLAndExp(lOrExp.lAndExp);
        if (lOrExp.lOrExp != null) {
            checkLOrExp(lOrExp.lOrExp);
        }

    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    public void checkLAndExp(LAndExp lAndExp) {
        checkEqExp(lAndExp.eqExp);
        if (lAndExp.lAndExp != null) {
            checkLAndExp(lAndExp.lAndExp);
        }
    }

    public void checkEqExp(EqExp eqExp) {
        checkRelExp(eqExp.relExp);
        if (eqExp.eqExp != null) {
            checkEqExp(eqExp.eqExp);
        }
    }

    //  RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public void checkRelExp(RelExp relExp) {
        checkAddExp(relExp.addExp);
        if (relExp.relExp != null) {
            checkRelExp(relExp.relExp);
        }
    }

    // AddExp → MulExp | AddExp ('+' | '-') MulExp
    public void checkAddExp(AddExp addExp) {
        checkMulExp(addExp.mulExp);
        if (addExp.addExp != null) {
            checkAddExp(addExp.addExp);
        }
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public void checkMulExp(MulExp mulExp) {
        checkUnaryExp(mulExp.unaryExp);
        if (mulExp.mulExp != null) {
            checkMulExp(mulExp.mulExp);
        }
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp  // c d e j
    public void checkUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.primaryExp != null) {
            checkPrimaryExp(unaryExp.primaryExp);
        } else if (unaryExp.unaryExp != null) {
            checkUnaryExp(unaryExp.unaryExp);
        } else {
            if (checkError.checkC(unaryExp.ident.value, symbolTable)) {
                addError(new Error(unaryExp.ident.line, "c"));
                return;
            }
            String result=checkError.checkDandE(unaryExp, symbolTable);
            if(result.equals("d")){
                addError(new Error(unaryExp.ident.line, "d"));
            }
            else if(result.equals("e")){
                addError(new Error(unaryExp.ident.line, "e"));
            }
        }

    }

    // FuncRParams -> Exp { ',' Exp }
    public void checkFuncRParams(FuncRParams funcRParams) {
        for (Exp exp : funcRParams.exps) {
            checkExp(exp);
        }
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    public void checkPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.exp != null) {
            checkExp(primaryExp.exp);
        } else if (primaryExp.lVal != null) {
            checkLVal(primaryExp.lVal);
        }
}


    //ForStmt → LVal '=' Exp   //h
    public void checkForStmt(ForStmt forstmt) {
        if (checkError.checkH(forstmt.lVal.ident.value, symbolTable)) {
            addError(new Error(forstmt.lVal.ident.line, "h"));
        }
        checkLVal(forstmt.lVal);
        checkExp(forstmt.exp);

    }

    public void checkLVal(LVal lVal) {
        if (checkError.checkC(lVal.ident.value, symbolTable)) {
            addError(new Error(lVal.ident.line, "c"));
        }
        for (Exp exp : lVal.exps) {
            checkExp(exp);
        }

    }

    public void checkExp(Exp exp) {
        checkAddExp(exp.addExp);
    }

    public void checkConstExp(ConstExp constExp) {
        checkAddExp(constExp.addExp);
    }


    public void printToFile() {
        //按照行号排序
        errors.sort((o1, o2) -> {
            if (o1.line > o2.line) {
                return 1;
            } else if (o1.line < o2.line) {
                return -1;
            } else {
                return 0;
            }
        });
        //打印errors到error.txt
        filewritter filewritter = new filewritter();
        filewritter.printError(errors);
    }

}
