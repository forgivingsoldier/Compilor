package node;


import tool.filewritter;

import java.util.ArrayList;
import java.util.List;


public class Stmt {


    public enum Type {
        ASSIGN,
        EXP,
        BLOCK,
        IF,
        FOR,
        BREAK,
        CONTINUE,
        RETURN,
        GETINT,
        PRINTF
    }

    public Type type;
    public LVal lVal;
    public Exp exp;
    public List<Exp> exps;
    public Block block;
    public Cond cond;
    public List<Stmt> stmts = new ArrayList<>();
    public Stmt stmt;
    public ForStmt forstmt1;
    public ForStmt forstmt2;
    public String formatString;
    //错误处理使用
    public int breakLine;
    public int continueLine;
    public int printfLine;
    public int returnLine;
    //llvm使用
    public int forstmt1Label;
    public int condLabel;
    public int forstmt2Label;
    public int stmtLabel;
    public int nextLabel;

    //语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    public Stmt(LVal lVal, Exp exp) {
        this.type = Type.ASSIGN;
        this.lVal = lVal;
        this.exp = exp;
    }

    //| [Exp] ';' //有无Exp两种情况
    public Stmt(Exp exp) {
        this.type = Type.EXP;
        this.exp = exp;
    }

    //| Block
    public Stmt(Block block) {
        this.type = Type.BLOCK;
        this.block = block;
    }

    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    public Stmt(Cond cond, Stmt stmt1, Stmt stmt2) {
        this.type = Type.IF;
        this.cond = cond;
        this.stmts.add(stmt1);
        this.stmts.add(stmt2);
    }

    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    public Stmt(ForStmt forstmt1, Cond cond, ForStmt forstmt2, Stmt stmt) {
        this.type = Type.FOR;
        this.forstmt1 = forstmt1;
        this.cond = cond;
        this.forstmt2 = forstmt2;
        this.stmt = stmt;
    }

    //| 'break' ';'
    public Stmt(int breakLine) {
        this.type = Type.BREAK;
        this.breakLine = breakLine;
    }

    // | 'continue' ';'
    public Stmt(boolean isContinue, int continueLine) {
        this.type = Type.CONTINUE;
        this.continueLine = continueLine;
    }

    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    public Stmt(Exp exp, boolean isReturn, int returnLine) {
        this.type = Type.RETURN;
        this.exp = exp;
        this.returnLine = returnLine;
    }

    //| LVal '=' 'getint''('')'';'
    public Stmt(LVal lVal) {
        this.type = Type.GETINT;
        this.lVal = lVal;
    }

    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public Stmt(String formatString, List<Exp> exps, int printfLine) {
        this.type = Type.PRINTF;
        this.formatString = formatString;
        this.exps = exps;
        this.printfLine = printfLine;
    }

    // Stmt -> LVal '=' Exp ';'
    //	| [Exp] ';'
    //	| Block
    //	| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //	|  'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //	| 'break' ';' | 'continue' ';'
    //	| 'return' [Exp] ';'
    //	| LVal '=' 'getint' '(' ')' ';'
    //	| 'printf' '(' FormatString { ',' Exp } ')' ';'
    public void printToFile() {
        if (type == Type.ASSIGN) {
            lVal.printToFile();
            filewritter.printToken("ASSIGN", "=");
            exp.printToFile();
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.EXP) {
            if (exp != null)
                exp.printToFile();
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.BLOCK) {
            block.printToFile();
        } else if (type == Type.IF) {
            filewritter.printToken("IFTK", "if");
            filewritter.printToken("LPARENT", "(");
            cond.printToFile();
            filewritter.printToken("RPARENT", ")");
            stmts.get(0).printToFile();
            if (stmts.get(1) != null) {
                filewritter.printToken("ELSETK", "else");
                stmts.get(1).printToFile();
            }
        } else if (type == Type.FOR) {
            filewritter.printToken("FORTK", "for");
            filewritter.printToken("LPARENT", "(");
            if (forstmt1 != null)
                forstmt1.printToFile();
            filewritter.printToken("SEMICN", ";");
            if (cond != null)
                cond.printToFile();
            filewritter.printToken("SEMICN", ";");
            if (forstmt2 != null)
                forstmt2.printToFile();
            filewritter.printToken("RPARENT", ")");
            stmt.printToFile();
        } else if (type == Type.BREAK) {
            filewritter.printToken("BREAKTK", "break");
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.CONTINUE) {
            filewritter.printToken("CONTINUETK", "continue");
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.RETURN) {
            filewritter.printToken("RETURNTK", "return");
            if (exp != null)
                exp.printToFile();
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.GETINT) {
            lVal.printToFile();
            filewritter.printToken("ASSIGN", "=");
            filewritter.printToken("GETINTTK", "getint");
            filewritter.printToken("LPARENT", "(");
            filewritter.printToken("RPARENT", ")");
            filewritter.printToken("SEMICN", ";");
        } else if (type == Type.PRINTF) {
            filewritter.printToken("PRINTFTK", "printf");
            filewritter.printToken("LPARENT", "(");
            filewritter.printToken("STRCON", formatString);
            if (exps != null) {
                for (Exp exp : exps) {
                    filewritter.printToken("COMMA", ",");
                    exp.printToFile();
                }
            }
            filewritter.printToken("RPARENT", ")");
            filewritter.printToken("SEMICN", ";");
        }
        System.out.println("print stmt already");
        filewritter.printGrammer("Stmt");
    }
}
