package error;


import node.*;
import symbolTable.*;
import error.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CheckError {
    public static final int VOID_RETURN_TYPE = -1;
    public static final int NON_VOID_RETURN_TYPE = 0;
    private Symbol getSymbol(String ident, List<SymbolTableItem> symbolTable) {
        for (int i = symbolTable.size() - 1; i >= 0; i--) {
            if (symbolTable.get(i).Symbols.containsKey(ident)) {
                return symbolTable.get(i).Symbols.get(ident);
            }
        }
        return null;
    }


    //非法符号	a	格式字符串中出现非法字符报错行号为 <FormatString> 所在行数。	<FormatString> → ‘“‘{<Char>}’”
    public boolean checkA(String source, int position) {
        int cur = source.charAt(position);
        if (cur == 32 || cur == 33 || (cur >= 40 && cur <= 126)) {
            return cur == 92 && source.charAt(position + 1) != 'n';
        } else if (cur == 37) {
            return source.charAt(position + 1) != 'd';
        }
        return true;
    }


    //名字重定义	b	函数名或者变量名在当前作用域下重复定义。注意，变量一定是同一级作用域下才会判定出错，不同级作用域下，内层会覆盖外层定义。报错行号为 <Ident> 所在行数。	<ConstDef>→<Ident> …
    //<VarDef>→<Ident> … <Ident> …
    //<FuncDef>→<FuncType><Ident> …
    //<FuncFParam> → <BType> <Ident> …
    public boolean checkB(String ident, List<SymbolTableItem> symbolTableItems) {
        SymbolTableItem symbolTableItem = symbolTableItems.get(symbolTableItems.size() - 1);
        return symbolTableItem.Symbols.containsKey(ident);
    }


    public boolean checkC(String ident, List<SymbolTableItem> symbolTableItems) {
        for (SymbolTableItem symbolTableItem : symbolTableItems) {
            if (symbolTableItem.Symbols.containsKey(ident)) {
                return false;
            }
        }
        return true;
    }


    public String checkDandE(UnaryExp unaryExp, List<SymbolTableItem> symbolTable) {
        Symbol symbol = null;
        symbol = getSymbol(unaryExp.ident.value, symbolTable);
        if (!(symbol instanceof FuncSym)) {
            return "e";
        }
        FuncSym funcSym = (FuncSym) symbol;
        if (unaryExp.funcRParams == null) {
            if (funcSym.Params != null) {
                if (funcSym.Params.size() != 0) {
                    return "d";
                }
            }
        }
        else if(funcSym.Params==null){
            if(unaryExp.funcRParams.exps.size()!=0){
                return "d";
            }
        }
        else if (funcSym.Params.size() != unaryExp.funcRParams.exps.size()) {
            return "d";
        } else {
            //之前定义的形参
            List<Integer> FDimension = new ArrayList<>();
            for (Param param : funcSym.Params) {
                FDimension.add(param.dimension);
            }
            //传入的实参
            List<Integer> RDimension = new ArrayList<>();
            if (unaryExp.funcRParams != null) {
                ErrorHandling.getErrorHandling().checkFuncRParams(unaryExp.funcRParams);
                for (Exp exp : unaryExp.funcRParams.exps) {
                    Param param = getParamFromExp(exp, symbolTable);
                    if (param != null) {
                        if (param.name != null) {
                            Symbol symbolR = null;
                            symbolR = getSymbol(param.name, symbolTable);

                            if (symbolR instanceof VarSym) {
                                RDimension.add(((VarSym) symbolR).dimension - param.dimension);
                            } else if (symbolR instanceof ConstSym) {
                                RDimension.add(((ConstSym) symbolR).dimension - param.dimension);
                            } else if (symbolR instanceof FuncSym) {
                                FuncSym funcSym1 = (FuncSym) symbolR;
                                if (funcSym1.returnType.equals("void")) {

                                    RDimension.add(-1);
                                } else {

                                    RDimension.add(0);
                                }
                            }
                        } else {

                            RDimension.add(param.dimension);
                        }
                    }
                }
            }
            if (!checkSame(FDimension, RDimension)) {
                return "e";
            }
        }
        return "no error";
    }

    //无返回值的函数存在不匹配的return语句	f	报错行号为 ‘return’ 所在行号。	<Stmt>→‘return’ {‘[’<Exp>’]’}‘;’
    public boolean checkF(List<SymbolTableItem> symbolTableItems, Stmt stmt) {
        for (int i = symbolTableItems.size() - 1; i >= 0; i--) {
            if (symbolTableItems.get(i).isFunc) {
                if (symbolTableItems.get(i).returnType.equals("void") && stmt.exp != null) {
                    return true;
                }
            }
        }
        return false;
    }

    //有返回值的函数缺少return语句	g	只需要考虑函数末尾是否存在return语句，无需考虑数据流。报错行号为函数结尾的’}’ 所在行号。	<FuncDef> → <FuncType> <Ident> ‘(’ [<FuncFParams>] ‘)’ <Block>
    //<MainFuncDef> → ‘int’ ‘main’ ‘(’ ‘)’ <Block>
    public boolean checkG(List<SymbolTableItem> symbolTableItems, Block block) {
        SymbolTableItem currentSymbolTableItem = symbolTableItems.get(symbolTableItems.size() - 1);
        if (currentSymbolTableItem.isFunc && currentSymbolTableItem.returnType.equals("int")) {
            if (block.blockItems == null || block.blockItems.isEmpty()||block.blockItems.get(block.blockItems.size() - 1).stmt == null || !block.blockItems.get(block.blockItems.size() - 1).stmt.type.toString().equals("RETURN")) {
                return true;
            }
        }
        return false;
    }

    //不能改变常量的值	h	<LVal>为常量时，不能对其修改。报错行号为 <LVal> 所在行号。	<Stmt>→<LVal>‘=’ <Exp>‘;’<Stmt>→<LVal>‘=’ ‘getint’ ‘(’ ‘)’ ‘;’
    public boolean checkH(String ident, List<SymbolTableItem> symbolTableItems) {

        for (int i = symbolTableItems.size() - 1; i >= 0; i--) {
            if (symbolTableItems.get(i).Symbols.containsKey(ident)) {
                if (symbolTableItems.get(i).Symbols.get(ident) instanceof ConstSym) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }



    //printf中格式字符与表达式个数不匹配	l	报错行号为 ‘printf’ 所在行号。	<Stmt> →‘printf’‘(’<FormatString>{,<Exp>}’)’‘;’
    public boolean checkL(String formatString, int ExpCount) {
        int count = 0;
        for (int i = 0; i < formatString.length(); i++) {
            if (formatString.charAt(i) == '%') {
                count++;
            }
        }
        if (count != ExpCount) {
            return true;
        } else {
            return false;
        }
    }


    //函数参数个数不匹配	d	函数调用语句中，参数个数与函数定义中的参数个数不匹配。报错行号为函数调用语句的函数名所在行数。	<UnaryExp>→<Ident>‘(’[<FuncRParams>]‘)’

    //函数参数类型不匹配	e	函数调用语句中，参数类型与函数定义中对应位置的参数类型不匹配。报错行号为函数调用语句的函数名所在行数。	<UnaryExp>→<Ident>‘(’[<FuncRParams>]‘)’

    public boolean checkSame(List<Integer> Fdimensions, List<Integer> Rdimensions) {
        //打印两个维度
//        System.out.println("Fdimensions:");
//        for (int i = 0; i < Fdimensions.size(); i++) {
//            System.out.print(Fdimensions.get(i) + " ");
//        }
//        System.out.println();
//        System.out.println("Rdimensions:");
//        for (int i = 0; i < Rdimensions.size(); i++) {
//            System.out.print(Rdimensions.get(i) + " ");
//        }
        return Objects.equals(Fdimensions, Rdimensions);
    }

    // Exp -> AddExp
    public Param getParamFromExp(Exp exp, List<SymbolTableItem> symbolTable) {
        return getParamFromAddExp(exp.addExp, symbolTable);
    }

    // AddExp -> MulExp | MulExp ('+' | '-') AddExp
    private Param getParamFromAddExp(AddExp addExp, List<SymbolTableItem> symbolTable) {
        return getParamFromMulExp(addExp.mulExp, symbolTable);
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private Param getParamFromMulExp(MulExp mulExp, List<SymbolTableItem> symbolTable) {
        return getParamFromUnaryExp(mulExp.unaryExp, symbolTable);
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp
    private Param getParamFromUnaryExp(UnaryExp unaryExp, List<SymbolTableItem> symbolTable) {
        if (unaryExp.primaryExp != null) {

            return getParamFromPrimaryExp(unaryExp.primaryExp, symbolTable);
        } else if (unaryExp.ident != null) {

            Symbol symbol = null;
            for (int i = symbolTable.size() - 1; i >= 0; i--) {
                if (symbolTable.get(i).Symbols.containsKey(unaryExp.ident.value)) {
                    symbol = symbolTable.get(i).Symbols.get(unaryExp.ident.value);

                }
            }
            if (symbol instanceof FuncSym) {
                FuncSym funcSym = (FuncSym) symbol;
                if (funcSym.returnType.equals("void")) {
                    return new Param(funcSym.name, VOID_RETURN_TYPE);
                } else {
                    return new Param(funcSym.name, NON_VOID_RETURN_TYPE);
                }


            } else {
                return null;
            }
        } else {
            return getParamFromUnaryExp(unaryExp.unaryExp, symbolTable);
        }
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    private Param getParamFromPrimaryExp(PrimaryExp primaryExp, List<SymbolTableItem> symbolTable) {
        if (primaryExp.exp != null) {

            return getParamFromExp(primaryExp.exp, symbolTable);
        } else if (primaryExp.lVal != null) {

            return getParamFromLVal(primaryExp.lVal, symbolTable);
        } else {

            return new Param(null, 0);
        }
    }
   // LVal → Ident {'[' Exp ']'}
    private Param getParamFromLVal(LVal lVal, List<SymbolTableItem> symbolTable) {

        return new Param(lVal.ident.value, lVal.exps.size());
    }
}
