package parser;

import lexer.token;
import lexer.type;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class parser {
    public ArrayList<token> tokens;
    public ArrayList<token> grammer;
    public int position;
    public int line;
    public token prevToken;
    public String nowType;
    public int length;

    public parser(ArrayList<token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.line = 1;
        this.grammer = new ArrayList<token>();
        this.nowType = this.tokens.get(0).type.toString();
        this.prevToken = null;
        this.length = tokens.size();
    }

    public token nextTokenWithForward() {
        if (position < length) {
            return tokens.get(position++);
        }
        else {
            return null;
        }
    }

    public String nextTypeWithForward() {
        if (position < length) {
            token token = tokens.get(position++);
            nowType = String.valueOf(token.type);
            grammer.add(prevToken);
            prevToken = token;
            line = token.line;
            return nowType;
        }
        else {
            return null;
        }
    }

    public String getPreType() {
        if (position < length) {
            return String.valueOf(tokens.get(position - 1).type);
        }
        else {
            return null;
        }
    }

    public String getNextType() {
        if (position < length) {
            return String.valueOf(tokens.get(position).type);
        }
        else {
            return null;
        }
    }

    public String getNext2Type() {
        if (position < length) {
            return String.valueOf(tokens.get(position + 1).type);
        }
        else {
            return null;
        }
    }

    public void analyze() {
        CompUnit();
    }

    public void CompUnit() {
        nowType = nextTypeWithForward();
        while (nowType.equals("CONSTTK") || nowType.equals("INTTK") && getNextType().equals("IDENFR") && !getNext2Type().equals("LPARENT")) {
            if (nowType.equals("CONSTTK")) {
                ConstDecl();
            } else {
                VarDecl();
            }
            //System.out.println("ConstDecl or VarDecl");
        }
        while (nowType.equals("VOIDTK") || nowType.equals("INTTK") && getNextType().equals("IDENFR") && getNext2Type().equals("LPARENT")) {
            FuncDef();
        }
        if (nowType.equals("INTTK") && getNextType().equals("MAINTK")) {
            MainFuncDef();
        } else {
            System.out.println("error" + nowType + " " + position + getNextType() + getNext2Type());
            System.out.println(tokens.get(327).type + "," + tokens.get(328).type + "," + tokens.get(329).type);
        }
        token token = new token(line, position, type.GRAMMER, "<CompUnit>");
        grammer.add(token);


    }

    public void FuncDef() {
        if (nowType.equals("INTTK") || nowType.equals("VOIDTK")) {
            FuncType();
            if (nowType.equals("IDENFR")) {
                nextTypeWithForward();
                if (nowType.equals("LPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("INTTK")) {
                        FuncFParams();
                    }
                    if (nowType.equals("RPARENT")) {
                        nextTypeWithForward();
                        Block();
                    }
                    else {
                    }
                }
                else {
                }
            }
            else {
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncDef>");
        grammer.add(token);
    }

    public boolean checkBlock() {
        boolean ans = Arrays.asList("CONSTTK", "INTTK", "IDENFR", "SEMICN", "IFTK", "FORTK", "BREAKTK",
                "CONTINUETK", "RETURNTK", "PRINTFTK", "LBRACE", "LPARENT",
                "PLUS", "MINU", "NOT", "INTCON").contains(nowType);

        return ans;
    }

    public boolean checkStmt() {

        return Arrays.asList("IDENFR", "SEMICN", "IFTK", "FORTK", "BREAKTK",
                "CONTINUETK", "RETURNTK", "PRINTFTK", "LBRACE", "LPARENT",
                "PLUS", "MINU", "NOT", "INTCON").contains(nowType);
    }

    public void Block() {
        if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            while (checkBlock()) {
                BlockItem();
            }
            if (nowType.equals("RBRACE")) {
                nextTypeWithForward();
            }
            else {
            }
        }
        else {
        }
        if(position==length)
            grammer.add(prevToken);
        token token = new token(line, position, type.GRAMMER, "<Block>");
        grammer.add(token);
    }

    public void BlockItem() {
        if (nowType.equals("CONSTTK")) {
            ConstDecl();
        } else if (nowType.equals("INTTK")) {
            VarDecl();
        } else if (checkStmt()) {
            Stmt();
        }
        else {
        }
    }

    public void Stmt() {
        if (nowType.equals("LBRACE")) {
            Block();
        } else if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("INTCON")) {
            Exp();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            }
            else {
            }
        } else if (nowType.equals("SEMICN")) {
            nextTypeWithForward();
        } else if (nowType.equals("IFTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                Cond();
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                    Stmt();
                    if (nowType.equals("ELSETK")) {
                        nextTypeWithForward();
                        Stmt();
                    }
                }
                else {
                }
            }
            else {
            }
        } else if (nowType.equals("FORTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                if (nowType.equals("IDENFR")) {
                    ForStmt();
                }
                if (nowType.equals("SEMICN")) {
                    nextTypeWithForward();
                    if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                        Cond();
                    }
                    if (nowType.equals("SEMICN")) {
                        nextTypeWithForward();
                        if (nowType.equals("IDENFR")) {
                            ForStmt();
                        }
                        if (nowType.equals("RPARENT")) {
                            nextTypeWithForward();
                            Stmt();
                        }
                    }
                    else {
                    }
                }
            }
        } else if (nowType.equals("BREAKTK")) {
            nextTypeWithForward();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            }
            else {
            }
        } else if (nowType.equals("CONTINUETK")) {
            nextTypeWithForward();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            }
            else {
            }
        } else if (nowType.equals("RETURNTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                Exp();
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            }
            else {
            }
        } else if (nowType.equals("PRINTFTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                nextTypeWithForward();
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    Exp();
                }
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("SEMICN")) {
                        nextTypeWithForward();
                    }
                    else {
                    }
                }
                else {
                }
            }
            else {
            }
        } else if (nowType.equals("IDENFR")) {
            if(checkNotExp()){
                LVal();
                if (nowType.equals("ASSIGN")) {
                    nextTypeWithForward();
                    if (nowType.equals("GETINTTK")) {
                        nextTypeWithForward();
                        if (nowType.equals("LPARENT")) {
                            nextTypeWithForward();
                            if (nowType.equals("RPARENT")) {
                                nextTypeWithForward();
                                if (nowType.equals("SEMICN")) {
                                    nextTypeWithForward();
                                }
                                else {
                                }
                            }
                            else {
                            }
                        }
                        else {
                        }
                    } else if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                        Exp();
                        if (nowType.equals("SEMICN")) {
                            nextTypeWithForward();
                        }
                        else {
                        }
                    }
                    else {
                    }
                }
            }else{Exp();
                if(nowType.equals("SEMICN")){ nextTypeWithForward();}
                else{}
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<Stmt>");
        grammer.add(token);
    }

    private boolean checkNotExp() {
        int i=position;
        while(i<length&&!String.valueOf(tokens.get(i).type).equals("SEMICN")){
            if(String.valueOf(tokens.get(i).type).equals("ASSIGN")){return true;}
            i++;
        }
        return false;
    }

    public void ForStmt() {
        if (nowType.equals("IDENFR")) {
            LVal();
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                    Exp();
                }
                else {
                }
            }
            else {
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<ForStmt>");
        grammer.add(token);
    }

    public void LVal() {
        if (nowType.equals("IDENFR")) {
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                Exp();
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                }
                else {
                }
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<LVal>");
        grammer.add(token);
    }

    public void Cond() {
        LOrExp();
        token token = new token(line, position, type.GRAMMER, "<Cond>");
        grammer.add(token);
    }

    public void FuncType() {
        if (nowType.equals("VOIDTK")) {
            nextTypeWithForward();
        } else if (nowType.equals("INTTK")) {
            nextTypeWithForward();
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncType>");
        grammer.add(token);
    }

    public void FuncFParams() {
        if (nowType.equals("INTTK")) {
            FuncFParam();
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                FuncFParam();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncFParams>");
        grammer.add(token);
    }

    public void FuncFParam() {
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
            if (nowType.equals("IDENFR")) {
                nextTypeWithForward();
                if (nowType.equals("LBRACK")) {
                    nextTypeWithForward();
                    if (nowType.equals("RBRACK")) {
                        nextTypeWithForward();
                        while (nowType.equals("LBRACK")) {
                            nextTypeWithForward();
                            ConstExp();
                            if (nowType.equals("RBRACK")) {
                                nextTypeWithForward();
                            }
                            else {
                            }
                        }
                    }
                    else {
                    }
                }
            }
            else {
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncFParam>");
        grammer.add(token);
    }

    public void MainFuncDef() {
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
            if (nowType.equals("MAINTK")) {
                nextTypeWithForward();
                if (nowType.equals("LPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("RPARENT")) {
                        nextTypeWithForward();
                        Block();
                    }
                }
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<MainFuncDef>");
        grammer.add(token);
    }

    public void VarDecl() {
        if (nowType.equals("INTTK")) {
            Btype();
            VarDef();
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                VarDef();
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                token token = new token(line, position, type.GRAMMER, "<VarDecl>");
                grammer.add(token);
            }
            else {
            }
        }
        else {
        }
    }

    public void VarDef() {
        if (nowType.equals("IDENFR")) {
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                ConstExp();
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                }
                else {
                }
            }
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                InitVal();
            }
            token token = new token(line, position, type.GRAMMER, "<VarDef>");
            grammer.add(token);
        }
        else {
        }
    }

    public void InitVal() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            Exp();
        }
        else if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON") || nowType.equals("LBRACE")) {
                InitVal();
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    InitVal();
                }
            }
            if (nowType.equals("RBRACE")) {
                nextTypeWithForward();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<InitVal>");
        grammer.add(token);
    }

    public void ConstDecl() {
        if (nowType.equals("CONSTTK")) {
            nextTypeWithForward();
            Btype();
            ConstDef();
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                ConstDef();
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<ConstDecl>");
        grammer.add(token);
        //System.out.println("<ConstDecl>");
    }

    public void Btype() {
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
        }
        else {
        }
    }

    public void ConstDef() {
        if (nowType.equals("IDENFR")) {
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                ConstExp();
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                }
                else {
                }
            }
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                ConstInitVal();
                token token = new token(line, position, type.GRAMMER, "<ConstDef>");
                grammer.add(token);
            }
            else {
            }
        }
        else {
        }
    }

    public void ConstInitVal() {
        //判断(、ident/number/+/-/!
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            ConstExp();
        }
        else if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON") || nowType.equals("LBRACE")) {
                ConstInitVal();
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    ConstInitVal();
                }
            }
            if (nowType.equals("RBRACE")) {
                nextTypeWithForward();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<ConstInitVal>");
        grammer.add(token);
    }

    private void ConstExp() {
        AddExp();
        token token = new token(line, position, type.GRAMMER, "<ConstExp>");
        grammer.add(token);
    }

    public void MulExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            UnaryExp();
            while (nowType.equals("MULT") || nowType.equals("DIV") || nowType.equals("MOD")) {
                token token = new token(0, position, type.GRAMMER, "<MulExp>");
                grammer.add(token);
                nextTypeWithForward();
                UnaryExp();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<MulExp>");
        grammer.add(token);
    }

    public void PrimaryExp() {
        if (nowType.equals("LPARENT")) {
            nextTypeWithForward();
            Exp();
            if (nowType.equals("RPARENT")) {
                nextTypeWithForward();
            }
            else {
            }
        }
        else if (nowType.equals("INTCON")) {
            Number();
        }
        else if (nowType.equals("IDENFR")) {
            LVal();
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<PrimaryExp>");
        grammer.add(token);
    }

    public void Number() {
        if (nowType.equals("INTCON")) {
            nextTypeWithForward();
        }
        token token = new token(line, position, type.GRAMMER, "<Number>");
        grammer.add(token);
    }

    public void UnaryExp() {
        if (nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT")) {
            UnaryOp();
            UnaryExp();
        }
        else if (nowType.equals("LPARENT") || nowType.equals("INTCON")) {
            PrimaryExp();
        }
        else if (nowType.equals("IDENFR")) {
            if (getNextType().equals("LPARENT")) {
                nextTypeWithForward();
                nextTypeWithForward();
                if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                    FuncRParams();
                }
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                }
                else {
                }
            }
            else {
                PrimaryExp();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<UnaryExp>");
        grammer.add(token);
    }

    public void FuncRParams() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            Exp();
        }
        while (nowType.equals("COMMA")) {
            nextTypeWithForward();
            Exp();
        }
        token token = new token(line, position, type.GRAMMER, "<FuncRParams>");
        grammer.add(token);
    }

    public void UnaryOp() {
        if (nowType.equals("NOT") || nowType.equals("PLUS") || nowType.equals("MINU")) {
            nextTypeWithForward();
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<UnaryOp>");
        grammer.add(token);
    }

    public void AddExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            MulExp();
            while (nowType.equals("PLUS") || nowType.equals("MINU")) {

                token token = new token(line, position, type.GRAMMER, "<AddExp>");
                grammer.add(token);
                nextTypeWithForward();
                MulExp();

            }
        }
        else {
        }
            token token = new token(line, position, type.GRAMMER, "<AddExp>");
            grammer.add(token);

    }

    public void RelExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            AddExp();
            while (nowType.equals("LSS") || nowType.equals("GRE") || nowType.equals("LEQ") || nowType.equals("GEQ")) {
                token token = new token(line, position, type.GRAMMER, "<RelExp>");
                grammer.add(token);
                nextTypeWithForward();
                AddExp();
            }
        }
        else {
        }
        token token = new token(0, position, type.GRAMMER, "<RelExp>");
        grammer.add(token);
    }

    public void EqExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            RelExp();
            while (nowType.equals("EQL") || nowType.equals("NEQ")) {
                token token = new token(line, position, type.GRAMMER, "<EqExp>");
                grammer.add(token);
                nextTypeWithForward();
                RelExp();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<EqExp>");
        grammer.add(token);
    }

    public void LAndExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            EqExp();
            while (nowType.equals("AND")) {
                token token = new token(line, position, type.GRAMMER, "<LAndExp>");
                grammer.add(token);
                nextTypeWithForward();
                EqExp();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<LAndExp>");
        grammer.add(token);
    }

    public void LOrExp() {
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            LAndExp();
            while (nowType.equals("OR")) {
                token token = new token(line, position, type.GRAMMER, "<LOrExp>");
                grammer.add(token);
                nextTypeWithForward();
                LAndExp();
            }
        }
        else {
        }
        token token = new token(line, position, type.GRAMMER, "<LOrExp>");
        grammer.add(token);
    }

    private void Exp() {
        AddExp();
        token token = new token(line, position, type.GRAMMER, "<Exp>");
        grammer.add(token);
    }

    public void printGrammer() {
        for (int i = 1; i < grammer.size(); i++) {
            if (grammer.get(i).type == type.GRAMMER) {
                System.out.println(grammer.get(i).value);
            }
            else {
                System.out.println(grammer.get(i).type + " " + grammer.get(i).value);
            }
        }
    }

    public void printGrammerToFile() {
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file);
            for (int i = 1; i < grammer.size(); i++) {
                if (grammer.get(i).type == type.GRAMMER) {
                    fileWriter.write(grammer.get(i).value + "\n");
                }
                else {
                    fileWriter.write(grammer.get(i).type + " " + grammer.get(i).value + "\n");
                }
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
