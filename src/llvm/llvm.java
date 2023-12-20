package llvm;

import node.*;
import tool.filewritter;

import java.util.*;

import tool.Pair;

public class llvm {
    public CompUnit compUnit;
    public int regId = 1;
    private List<SymbolTable> symbolTable;
    private int isFinalOr = 0;
    private int isFinalAnd = 0;

    public SymbolTable nowTable() {
        return symbolTable.get(symbolTable.size() - 1);
    }

    public Map<String, List<Symbol>> funcParams;

    public void pushTable() {
        symbolTable.add(new SymbolTable(new HashMap<>(), symbolTable.size()));
    }

    public SymbolTable getFuncTable() {
        return symbolTable.get(0);
    }

    public void addParam(Symbol symbol) {

    }

    public void addFunc(String ident) {
        funcParams.put(ident, new ArrayList<>());
    }

    public void popTable() {
        symbolTable.remove(symbolTable.size() - 1);
    }

    public Pair<Symbol, Integer> getSymbolTable(String value) {
        for (int i = symbolTable.size() - 1; i >= 0; i--) {
            if (symbolTable.get(i).table.containsKey(value)) {
                // 正确创建 Pair 对象并返回
                return new Pair<>(symbolTable.get(i).table.get(value), symbolTable.get(i).level);
            }
        }
        return null;
    }


    public llvm(CompUnit compUnit) {
        this.compUnit = compUnit;
        this.symbolTable = new ArrayList<>();
    }

    public void generate() {
        compUnit(compUnit);
    }

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    private void compUnit(CompUnit compUnit) {
        pushTable();
        printf("declare i32 @getint()          \n" +
                "declare void @putint(i32)      \n" +
                "declare void @putch(i32)      \n" +
                "declare void @putstr(i8*)      \n");
        for (Decl decl : compUnit.decls) {
            decl(decl);
        }
        for (FuncDef funcDef : compUnit.funcDefs) {
            funcDef(funcDef);
        }
        mainFuncDef(compUnit.mainFuncDef);

    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block /
    private void funcDef(FuncDef funcDef) {
        String funcType = funcDef.funcType.type.value;
        String ident = funcDef.ident.value;
        String type = null;
        if (funcType.equals("int")) {
            type = "i32";
        } else if (funcType.equals("void")) {
            type = "void";
        }
        printf("define dso_local " + type + " @" + ident + "(");
        Symbol symbol = new Symbol(null, null, funcType, ident);
        nowTable().putSymbol(funcDef.ident.value, symbol);

        pushTable();//先为函数创建一个符号表存形参

        if (funcDef.funcFParams != null) {
            funcFParams(funcDef.funcFParams);
        }
        printf(") {\n");

        block(funcDef.block, true);

        if (funcType.equals("void"))
            printf("ret void\n");
        printf("}\n");
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    private void funcFParams(FuncFParams funcFParams) {
        for (FuncFParam funcFParam : funcFParams.funcFParams) {
            funcFParam(funcFParam);
            //不是最后一个时输出逗号
            if (funcFParams.funcFParams.indexOf(funcFParam) != funcFParams.funcFParams.size() - 1)
                printf(", ");
        }
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维
    //数组变量 3.二维数组变量
    private void funcFParam(FuncFParam funcFParam) {
        if (!funcFParam.isLrack && funcFParam.constExps.isEmpty()) {
            printf("i32 %v" + regId);
            Symbol symbol = new Symbol(null, "%v" + regId, "i32", 0, 0, 0);
            nowTable().putSymbol(funcFParam.ident.value, symbol);
            regId++;
        }
        //一维数组
        else if (funcFParam.isLrack && funcFParam.constExps.isEmpty()) {
            printf("i32* %v" + regId);
            Symbol symbol = new Symbol(null, "%v" + regId, "i32*", 1, 0, 0);
            symbol.setD1(0);
            nowTable().putSymbol(funcFParam.ident.value, symbol);
            regId++;
        }
        //二维数组
        else {
            int level = nowTable().level;
            nowTable().level = 0;
            constExp(funcFParam.constExps.get(0));
            nowTable().level = level;

            printf("[" + funcFParam.constExps.get(0).value + " x i32]* %v" + regId);
            Symbol symbol = new Symbol(null, "%v" + regId, "[" + funcFParam.constExps.get(0).value + " x i32]*", 2, 0, 0);
            symbol.setD1(0);
            symbol.setD2(Integer.parseInt(funcFParam.constExps.get(0).value));
            nowTable().putSymbol(funcFParam.ident.value, symbol);
            regId++;

        }
    }

    private void decl(Decl decl) {
        if (decl.constDecl != null) {
            constDecl(decl.constDecl);
        } else {
            varDecl(decl.varDecl);
        }
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block
    private void mainFuncDef(MainFuncDef mainFuncDef) {
        printf("\ndefine dso_local i32 @main() {\n");
        block(mainFuncDef.block, false);
        printf("}\n");
    }

    // Block → '{' {BlockItem} '}'
    private void block(Block block, Boolean isFunc) {
        if (!isFunc)
            pushTable();
        //查找当前符号表regid为null的符号
        for (Symbol symbol : nowTable().table.values()) {
            if (symbol.regId == null) {
                printf("%v" + regId + " = alloca " + symbol.addrType + "\n");//todo i32?
                symbol.regId = "%v" + regId;
                printf("store " + symbol.addrType + " " + symbol.value + ", " + symbol.addrType + "* %v" + regId + "\n");//todo i32?
                regId++;
            }
        }
        for (BlockItem blockItem : block.blockItems) {
            blockItem.forstmt1Label = block.forstmt1Label;
            blockItem.condLabel = block.condLabel;
            blockItem.forstmt2Label = block.forstmt2Label;
            blockItem.stmtLabel = block.stmtLabel;
            blockItem.nextLabel = block.nextLabel;
            blockItem(blockItem);
        }
        popTable();
    }


    // BlockItem → Decl | Stmt
    private void blockItem(BlockItem blockItem) {
        if (blockItem.varDecl != null) {
            varDecl(blockItem.varDecl);
        } else if (blockItem.constDecl != null) {
            constDecl(blockItem.constDecl);
        } else {
            blockItem.stmt.forstmt1Label = blockItem.forstmt1Label;
            blockItem.stmt.condLabel = blockItem.condLabel;
            blockItem.stmt.forstmt2Label = blockItem.forstmt2Label;
            blockItem.stmt.stmtLabel = blockItem.stmtLabel;
            blockItem.stmt.nextLabel = blockItem.nextLabel;
            stmt(blockItem.stmt);
        }
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
    //数组、二维数组共三种情况
    private void constDecl(ConstDecl constDecl) {
        for (ConstDef constDef : constDecl.constDefs) {
            constDef(constDef);
        }
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private void constDef(ConstDef constDef) {
        String regid = null;
        String value = null;
        int dim_depth = 0;
        int dim_num1 = 0;
        int dim_num2 = 0;
        if (constDef.constExps.isEmpty()) {
            //局部常量
            if (nowTable().level > 0) {
                printf("%v" + regId + " = alloca i32\n");
                regid = "%v" + this.regId;
                value = "0";
                regId++;
                if (constDef.constInitVal != null) {
                    constInitVal(constDef.constInitVal);
                    value = constDef.constInitVal.value;
                }
                printf("store i32 " + value + ", i32* " + regid + "\n");

            }
            //全局常量
            else {
                value = "0";
                if (constDef.constInitVal != null) {
                    constInitVal(constDef.constInitVal);
                    value = constDef.constInitVal.value;
                }
                printf("@" + constDef.ident.value + " = dso_local global i32 " + value + "\n");
            }
            Symbol symbol = new Symbol(regid, value);
            System.out.println(constDef.ident.value);
            nowTable().putSymbol(constDef.ident.value, symbol);
        } else {
            //一维数组
            if (constDef.constExps.size() == 1) {
                dim_depth = 1;
                int level = nowTable().level;

                nowTable().level = 0;
                constExp(constDef.constExps.get(0));
                nowTable().level = level;

                dim_num1 = Integer.parseInt(constDef.constExps.get(0).value);
                //局部才用alloca,并搭配寄存器
                if (nowTable().level > 0) {
                    printf("%v" + regId + " = alloca [" + dim_num1 + " x i32]\n");
                    Symbol symbol = new Symbol("%v" + regId, "%v" + regId, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(constDef.ident.value, symbol);
                    regId++;
                }
                //全局存个ident的名字即可
                else {
                    Symbol symbol = new Symbol(null, null, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(constDef.ident.value, symbol);
                }
                Symbol symbol = nowTable().table.get(constDef.ident.value);

                symbol.setD1(dim_num1);
                System.out.println(symbol.d1Value.length);
                constDef.constInitVal.dim_num1 = dim_num1;
                constDef.constInitVal.dim_depth = dim_depth;
                constDef.constInitVal.dim_num2 = dim_num2;
                constDef.constInitVal.d1Value = symbol.d1Value;

                constInitVal(constDef.constInitVal);
                //全局常量
                if (nowTable().level == 0) {
                    printf("@" + constDef.ident.value + " = dso_local constant [" + dim_num1 + " x i32] [");
                    String[] d1Value = constDef.constInitVal.d1Value;
                    for (int i = 0; i < dim_num1; i++) {
                        if (i != dim_num1 - 1) {
                            printf("i32 " + d1Value[i] + ", ");
                        } else {
                            printf("i32 " + d1Value[i] + "]\n");
                        }
                    }
                }
                //局部常量
                else {
                    String[] d1Value = constDef.constInitVal.d1Value;
                    for (int i = 0; i < dim_num1; i++) {
                        if (!d1Value[i].equals("Null")) {
                            printf("%v" + regId + " = getelementptr [" + dim_num1 + " x i32], [" + dim_num1 + " x i32]* " + symbol.regId + ", i32 0, i32 " + i + "\n");
                            printf("store i32 " + d1Value[i] + ", i32* %v" + regId + "\n");
                            regId++;
                        }
                    }
                }
            }
            //二维数组
            else {
                dim_depth = 2;

                int level = nowTable().level;
                nowTable().level = 0;
                constExp(constDef.constExps.get(0));
                constExp(constDef.constExps.get(1));
                nowTable().level = level;

                dim_num1 = Integer.parseInt(constDef.constExps.get(0).value);
                dim_num2 = Integer.parseInt(constDef.constExps.get(1).value);
                //局部才用alloca
                if (nowTable().level > 0) {
                    printf("%v" + regId + " = alloca [" + dim_num1 + " x [ " + dim_num2 + " x i32]]\n");
                    Symbol symbol = new Symbol("%v" + regId, "%v" + regId, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(constDef.ident.value, symbol);
                    regId++;
                }
                //全局存个ident的名字即可
                else {
                    Symbol symbol = new Symbol(null, null, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(constDef.ident.value, symbol);
                }
                Symbol symbol = nowTable().table.get(constDef.ident.value);
                //通过符号来创建数组模板
                symbol.setD1(dim_num1);
                symbol.setD2(dim_num2);
                //设置节点的数组属性
                constDef.constInitVal.dim_num1 = dim_num1;
                constDef.constInitVal.dim_depth = dim_depth;
                constDef.constInitVal.dim_num2 = dim_num2;
                constDef.constInitVal.d2Value = symbol.d2Value;

                constInitVal(constDef.constInitVal);

                //全局常量
                if (nowTable().level == 0) {
                    printf("@" + constDef.ident.value + " = dso_local constant [" + dim_num1 + " x [" + dim_num2 + " x i32]] [[");
                    String[][] d2Value = constDef.constInitVal.d2Value;
                    for (int i = 0; i < dim_num1 - 1; i++) {
                        printf(dim_num2 + " x i32] [");
                        for (int j = 0; j < dim_num2 - 1; j++) {
                            printf("i32 " + d2Value[i][j] + ", ");
                        }
                        printf("i32 " + d2Value[i][dim_num2 - 1] + "], [");
                    }
                    printf(dim_num2 + " x i32] [");
                    for (int j = 0; j < dim_num2 - 1; j++) {
                        printf("i32 " + d2Value[dim_num1 - 1][j] + ", ");
                    }
                    printf("i32 " + d2Value[dim_num1 - 1][dim_num2 - 1] + "]]\n");
                }
                //局部常量
                else {
                    String[][] d2Value = constDef.constInitVal.d2Value;
                    for (int i = 0; i < dim_num1; i++) {
                        for (int j = 0; j < dim_num2; j++) {
                            if (!d2Value[i][j].equals("Null")) {
                                printf("%v" + regId + " = getelementptr [" + dim_num1 + " x [" + dim_num2 + " x i32]], [" + dim_num1 + " x [" + dim_num2 + " x i32]]* " + symbol.regId + ", i32 0, i32 " + i + ", i32 " + j + "\n");
                                printf("store i32 " + d2Value[i][j] + ", i32* %v" + regId + "\n");
                                regId++;
                            }
                        }
                    }
                }
            }
        }

    }

    // ConstInitVal → ConstExp| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
    //维数组初值
    private void constInitVal(ConstInitVal constInitVal) {
        if (constInitVal.constExp != null) {

            constExp(constInitVal.constExp);
            constInitVal.value = constInitVal.constExp.value;
        } else {
            //一维数组
            if (constInitVal.dim_depth == 1) {

                String[] d1v = constInitVal.d1Value;
                //将输入的值存在div中
                int index = 0;
                for (ConstInitVal div : constInitVal.constInitVals) {
                    constInitVal(div);
                    d1v[index] = div.value;
                    index++;
                }
                if (index < constInitVal.dim_num1) {
                    for (int i = index; i < constInitVal.dim_num1; i++) {
                        d1v[i] = (nowTable().level != 0) ? "Null" : "0";
                    }
                }
                constInitVal.d1Value = d1v;
            }
            //二维数组
            else {
                String[][] d2v = constInitVal.d2Value;
                //将输入的值存在d2v中
                int rowIndex = 0;
                for (ConstInitVal rowVal : constInitVal.constInitVals) {
                    if (rowVal.constInitVals != null) {
                        System.out.println(rowVal.dim_num2);
                        String[] row = new String[constInitVal.dim_num2];
                        int colIndex = 0;
                        for (ConstInitVal colVal : rowVal.constInitVals) {
                            constInitVal(colVal);
                            row[colIndex++] = colVal.value;
                        }
                        // 填充剩余的二维数组列元素
                        while (colIndex < constInitVal.dim_num2) {
                            row[colIndex++] = (nowTable().level != 0) ? "Null" : "0";
                        }
                        d2v[rowIndex++] = row;
                    }
                }
                // 填充剩余的二维数组行
                while (rowIndex < constInitVal.dim_num1) {
                    String[] row = new String[constInitVal.dim_num2];
                    Arrays.fill(row, (nowTable().level != 0) ? "Null" : "0");
                    d2v[rowIndex++] = row;
                }
                constInitVal.d2Value = d2v;
            }

        }
    }

    private void constExp(ConstExp constExp) {
        addExp(constExp.addExp);
        constExp.value = constExp.addExp.value;
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    private void varDecl(VarDecl varDecl) {
        for (VarDef varDef : varDecl.varDefs) {
            varDef(varDef);
        }
    }

    //义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //| Ident { '[' ConstExp ']' } '=' InitVal
    private void varDef(VarDef varDef) {
        String regid = null;
        String value = null;
        int dim_depth = 0;
        int dim_num1 = 0;
        int dim_num2 = 0;
        if (varDef.constExps.isEmpty()) {
            if (nowTable().level > 0) {
                printf("%v" + regId + " = alloca i32\n");
                regid = "%v" + this.regId;
                value = "0";
                regId++;
                if (varDef.initVal != null) {
                    initVal(varDef.initVal);
                    value = varDef.initVal.value;
                }
                printf("store i32 " + value + ", i32* " + regid + "\n");
            } else {
                value = "0";
                System.out.println(varDef.ident.value);
                if (varDef.initVal != null) {
                    initVal(varDef.initVal);
                    value = varDef.initVal.value;
                }
                printf("@" + varDef.ident.value + " = dso_local global i32 " + value + "\n");
            }
            Symbol symbol = new Symbol(regid, value);
            nowTable().putSymbol(varDef.ident.value, symbol);
        } else {
            //一维数组，有初值和无初值
            if (varDef.constExps.size() == 1) {
                dim_depth = 1;

                int level = nowTable().level;
                nowTable().level = 0;
                constExp(varDef.constExps.get(0));
                nowTable().level = level;

                dim_num1 = Integer.parseInt(varDef.constExps.get(0).value);
                //局部才用alloca
                if (nowTable().level != 0) {
                    printf("%v" + regId + " = alloca [" + varDef.constExps.get(0).value + " x i32]\n");
                    Symbol symbol = new Symbol("%v" + regId, "%v" + regId, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(varDef.ident.value, symbol);
                    regId++;
                }
                //全局存个ident的名字即可
                else {
                    Symbol symbol = new Symbol(null, null, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(varDef.ident.value, symbol);
                }

                Symbol symbol = nowTable().table.get(varDef.ident.value);
                symbol.setD1(Integer.parseInt(varDef.constExps.get(0).value));
                String[] d1Value = symbol.d1Value;

                //有初值
                if (varDef.initVal != null) {
                    varDef.initVal.dim_depth = 1;
                    varDef.initVal.dim_num1 = dim_num1;
                    varDef.initVal.d1Value = symbol.d1Value;
                    initVal(varDef.initVal);
                }
                //无初值
                else {
                    for (int i = 0; i < dim_num1; i++) {
                        if (nowTable().level == 0) {
                            d1Value[i] = "0";
                        } else {
                            d1Value[i] = "Null";
                        }
                    }
                }

                //全局变量
                if (nowTable().level == 0) {
                    if (varDef.initVal != null) {
                        printf("@" + varDef.ident.value + " = dso_local global [" + dim_num1 + " x i32] [");
                        for (int i = 0; i < dim_num1; i++) {
                            if (i != dim_num1 - 1) {
                                printf("i32 " + d1Value[i] + ", ");
                            } else {
                                printf("i32 " + d1Value[i] + "]\n");
                            }
                        }
                    } else {
                        printf("@" + varDef.ident.value + " = dso_local global [" + dim_num1 + " x i32] zeroinitializer\n");
                    }
                }
                //局部变量
                else {
                    if (varDef.initVal != null) {
                        d1Value = varDef.initVal.d1Value;
                        for (int i = 0; i < dim_num1; i++) {
                            if (!d1Value[i].equals("Null")) {
                                printf("%v" + regId + " = getelementptr [" + dim_num1 + " x i32], [" + dim_num1 + " x i32]* " + symbol.regId + ", i32 0, i32 " + i + "\n");
                                printf("store i32 " + d1Value[i] + ", i32* %v" + regId + "\n");
                                regId++;
                            }
                        }
                    }
                }
            }
            //二维数组，有初值和无初值
            else {
                int level = nowTable().level;
                nowTable().level = 0;
                constExp(varDef.constExps.get(0));
                constExp(varDef.constExps.get(1));
                nowTable().level = level;

                dim_depth = 2;
                dim_num1 = Integer.parseInt(varDef.constExps.get(0).value);
                dim_num2 = Integer.parseInt(varDef.constExps.get(1).value);

                //局部才用alloca
                if (nowTable().level != 0) {
                    printf("%v" + regId + " = alloca [" + dim_num1 + " x [" + dim_num2 + " x i32]]\n");
                    Symbol symbol = new Symbol("%v" + regId, "%v" + regId, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(varDef.ident.value, symbol);
                    regId++;
                }
                //全局存个ident的名字即可
                else {
                    Symbol symbol = new Symbol(null, null, dim_depth, dim_num1, dim_num2);
                    nowTable().putSymbol(varDef.ident.value, symbol);
                }

                Symbol symbol = nowTable().table.get(varDef.ident.value);
                symbol.setD1(Integer.parseInt(varDef.constExps.get(0).value));
                symbol.setD2(Integer.parseInt(varDef.constExps.get(1).value));
                String[][] d2Value = symbol.d2Value;

                //有初值
                if (varDef.initVal != null) {
                    varDef.initVal.dim_depth = 2;
                    varDef.initVal.dim_num1 = dim_num1;
                    varDef.initVal.dim_num2 = dim_num2;
                    varDef.initVal.d2Value = symbol.d2Value;
                    initVal(varDef.initVal);
                }
                //无初值
                else {
                    for (int i = 0; i < dim_num1; i++) {
                        for (int j = 0; j < dim_num2; j++) {
                            if (nowTable().level == 0) {
                                d2Value[i][j] = "0";
                            } else {
                                d2Value[i][j] = "Null";
                            }
                        }
                    }
                }
                //全局变量
                if (nowTable().level == 0) {
                    if (varDef.initVal != null) {
                        printf("@" + varDef.ident.value + " = dso_local global [" + dim_num1 + " x [" + dim_num2 + " x i32]] [[");
                        for (int i = 0; i < dim_num1 - 1; i++) {
                            printf(dim_num2 + " x i32] [");
                            for (int j = 0; j < dim_num2 - 1; j++) {
                                printf("i32 " + d2Value[i][j] + ", ");
                            }
                            printf("i32 " + d2Value[i][dim_num2 - 1] + "], [");
                        }
                        printf(dim_num2 + " x i32] [");
                        for (int j = 0; j < dim_num2 - 1; j++) {
                            printf("i32 " + d2Value[dim_num1 - 1][j] + ", ");
                        }
                        printf("i32 " + d2Value[dim_num1 - 1][dim_num2 - 1] + "]]\n");
                    } else {
                        printf("@" + varDef.ident.value + " = dso_local global [" + dim_num1 + " x [" + dim_num2 + " x i32]] zeroinitializer\n");
                    }
                }
                //局部变量
                else {
                    if (varDef.initVal != null) {
                        d2Value = varDef.initVal.d2Value;
                        for (int i = 0; i < dim_num1; i++) {
                            for (int j = 0; j < dim_num2; j++) {
                                if (!d2Value[i][j].equals("Null")) {
                                    printf("%v" + regId + " = getelementptr [" + dim_num1 + " x [" + dim_num2 + " x i32]], [" + dim_num1 + " x [" + dim_num2 + " x i32]]* " + symbol.regId + ", i32 0, i32 " + i + ", i32 " + j + "\n");
                                    printf("store i32 " + d2Value[i][j] + ", i32* %v" + regId + "\n");
                                    regId++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数
//    组初值 3.二维数组初值
    private void initVal(InitVal initVal) {
        if (initVal.exp != null) {
            exp(initVal.exp);
            initVal.value = initVal.exp.value;
        } else {
            //一维数组
            if (initVal.dim_depth == 1) {

                String[] d1v = initVal.d1Value;
                //将输入的值存在div中
                int index = 0;
                for (InitVal div : initVal.initVals) {
                    initVal(div);
                    d1v[index] = div.value;
                    index++;
                }
                if (index < initVal.dim_num1) {
                    for (int i = index; i < initVal.dim_num1; i++) {
                        d1v[i] = (nowTable().level != 0) ? "Null" : "0";
                    }
                }
                initVal.d1Value = d1v;
            }
            //二维数组
            else {
                String[][] d2v = initVal.d2Value;
                //将输入的值存在d2v中
                int rowIndex = 0;
                for (InitVal rowVal : initVal.initVals) {
                    if (rowVal.initVals != null) {
                        System.out.println(rowVal.dim_num2);
                        String[] row = new String[initVal.dim_num2];
                        int colIndex = 0;
                        for (InitVal colVal : rowVal.initVals) {
                            initVal(colVal);
                            row[colIndex++] = colVal.value;
                        }
                        // 填充剩余的二维数组列元素
                        while (colIndex < initVal.dim_num2) {
                            row[colIndex++] = (nowTable().level != 0) ? "Null" : "0";
                        }
                        d2v[rowIndex++] = row;
                    }
                }
                // 填充剩余的二维数组行
                while (rowIndex < initVal.dim_num1) {
                    String[] row = new String[initVal.dim_num2];
                    Arrays.fill(row, (nowTable().level != 0) ? "Null" : "0");
                    d2v[rowIndex++] = row;
                }
                initVal.d2Value = d2v;
                //遍历数组，打印数组内容
                System.out.println("d2v.length:" + d2v.length);
                for (int i = 0; i < d2v.length; i++) {
                    for (int j = 0; j < d2v[i].length; j++) {
                        System.out.print(d2v[i][j] + " ");
                    }
                    System.out.println();
                }
            }

        }
    }

    //    语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
//            | [Exp] ';' //有无Exp两种情况
//            | Block
//            | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
//            | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
//    ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
//            | 'break' ';' | 'continue' ';'
//            | 'return' [Exp] ';' // 1.有Exp 2.无Exp
//            | LVal '=' 'getint''('')'';'
//            | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    private void stmt(Stmt stmt) {
        String type = stmt.type.toString();
        // LVal '=' Exp ';'
        if (type.equals("ASSIGN")) {
            lVal(stmt.lVal);
            exp(stmt.exp);
            System.out.println(stmt.lVal.regId);
            printf("store i32 " + stmt.exp.value + ", i32* " + stmt.lVal.regId + "\n");
        }
        // [Exp] ';'
        else if (type.equals("EXP")) {
            if (stmt.exp != null) {
                exp(stmt.exp);
            }
        }
        //Block
        else if (type.equals("BLOCK")) {

            stmt.block.forstmt1Label = stmt.forstmt1Label;
            stmt.block.condLabel = stmt.condLabel;
            stmt.block.forstmt2Label = stmt.forstmt2Label;
            stmt.block.stmtLabel = stmt.stmtLabel;
            stmt.block.nextLabel = stmt.nextLabel;

            block(stmt.block, false);
        }
        //'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        else if (type.equals("IF")) {
            Cond cond = stmt.cond;

            printf("br label %v" + regId + "\n");
            printf("\nv" + regId + ":\n");

            cond.yesLabel = regId + 1;
            regId += 2;
            //有else
            if (stmt.stmts.get(1) != null) {
                cond.noLabel = regId;
                cond.nextLabel = regId + 1;
                regId += 2;
            }
            //无else
            else {
                cond.nextLabel = regId;
                cond.noLabel = cond.nextLabel;
                regId++;
            }

            cond(cond);
            printf("\nv" + cond.yesLabel + ":\n");
            stmt.stmts.get(0).forstmt1Label = stmt.forstmt1Label;
            stmt.stmts.get(0).condLabel = stmt.condLabel;
            stmt.stmts.get(0).forstmt2Label = stmt.forstmt2Label;
            stmt.stmts.get(0).stmtLabel = stmt.stmtLabel;
            stmt.stmts.get(0).nextLabel = stmt.nextLabel;
            stmt(stmt.stmts.get(0));
            printf("br label %v" + cond.nextLabel + "\n");
            if (stmt.stmts.get(1) != null) {
                printf("\nv" + cond.noLabel + ":\n");
                stmt.stmts.get(1).forstmt1Label = stmt.forstmt1Label;
                stmt.stmts.get(1).condLabel = stmt.condLabel;
                stmt.stmts.get(1).forstmt2Label = stmt.forstmt2Label;
                stmt.stmts.get(1).stmtLabel = stmt.stmtLabel;
                stmt.stmts.get(1).nextLabel = stmt.nextLabel;
                stmt(stmt.stmts.get(1));
                printf("br label %v" + cond.nextLabel + "\n");

            }
            printf("\nv" + cond.nextLabel + ":\n");
        }
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        else if (type.equals("FOR")) {


            stmt.forstmt1Label = regId++;
            stmt.condLabel = regId++;
            stmt.forstmt2Label = regId++;
            stmt.stmtLabel = regId++;
            stmt.nextLabel = regId++;

            printf("br label %v" + stmt.forstmt1Label + "\n");
            printf("\nv" + stmt.forstmt1Label + ":\n");
            if (stmt.forstmt1 != null) {
                forStmt(stmt.forstmt1);
            }
            printf("br label %v" + stmt.condLabel + "\n");
            printf("\nv" + stmt.condLabel + ":\n");

            if (stmt.cond != null) {
                stmt.cond.yesLabel = stmt.stmtLabel;
                stmt.cond.noLabel = stmt.nextLabel;
                stmt.cond.nextLabel = stmt.nextLabel;
                cond(stmt.cond);
                //printf("br i1 %v" + stmt.cond.value + ", label %v" + stmt.stmtLabel + ", label %v" + stmt.nextLabel + "\n");
            } else {
                printf("br label %v" + stmt.stmtLabel + "\n");
            }

            printf("\nv" + stmt.stmtLabel + ":\n");
            System.out.println(stmt.forstmt1Label);
            System.out.println(stmt.forstmt2Label);
            stmt.stmt.forstmt1Label = stmt.forstmt1Label;
            stmt.stmt.condLabel = stmt.condLabel;
            stmt.stmt.forstmt2Label = stmt.forstmt2Label;
            stmt.stmt.stmtLabel = stmt.stmtLabel;
            stmt.stmt.nextLabel = stmt.nextLabel;
            stmt(stmt.stmt);
            printf("br label %v" + stmt.forstmt2Label + "\n");
            printf("\nv" + stmt.forstmt2Label + ":\n");

            if (stmt.forstmt2 != null) {
                forStmt(stmt.forstmt2);
            }
            printf("br label %v" + stmt.condLabel + "\n");

            printf("\nv" + stmt.nextLabel + ":\n");

        }
        // 'break' ';'
        else if (type.equals("BREAK")) {
            System.out.println(stmt.nextLabel);
            printf("br label %v" + stmt.nextLabel + "\n");
        }
        // 'continue' ';'
        else if (type.equals("CONTINUE")) {
            System.out.println(stmt.forstmt2Label);
            printf("br label %v" + stmt.forstmt2Label + "\n");
        }
        // 'return' [Exp] ';' // 1.有Exp 2.无Exp
        else if (type.equals("RETURN")) {
            if (stmt.exp != null) {
                //printf("ret i32 "+exp(stmt.exp)+"\n");
                exp(stmt.exp);

                printf("ret i32 " + stmt.exp.value + "\n");
            }
        }
        //LVal '=' 'getint''('')'';'
        else if (type.equals("GETINT")) {
            lVal(stmt.lVal);
            printf("%v" + regId + " = call i32 @getint()\n");
            printf("store i32 %v" + regId + ", i32* " + stmt.lVal.regId + "\n");
            regId++;
        }
        //'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
        else if (type.equals("PRINTF")) {
            String formatString = stmt.formatString;
            if (stmt.exps != null) {
                for (Exp exp : stmt.exps) {
                    exp(exp);
                }
            }
            //遍历formatString，将%d替换为对应的exp.value
            int index = 1;//存了个“”
            int expIndex = 0;
            while (index < formatString.length() - 1) {
                if (formatString.charAt(index) == '%' && formatString.charAt(index + 1) == 'd') {
                    printf("call void @putint(i32 " + stmt.exps.get(expIndex).value + ")\n");
                    expIndex++;
                    index += 2;
                } else if (formatString.charAt(index) == '\\' && formatString.charAt(index + 1) == 'n') {
                    printf("call void @putch(i32 10)\n");
                    index += 2;
                } else {
                    printf("call void @putch(i32 " + (int) formatString.charAt(index) + ")\n");
                    index++;
                }
            }
        }


    }

    // ForStmt → LVal '=' Exp
    private void forStmt(ForStmt forstmt) {
        lVal(forstmt.lVal);
        exp(forstmt.exp);
        printf("store i32 " + forstmt.exp.value + ", i32* " + forstmt.lVal.regId + "\n");
    }

    private void cond(Cond cond) {
        cond.lOrExp.yesLabel = cond.yesLabel;
        cond.lOrExp.noLabel = cond.noLabel;
        cond.lOrExp.nextLabel = cond.nextLabel;
        lOrExp(cond.lOrExp);
        cond.value = cond.lOrExp.value;
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    private void lOrExp(LOrExp lOrExp) {
        if (lOrExp.lOrExp != null) {

            lOrExp.lOrExp.yesLabel = lOrExp.yesLabel;
            lOrExp.lOrExp.noLabel = lOrExp.noLabel;
            lOrExp.lOrExp.nextLabel = lOrExp.nextLabel;

            //左递归处理lorexp
            isFinalOr++;
            lOrExp(lOrExp.lOrExp);
            lOrExp.value = lOrExp.lOrExp.lAndExp.value;
            isFinalOr--;

            //子lorexp中多级landexp诸如a&&b,则已经在landexp中处理了，这里只需要处理a这种，也就是eqexp,todo 是否可以优化
            if (lOrExp.lOrExp.lAndExp.lAndExp == null) {
                printf("%v" + regId + " = icmp ne i32 " + lOrExp.value + ", 0\n");
                printf("br i1 %v" + regId + ", label %v" + lOrExp.yesLabel + ", label %v" + (regId + 1) + "\n");
                printf("\nv" + (regId + 1) + ":\n");
                regId += 2;
            }
        }
        //处理本层的landexp
        // 如果是a&&b这种，就需要有短路取值,也就是对于a的下一跳会有三种情况：b,a&&b的下一个逻辑式，yesId对应的基本快；
        // 如果单纯一个a，那就不需要
        if (lOrExp.lAndExp.lAndExp != null && isFinalOr != 0) {

            lOrExp.lAndExp.yesLabel = lOrExp.yesLabel;
            lOrExp.lAndExp.noLabel = regId++;

            lAndExp(lOrExp.lAndExp);

        } else {


            lOrExp.lAndExp.yesLabel = lOrExp.yesLabel;
            lOrExp.lAndExp.noLabel = lOrExp.noLabel;
            lAndExp(lOrExp.lAndExp);


            //printf("\nv" + lOrExp.lAndExp.noLabel + ":\n");
        }

        //到总表达式最后一项，需要跳转到yesId或者NoId
        if (isFinalOr == 0 && lOrExp.lAndExp.lAndExp == null) {
            printf("%v" + regId + " = icmp ne i32 " + lOrExp.lAndExp.value + ", 0\n");
            printf("br i1 %v" + regId + ", label %v" + lOrExp.lAndExp.yesLabel + ", label %v" + lOrExp.lAndExp.noLabel + "\n");
            regId++;
        }
//        } else {
//            //如果是a&&b这种，就需要有短路取值
//            if (lOrExp.lAndExp.lAndExp == null) {
//                isFinalOr++;
//                lOrExp.lAndExp.yesLabel = lOrExp.yesLabel;
//                lOrExp.lAndExp.noLabel = lOrExp.noLabel;
//                lAndExp(lOrExp.lAndExp);
//                isFinalOr--;
//            } else {
//                lOrExp.lAndExp.yesLabel = lOrExp.yesLabel;
//                lOrExp.lAndExp.noLabel = regId++;
//                isFinalOr++;
//                lAndExp(lOrExp.lAndExp);
//                isFinalOr--;
//                //printf("\nv" + lOrExp.lAndExp.noLabel + ":\n");
//            }
//
//            if (isFinalOr == 0) {
//                printf("%v" + regId + " = icmp eq i32 " + lOrExp.lAndExp.value + ", 1\n");
//                printf("br i1 %v" + regId + ", label %v" + lOrExp.lAndExp.yesLabel + ", label %v" + lOrExp.lAndExp.noLabel + "\n");
//                regId++;
//            }
//        }
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    //因为是左递归存储，实际思路其实就是递归拿eqexp的value
    private void lAndExp(LAndExp lAndExp) {
        if (lAndExp.lAndExp != null) {
            lAndExp.lAndExp.yesLabel = lAndExp.yesLabel;
            lAndExp.lAndExp.noLabel = lAndExp.noLabel;

            //左递归处理landexp
            isFinalAnd++;
            lAndExp(lAndExp.lAndExp);
            lAndExp.value = lAndExp.lAndExp.eqExp.value;
            isFinalAnd--;

            printf("%v" + regId + " = icmp eq i32 " + lAndExp.value + ", 0\n");
            printf("br i1 %v" + regId + ", label %v" + lAndExp.noLabel + ", label %v" + (regId + 1) + "\n");
            printf("\nv" + (regId + 1) + ":\n");
            regId += 2;


            eqExp(lAndExp.eqExp);
            if (isFinalAnd == 0) {
                printf("%v" + regId + " = icmp eq i32 " + lAndExp.eqExp.value + ", 0\n");
                printf("br i1 %v" + regId + ", label %v" + lAndExp.noLabel + ", label %v" + lAndExp.yesLabel + "\n");
                if (isFinalOr != 0)
                    printf("\nv" + lAndExp.noLabel + ":\n");
                regId += 2;
            }


        } else {
            eqExp(lAndExp.eqExp);
            lAndExp.value = lAndExp.eqExp.value;
        }
    }

    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private void eqExp(EqExp eqExp) {
        //模仿addExp
        if (eqExp.eqExp != null) {
            eqExp(eqExp.eqExp);
            relExp(eqExp.relExp);
            String left = eqExp.eqExp.value;
            String right = eqExp.relExp.value;
            if (nowTable().level > 0) {
                if (eqExp.operator.value.equals("=="))
                    printf("%v" + regId + " = " + "icmp eq" + " i32 " + left + ", " + right + "\n");
                else
                    printf("%v" + regId + " = " + "icmp ne" + " i32 " + left + ", " + right + "\n");
                printf("%v" + (regId + 1) + " = zext i1 %v" + regId + " to i32\n");

                eqExp.regId = "%v" + (regId + 1);
                eqExp.value = "%v" + (regId + 1);
                regId += 2;


            } else {
                eqExp.value = Calculate(left, right, eqExp.operator.value) + "";
            }
        } else {
            relExp(eqExp.relExp);
            eqExp.value = eqExp.relExp.value;
        }
    }

    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private void relExp(RelExp relExp) {
        if (relExp.relExp != null) {
            relExp(relExp.relExp);
            addExp(relExp.addExp);
            String left = relExp.relExp.value;
            String right = relExp.addExp.value;
            if (nowTable().level > 0) {
                if (relExp.operator.value.equals("<"))
                    printf("%v" + regId + " = " + "icmp slt" + " i32 " + left + ", " + right + "\n");
                else if (relExp.operator.value.equals(">"))
                    printf("%v" + regId + " = " + "icmp sgt" + " i32 " + left + ", " + right + "\n");
                else if (relExp.operator.value.equals("<="))
                    printf("%v" + regId + " = " + "icmp sle" + " i32 " + left + ", " + right + "\n");
                else
                    printf("%v" + regId + " = " + "icmp sge" + " i32 " + left + ", " + right + "\n");
                printf("%v" + (regId + 1) + " = zext i1 %v" + regId + " to i32\n");

                relExp.regId = "%v" + (regId + 1);
                relExp.value = "%v" + (regId + 1);
                regId += 2;


            } else {
                relExp.value = Calculate(left, right, relExp.operator.value) + "";
            }
        } else {
            addExp(relExp.addExp);
            relExp.value = relExp.addExp.value;
        }
    }

    // Exp → AddExp
    private void exp(Exp exp) {
        addExp(exp.addExp);
        exp.regId = exp.addExp.regId;
        exp.value = exp.addExp.value;
        exp.AddrType = exp.addExp.AddrType;
    }

    private void addExp(AddExp addExp) {
        if (addExp.operator == null) {
            mulExp(addExp.mulExp);
            addExp.value = addExp.mulExp.value;
            addExp.regId = addExp.mulExp.regId;
            addExp.AddrType = addExp.mulExp.AddrType;
        } else {
            addExp(addExp.addExp);
            mulExp(addExp.mulExp);
            String left = addExp.addExp.value;
            String right = addExp.mulExp.value;
            if (nowTable().level > 0) {
                if (addExp.operator.value.equals("+"))
                    printf("%v" + regId + " = " + "add" + " i32 " + left + ", " + right + "\n");
                else
                    printf("%v" + regId + " = " + "sub" + " i32 " + left + ", " + right + "\n");
                addExp.regId = "%v" + regId;
                addExp.value = "%v" + regId;
                addExp.AddrType = addExp.addExp.AddrType;
                regId++;
            } else {
                addExp.value = Calculate(left, right, addExp.operator.value) + "";
                addExp.AddrType = addExp.addExp.AddrType;
            }
        }
    }

    private void mulExp(MulExp mulExp) {
        if (mulExp.operator == null) {
            unaryExp(mulExp.unaryExp);
            mulExp.value = mulExp.unaryExp.value;
            mulExp.regId = mulExp.unaryExp.regId;
            mulExp.AddrType = mulExp.unaryExp.AddrType;
        } else {
            mulExp(mulExp.mulExp);
            unaryExp(mulExp.unaryExp);
            String left = mulExp.mulExp.value;
            String right = mulExp.unaryExp.value;
            if (nowTable().level > 0) {
                if (mulExp.operator.value.equals("*"))
                    printf("%v" + regId + " = " + "mul" + " i32 " + left + ", " + right + "\n");
                else if (mulExp.operator.value.equals("/"))
                    printf("%v" + regId + " = " + "sdiv" + " i32 " + left + ", " + right + "\n");
                else
                    printf("%v" + regId + " = " + "srem" + " i32 " + left + ", " + right + "\n");
                mulExp.regId = "%v" + regId;
                mulExp.value = "%v" + regId;
                mulExp.AddrType = mulExp.mulExp.AddrType;
                regId++;
            } else {
                mulExp.value = Calculate(left, right, mulExp.operator.value) + "";
                mulExp.AddrType = mulExp.mulExp.AddrType;
            }
        }
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函
    //数调用也需要覆盖FuncRParams的不同情况| UnaryOp UnaryExp // 存在即可
    private void unaryExp(UnaryExp unaryExp) {
        if (unaryExp.primaryExp != null) {
            primaryExp(unaryExp.primaryExp);
            unaryExp.value = unaryExp.primaryExp.value;
            unaryExp.regId = unaryExp.primaryExp.regId;
            unaryExp.AddrType = unaryExp.primaryExp.AddrType;
        } else if (unaryExp.ident != null) {
            String ident = unaryExp.ident.value;
            String type = getFuncTable().table.get(ident).returnType;
            System.out.println(type);
            if (type.equals("int")) {
                if (unaryExp.funcRParams != null) {
                    funcRParams(unaryExp.funcRParams);
                    printf("%v" + regId + " = call i32" + " @" + ident + "(" + unaryExp.funcRParams.value + ")\n");
                    unaryExp.regId = "%v" + regId;
                    unaryExp.value = "%v" + regId;
                    this.regId++;
                } else {
                    printf("%v" + regId + " = call i32" + " @" + ident + "()\n");
                    unaryExp.regId = "%v" + regId;
                    unaryExp.value = "%v" + regId;
                    this.regId++;
                }
                //能参与运算的一定是i32
                unaryExp.AddrType = "i32";
            } else {

                if (unaryExp.funcRParams != null) {
                    funcRParams(unaryExp.funcRParams);
                    printf("call " + type + " @" + ident + "(" + unaryExp.funcRParams.value + ")\n");
                } else {
                    printf("call " + type + " @" + ident + "()\n");
                }

            }
        }
        //直接对符号进行处理
        else if (unaryExp.unaryOp != null) {
            unaryExp(unaryExp.unaryExp);
            //处理减号
            if (unaryExp.unaryOp.token.value.equals("-")) {
                String value = unaryExp.unaryExp.value;
                if (nowTable().level > 0) {
                    printf("%v" + regId + " = " + "sub" + " i32 " + 0 + ", " + value + "\n");
                    unaryExp.regId = "%v" + regId;
                    unaryExp.value = "%v" + regId;
                    unaryExp.AddrType = unaryExp.unaryExp.AddrType;
                    regId++;
                } else {
                    unaryExp.value = Calculate("0", value, "-") + "";
                    unaryExp.AddrType = unaryExp.unaryExp.AddrType;
                }
            }
            //处理加号
            else if (unaryExp.unaryOp.token.value.equals("+")) {
                unaryExp.value = unaryExp.unaryExp.value;
                unaryExp.regId = unaryExp.unaryExp.regId;
                unaryExp.AddrType = unaryExp.unaryExp.AddrType;
            }
            //处理！
            else {
                printf("%v" + regId + " = icmp eq i32 " + unaryExp.unaryExp.value + ", 0\n");
                regId++;
                printf("%v" + regId + " = zext i1 %v" + (regId - 1) + " to i32\n");
                unaryExp.regId = "%v" + regId;
                unaryExp.value = "%v" + regId;
                unaryExp.AddrType = unaryExp.unaryExp.AddrType;
                regId++;
            }

        }

    }

    // FuncRParams → Exp { ',' Exp }
    private void funcRParams(FuncRParams funcRParams) {
        for (Exp exp : funcRParams.exps) {
            exp(exp);
            funcRParams.value += exp.AddrType + " " + exp.value;
            if (funcRParams.exps.indexOf(exp) != funcRParams.exps.size() - 1)
                funcRParams.value += ", ";
        }
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    private void primaryExp(PrimaryExp primaryExp) {
        if (primaryExp.number != null) {
            number(primaryExp.number);
            primaryExp.value = primaryExp.number.value;
            primaryExp.AddrType = "i32";
        } else if (primaryExp.exp != null) {
            exp(primaryExp.exp);
            primaryExp.value = primaryExp.exp.value;
            primaryExp.AddrType = primaryExp.exp.AddrType;

        } else if (primaryExp.lVal != null) {
            lVal(primaryExp.lVal);
            primaryExp.value = primaryExp.lVal.value;
            primaryExp.regId = primaryExp.lVal.regId;
            primaryExp.AddrType = primaryExp.lVal.AddrType;
        }


    }

    //LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    private void lVal(LVal lVal) {
        String ident = lVal.ident.value;
        System.out.println(ident);
        Pair<Symbol, Integer> pair = getSymbolTable(ident);
        System.out.println(pair);
        if (pair == null) {
            System.out.println(ident);
        }
        int symLevel = pair.getValue();
        Symbol symbol = pair.getKey();
        //此时在函数内
        if (nowTable().level > 0) {
            //局部变量
            if (symLevel > 0) {
                //int a
                if (symbol.dim_depth == 0) {
                    System.out.println(symbol.regId);
                    printf("%v" + regId + " = load i32, i32* " + symbol.regId + "\n");
                    lVal.regId = symbol.regId;//todo ？
                    lVal.value = "%v" + this.regId;
                    lVal.AddrType = "i32";
                    regId++;
                }
                //使用一维数组
                else if (symbol.dim_depth == 1) {
                    //func(int a[10]) {int a[10],func(a)}
                    if (symbol.dim_num1 != 0 && lVal.exps.isEmpty()) {
                        printf("%v" + regId + " = getelementptr [" + symbol.dim_num1 + " x i32], [" + symbol.dim_num1 + " x i32]* " + symbol.regId + ", i32 0, i32 0\n");
                        lVal.regId = "%v" + regId;
                        lVal.value = "%v" + regId;
                        lVal.AddrType = "i32*";
                        regId++;
                    }
                    //func(int a[]) {int a[10],func(a)}
                    else if (symbol.dim_num1 == 0 && lVal.exps.isEmpty()) {
                        printf("%v" + regId + " = load " + symbol.addrType + ", " + symbol.addrType + " * " + symbol.regId + "\n");
                        lVal.regId = "%v" + regId;
                        lVal.value = "%v" + regId;
                        lVal.AddrType = "i32*";
                        regId++;
                    }
                    //func(int a) {int a[10],func(a[1])}
                    else if (symbol.dim_num1 != 0 && lVal.exps.size() == 1) {
                        exp(lVal.exps.get(0));
                        printf("%v" + regId + " = getelementptr [" + symbol.dim_num1 + " x i32], [" + symbol.dim_num1 + " x i32]* " + symbol.regId + ", i32 0, i32 " + lVal.exps.get(0).value + "\n");
                        printf("%v" + (regId + 1) + " = load i32, i32* %v" + regId + "\n");
                        lVal.regId = "%v" + regId;
                        lVal.value = "%v" + (regId + 1);
                        lVal.AddrType = "i32";
                        regId += 2;
                    }
                    //func(int a) {int a[],func(a[1])}
                    else if (symbol.dim_num1 == 0 && lVal.exps.size() == 1) {
                        exp(lVal.exps.get(0));
                        printf("%v" + regId + " = load i32*, i32* *" + symbol.regId + "\n");
                        printf("%v" + (regId + 1) + " = getelementptr i32, i32* %v" + regId + ", i32 " + lVal.exps.get(0).value + "\n");
                        printf("%v" + (regId + 2) + " = load i32, i32* %v" + (regId + 1) + "\n");
                        lVal.regId = "%v" + (regId + 1);
                        lVal.value = "%v" + (regId + 2);
                        lVal.AddrType = "i32";
                        regId += 3;
                    }
                }
                //处理二维数组
                else {
                    //func(int a[2][3])     {int a[2][3],func(a)}
                    if (symbol.dim_num1 != 0 && lVal.exps.isEmpty()) {
                        printf("%v" + regId + " = getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* " + symbol.regId + ", i32 0, i32 0\n");
                        lVal.regId = "%v" + regId;
                        lVal.value = "%v" + regId;
                        lVal.AddrType = "[" + symbol.dim_num2 + " x i32]*";
                        regId++;
                    }
                    //func(int a[][3])      {int a[2][3],func(a)}
                    else if (symbol.dim_num1 == 0 && lVal.exps.isEmpty()) {
                        printf("%v" + regId + " = load " + symbol.addrType + ", " + symbol.addrType + " * " + symbol.regId + "\n");
                        lVal.regId = symbol.regId;
                        lVal.value = "%v" + regId;
                        lVal.AddrType = "[" + symbol.dim_num2 + " x i32]*";
                        regId++;
                    }
                    //{int a[2][3],func(a[1])}
                    else if (symbol.dim_num1 != 0 && lVal.exps.size() == 1) {
                        exp(lVal.exps.get(0));
                        printf("%v" + regId + " =mul i32 " + lVal.exps.get(0).value + ", " + symbol.dim_num2 + "\n");
                        printf("%v" + (regId + 1) + " = getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* " + symbol.regId + ", i32 0, i32 0\n");
                        printf("%v" + (regId + 2) + " = getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + (regId + 1) + ", i32 0, i32 %v" + regId + "\n");
                        lVal.regId = "%v" + (regId + 2);
                        lVal.value = "%v" + (regId + 2);
                        lVal.AddrType = "i32*";
                        regId += 3;
                    }
                    // {int a[][3],func(a[1])}
                    else if (symbol.dim_num1 == 0 && lVal.exps.size() == 1) {
                        exp(lVal.exps.get(0));
                        printf("%v" + regId + " = load [" + symbol.dim_num2 + " x i32] *, [" + symbol.dim_num2 + " x i32] ** " + symbol.regId + "\n");
                        printf("%v" + (regId + 1) + " = getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + regId + ", i32 " + lVal.exps.get(0).value + "\n");
                        printf("%v" + (regId + 2) + " = getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + (regId + 1) + ", i32 0, i32 0\n");
                        lVal.regId = "%v" + (regId + 1);
                        lVal.value = "%v" + (regId + 2);
                        lVal.AddrType = "i32*";
                        regId += 3;
                    }
                    //func(int a[2][3]),{func(a[1][2])}
                    else if (symbol.dim_num1 != 0 && lVal.exps.size() == 2) {
                        exp(lVal.exps.get(0));
                        exp(lVal.exps.get(1));
                        printf("%v" + regId + " = getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* " + symbol.regId + ", i32 0, i32 " + lVal.exps.get(0).value + ", i32 " + lVal.exps.get(1).value + "\n");
                        printf("%v" + (regId + 1) + " = load i32, i32* %v" + regId + "\n");
                        lVal.regId = "%v" + regId;
                        lVal.value = "%v" + (regId + 1);
                        lVal.AddrType = "i32";
                        regId += 2;
                    }
                    //func(int a[][3]),{func(a[1][2])}
                    else if (symbol.dim_num1 == 0 && lVal.exps.size() == 2) {
                        exp(lVal.exps.get(0));
                        exp(lVal.exps.get(1));
                        printf("%v" + regId + " = load [" + symbol.dim_num2 + " x i32]*, [" + symbol.dim_num2 + " x i32]** " + symbol.regId + "\n");
                        printf("%v" + (regId + 1) + " = getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + regId + ", i32 " + lVal.exps.get(0).value + "\n");
                        printf("%v" + (regId + 2) + " = getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + (regId + 1) + ", i32 0, i32 " + lVal.exps.get(1).value + "\n");
                        printf("%v" + (regId + 3) + " = load i32, i32* %v" + (regId + 2) + "\n");
                        lVal.regId = "%v" + (regId + 2);
                        lVal.value = "%v" + (regId + 3);
                        lVal.AddrType = "i32";
                        regId += 4;
                    }
                }
            }
            //全局变量
            else {
                if (symbol.dim_depth == 0) {
                    printf("%v" + regId + " = load i32, i32* @" + ident + "\n");
                    lVal.regId = "@" + ident;
                    lVal.value = "%v" + this.regId;
                    lVal.AddrType = "i32";
                    regId++;
                }
                //
                else {
                    //一维数组
                    if (symbol.dim_depth == 1) {
                        if (lVal.exps.size() == 0) {
                            printf("%v" + regId + "= getelementptr [" + symbol.dim_num1 + " x i32], [" + symbol.dim_num1 + " x i32]* @" + ident + ", i32 0, i32 0\n");
                            lVal.regId = "%v" + regId;
                            lVal.value = "%v" + regId;
                            lVal.AddrType = "i32*";
                            regId++;
                        } else if (lVal.exps.size() == 1) {
                            exp(lVal.exps.get(0));
                            printf("%v" + regId + "= getelementptr [" + symbol.dim_num1 + " x i32], [" + symbol.dim_num1 + " x i32]* @" + ident + ", i32 0, i32 " + lVal.exps.get(0).value + "\n");
                            printf("%v" + (regId + 1) + " = load i32, i32* %v" + regId + "\n");
                            lVal.AddrType = "i32";
                            lVal.regId = "%v" + regId;
                            lVal.value = "%v" + (regId + 1);
                            regId += 2;
                        }
                    }
                    //二维数组
                    else if (symbol.dim_depth == 2) {
                        if (lVal.exps.size() == 0) {
                            printf("%v" + regId + "= getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* @" + ident + ", i32 0, i32 0\n");
                            lVal.regId = "%v" + regId;
                            lVal.value = "%v" + regId;
                            lVal.AddrType = "[" + symbol.dim_num2 + " x i32]*";
                            regId++;
                        } else if (lVal.exps.size() == 1) {
                            exp(lVal.exps.get(0));
                            printf("%v" + regId + "=mul i32 " + lVal.exps.get(0).value + ", " + symbol.dim_num2 + "\n");
                            printf("%v" + (regId + 1) + "= getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* @" + ident + ", i32 0, i32 0\n");
                            printf("%v" + (regId + 2) + "= getelementptr [" + symbol.dim_num2 + " x i32], [" + symbol.dim_num2 + " x i32]* %v" + (regId + 1) + ", i32 0, i32 %v" + regId + "\n");
                            lVal.AddrType = "i32*";
                            lVal.regId = "%v" + (regId + 2);
                            lVal.value = "%v" + (regId + 2);
                            regId += 3;
                        } else {
                            exp(lVal.exps.get(0));
                            exp(lVal.exps.get(1));
                            printf("%v" + regId + " = getelementptr [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]], [" + symbol.dim_num1 + " x [" + symbol.dim_num2 + " x i32]]* @" + ident + ", i32 0, i32 " + lVal.exps.get(0).value + ", i32 " + lVal.exps.get(1).value + "\n");
                            printf("%v" + (regId + 1) + " = load i32, i32* %v" + regId + "\n");
                            lVal.AddrType = "i32";
                            lVal.regId = "%v" + regId;
                            lVal.value = "%v" + (regId + 1);
                            regId += 2;

                        }
                    }
                }
            }
        }
        //函数外，直接拿值,而且全局变量只能使用全局变量去运算
        else {
            if (symbol.dim_depth == 0) {
                lVal.value = symbol.value;
            } else if (symbol.dim_depth == 1) {
                exp(lVal.exps.get(0));
                lVal.value = symbol.d1Value[Integer.parseInt(lVal.exps.get(0).value)];
            } else {
                exp(lVal.exps.get(0));
                exp(lVal.exps.get(1));
                lVal.value = symbol.d2Value[Integer.parseInt(lVal.exps.get(0).value)][Integer.parseInt(lVal.exps.get(1).value)];
            }

        }
    }

    private void number(Number_ number) {
        number.value = number.token.value;
    }

    public void printf(String str) {
        filewritter.printIR(str);
    }

    private int Calculate(String left, String right, String operator) {
        int l = Integer.parseInt(left);
        int r = Integer.parseInt(right);
        if (operator.equals("+"))
            return l + r;
        else if (operator.equals("-"))
            return l - r;
        else if (operator.equals("*"))
            return l * r;
        else if (operator.equals("/"))
            return l / r;
        else if (operator.equals("%"))
            return l % r;
        else if (operator.equals("<"))
            return l < r ? 1 : 0;
        else if (operator.equals("<="))
            return l <= r ? 1 : 0;
        else if (operator.equals(">"))
            return l > r ? 1 : 0;
        else if (operator.equals(">="))
            return l >= r ? 1 : 0;
        else if (operator.equals("=="))
            return l == r ? 1 : 0;
        else if (operator.equals("!="))
            return l != r ? 1 : 0;
        else throw new RuntimeException("error");
    }
}

