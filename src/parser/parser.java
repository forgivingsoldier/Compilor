package parser;

import error.Error;
import error.ErrorHandling;
import lexer.token;
import lexer.type;
import node.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class parser {
    public ArrayList<token> tokens;
    public ArrayList<token> grammer;
    public int position;
    public int line;
    public token prevToken;
    public String nowType;
    public int length;
    private ErrorHandling errorHandling = ErrorHandling.getErrorHandling();

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
        } else {
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
        } else {
            return null;
        }
    }

    public String getPreType() {
        if (position < length) {
            return String.valueOf(tokens.get(position - 1).type);
        } else {
            return null;
        }
    }

    public String getNextType() {
        if (position < length) {
            return String.valueOf(tokens.get(position).type);
        } else {
            return null;
        }
    }

    public String getNext2Type() {
        if (position < length) {
            return String.valueOf(tokens.get(position + 1).type);
        } else {
            return null;
        }
    }

    public CompUnit analyze() {
        CompUnit compUnit = CompUnit();
        System.out.println("语法树完成");
        compUnit.printToFile();
        System.out.println("语法树输出完成");
        return compUnit;
    }

    public CompUnit CompUnit() {
        nowType = nextTypeWithForward();
        List<Decl> decls = new ArrayList<>();
        List<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        while (nowType.equals("CONSTTK") || nowType.equals("INTTK") && getNextType().equals("IDENFR") && !getNext2Type().equals("LPARENT")) {
            if (nowType.equals("CONSTTK")) {
                Decl decl = new Decl(ConstDecl(), null);
                decls.add(decl);
            } else {
                Decl decl = new Decl(null, VarDecl());
                decls.add(decl);
            }
            //System.out.println("ConstDecl or VarDecl");
        }
        while (nowType.equals("VOIDTK") || nowType.equals("INTTK") && getNextType().equals("IDENFR") && getNext2Type().equals("LPARENT")) {
            FuncDef funcDef = FuncDef();
            funcDefs.add(funcDef);
        }
        if (nowType.equals("INTTK") && getNextType().equals("MAINTK")) {
            mainFuncDef = MainFuncDef();
        } else {

        }
        token token = new token(line, position, type.GRAMMER, "<CompUnit>");
        grammer.add(token);

        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    public FuncDef FuncDef() {
        FuncType funcType = null;
        token ident = null;
        FuncFParams funcFParams = null;
        Block block = null;
        if (nowType.equals("INTTK") || nowType.equals("VOIDTK")) {
            funcType = FuncType();
            if (nowType.equals("IDENFR")) {
                ident = tokens.get(position - 1);
                nextTypeWithForward();
                if (nowType.equals("LPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("INTTK")) {
                        funcFParams = FuncFParams();
                    }
                    if (nowType.equals("RPARENT")) {
                        nextTypeWithForward();
                        block = Block();
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                        block = Block();
                    }
                } else {
                }
            } else {
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncDef>");
        grammer.add(token);
        return new FuncDef(funcType, ident, funcFParams, block);
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

    public Block Block() {
        List<BlockItem> blockItems = new ArrayList<>();
        token token1 = null;
        if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            while (checkBlock()) {
                blockItems.add(BlockItem());
            }
            if (nowType.equals("RBRACE")) {
                token1 = tokens.get(position - 1);
                nextTypeWithForward();
            } else {
            }
        } else {
        }
        if (position == length)
            grammer.add(prevToken);
        token token = new token(line, position, type.GRAMMER, "<Block>");
        grammer.add(token);
        return new Block(blockItems, token1);
    }

    public BlockItem BlockItem() {
        ConstDecl constDecl = null;
        VarDecl varDecl = null;
        Stmt stmt = null;
        if (nowType.equals("CONSTTK")) {
            constDecl = ConstDecl();
        } else if (nowType.equals("INTTK")) {
            varDecl = VarDecl();
        } else if (checkStmt()) {
            stmt = Stmt();
        } else {
        }
        return new BlockItem(constDecl, varDecl, stmt);
    }

    //语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
    //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public Stmt Stmt() {
        Block block = null;
        Exp exp = null;
        Cond cond = null;
        Stmt stmt1 = null;
        Stmt stmt2 = null;
        LVal lVal = null;
        ForStmt forstmt1 = null;
        ForStmt forstmt2 = null;
        String formatString = null;
        List<Exp> exps = new ArrayList<>();
        //| Block
        if (nowType.equals("LBRACE")) {
            block = Block();
            return new Stmt(block);
        }
        //| [Exp] ';' //有无Exp两种情况
        else if (nowType.equals("SEMICN")) {
            nextTypeWithForward();
            return new Stmt(exp);
        } else if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("INTCON")) {
            exp = Exp();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                return new Stmt(exp);
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                return new Stmt(exp);
            }
        }
        //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        else if (nowType.equals("IFTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                cond = Cond();
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                    stmt1 = Stmt();
                    if (nowType.equals("ELSETK")) {
                        nextTypeWithForward();
                        stmt2 = Stmt();
                    }
                    return new Stmt(cond, stmt1, stmt2);
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                    stmt1 = Stmt();
                    if (nowType.equals("ELSETK")) {
                        nextTypeWithForward();
                        stmt2 = Stmt();
                    }
                    return new Stmt(cond, stmt1, stmt2);
                }
            } else {
            }
        }
        //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
        else if (nowType.equals("FORTK")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                if (nowType.equals("IDENFR")) {
                    forstmt1 = ForStmt();
                }
                if (nowType.equals("SEMICN")) {
                    nextTypeWithForward();
                    if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                        cond = Cond();
                    }
                    if (nowType.equals("SEMICN")) {
                        nextTypeWithForward();
                        if (nowType.equals("IDENFR")) {
                            forstmt2 = ForStmt();
                        }
                        if (nowType.equals("RPARENT")) {
                            nextTypeWithForward();
                            stmt1 = Stmt();
                            return new Stmt(forstmt1, cond, forstmt2, stmt1);
                        } else {
                            errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                            stmt1 = Stmt();
                            return new Stmt(forstmt1, cond, forstmt2, stmt1);
                        }
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                    }
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                }
            }
        }
        //| 'break' ';'
        else if (nowType.equals("BREAKTK")) {
            int line = tokens.get(position - 1).line;
            nextTypeWithForward();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                return new Stmt(line);
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                return new Stmt(line);
            }
        }
        //| 'continue' ';'
        else if (nowType.equals("CONTINUETK")) {
            int line = tokens.get(position - 1).line;
            nextTypeWithForward();
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                return new Stmt(true, line);
            } else {

                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                return new Stmt(true, line);
            }
        }
        //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
        else if (nowType.equals("RETURNTK")) {
            int line = tokens.get(position - 1).line;
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                exp = Exp();
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                return new Stmt(exp, true, line);
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                return new Stmt(exp, true, line);
            }
        }
        //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
        else if (nowType.equals("PRINTFTK")) {
            int line = tokens.get(position - 1).line;
            nextTypeWithForward();
            if (nowType.equals("LPARENT")) {
                nextTypeWithForward();
                formatString = tokens.get(position - 1).value;
                nextTypeWithForward();
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    exps.add(Exp());
                }
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("SEMICN")) {
                        nextTypeWithForward();
                        return new Stmt(formatString, exps, line);
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                        return new Stmt(formatString, exps, line);
                    }
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                    if (nowType.equals("SEMICN")) {
                        nextTypeWithForward();
                        return new Stmt(formatString, exps, line);
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                        return new Stmt(formatString, exps, line);
                    }
                }
            } else {
            }
        }
        //| LVal '=' 'getint''('')'';'LVal '=' Exp ';' // 每种类型的语句都要覆盖
        else if (nowType.equals("IDENFR")) {
            if (checkNotExp()) {
                lVal = LVal();
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
                                    return new Stmt(lVal);
                                } else {
                                    errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                                    return new Stmt(lVal);
                                }
                            } else {
                                errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                                if (nowType.equals("SEMICN")) {
                                    nextTypeWithForward();
                                    return new Stmt(lVal);
                                } else {
                                    errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                                    return new Stmt(lVal);
                                }
                            }
                        } else {
                        }
                    } else if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                        exp = Exp();
                        if (nowType.equals("SEMICN")) {
                            nextTypeWithForward();
                            return new Stmt(lVal, exp);
                        } else {
                            System.out.println(tokens.get(position - 2).value);
                            errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                            return new Stmt(lVal, exp);
                        }
                    } else {
                    }
                }
            } else {
                exp = Exp();
                if (nowType.equals("SEMICN")) {
                    nextTypeWithForward();
                    return new Stmt(exp);
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
                    return new Stmt(exp);
                }
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<Stmt>");
        grammer.add(token);
        return null;
    }

    public boolean checkNotExp() {
        int i = position;
        while (i < length && !String.valueOf(tokens.get(i).type).equals("SEMICN")) {
            if (String.valueOf(tokens.get(i).type).equals("ASSIGN")) {
                return true;
            }
            i++;
        }
        return false;
    }

    public ForStmt ForStmt() {
        LVal lVal = null;
        Exp exp = null;
        if (nowType.equals("IDENFR")) {
            lVal = LVal();
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                    exp = Exp();
                    return new ForStmt(lVal, exp);
                } else {
                }
            } else {
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<ForStmt>");
        grammer.add(token);
        return null;
    }

    public LVal LVal() {
        token ident = null;
        List<Exp> exps = new ArrayList<>();
        if (nowType.equals("IDENFR")) {
            ident = tokens.get(position - 1);
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                exps.add(Exp());
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                }
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<LVal>");
        grammer.add(token);
        return new LVal(ident, exps);
    }

    public Cond Cond() {
        LOrExp lOrExp;
        lOrExp = LOrExp();
        token token = new token(line, position, type.GRAMMER, "<Cond>");
        grammer.add(token);
        return new Cond(lOrExp);
    }

    public FuncType FuncType() {
        token type = null;
        if (nowType.equals("INTTK")) {
            type = tokens.get(position - 1);
            nextTypeWithForward();
        } else if (nowType.equals("VOIDTK")) {
            type = tokens.get(position - 1);
            nextTypeWithForward();
        } else {
        }
        token token = new token(line, position, lexer.type.GRAMMER, "<FuncType>");
        grammer.add(token);
        return new FuncType(type);
    }

    public FuncFParams FuncFParams() {
        List<FuncFParam> funcFParams = new ArrayList<>();
        if (nowType.equals("INTTK")) {
            funcFParams.add(FuncFParam());
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                funcFParams.add(FuncFParam());
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncFParams>");
        grammer.add(token);
        return new FuncFParams(funcFParams);
    }

    public FuncFParam FuncFParam() {
        token ident = null;
        List<ConstExp> constExps = new ArrayList<>();
        boolean isLrack = false;
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
            if (nowType.equals("IDENFR")) {
                ident = tokens.get(position - 1);
                nextTypeWithForward();
                if (nowType.equals("LBRACK")) {
                    nextTypeWithForward();
                    isLrack = true;
                    if (nowType.equals("RBRACK")) {
                        nextTypeWithForward();
                        while (nowType.equals("LBRACK")) {
                            nextTypeWithForward();
                            constExps.add(ConstExp());
                            if (nowType.equals("RBRACK")) {
                                nextTypeWithForward();
                            } else {
                                errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                            }
                        }
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                        while (nowType.equals("LBRACK")) {
                            nextTypeWithForward();
                            constExps.add(ConstExp());
                            if (nowType.equals("RBRACK")) {
                                nextTypeWithForward();
                            } else {
                                errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                            }
                        }
                    }
                }
            } else {
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<FuncFParam>");
        grammer.add(token);
        return new FuncFParam(ident, isLrack, constExps);
    }

    public MainFuncDef MainFuncDef() {
        Block block = null;
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
            if (nowType.equals("MAINTK")) {
                nextTypeWithForward();
                if (nowType.equals("LPARENT")) {
                    nextTypeWithForward();
                    if (nowType.equals("RPARENT")) {
                        nextTypeWithForward();
                        block = Block();
                    } else {
                        errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                        block = Block();
                    }
                }
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<MainFuncDef>");
        grammer.add(token);
        return new MainFuncDef(block);
    }

    public VarDecl VarDecl() {
        List<VarDef> varDefs = new ArrayList<>();
        if (nowType.equals("INTTK")) {
            Btype();
            varDefs.add(VarDef());
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                varDefs.add(VarDef());
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
                token token = new token(line, position, type.GRAMMER, "<VarDecl>");
                grammer.add(token);
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
            }
        } else {
        }
        return new VarDecl(varDefs);
    }

    public VarDef VarDef() {
        token ident = null;
        List<ConstExp> constExps = new ArrayList<>();
        InitVal initVal = null;
        if (nowType.equals("IDENFR")) {
            ident = tokens.get(position - 1);
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                constExps.add(ConstExp());
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                }
            }
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                initVal = InitVal();
            }
            token token = new token(line, position, type.GRAMMER, "<VarDef>");
            grammer.add(token);
        } else {
        }
        return new VarDef(ident, constExps, initVal);
    }

    public InitVal InitVal() {
        Exp exp = null;
        List<InitVal> initVals = new ArrayList<>();
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            exp = Exp();
        } else if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON") || nowType.equals("LBRACE")) {
                initVals.add(InitVal());
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    initVals.add(InitVal());
                }
            }
            if (nowType.equals("RBRACE")) {
                nextTypeWithForward();
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<InitVal>");
        grammer.add(token);
        return new InitVal(exp, initVals);
    }

    public ConstDecl ConstDecl() {
        List<ConstDef> constDefs = new ArrayList<>();
        if (nowType.equals("CONSTTK")) {
            nextTypeWithForward();
            Btype();
            ConstDef constDef = ConstDef();
            constDefs.add(constDef);
            while (nowType.equals("COMMA")) {
                nextTypeWithForward();
                constDefs.add(ConstDef());
            }
            if (nowType.equals("SEMICN")) {
                nextTypeWithForward();
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "i"));
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<ConstDecl>");
        grammer.add(token);
        //System.out.println("<ConstDecl>");
        ConstDecl constDecl = new ConstDecl(constDefs);
        return constDecl;
    }

    public void Btype() {
        if (nowType.equals("INTTK")) {
            nextTypeWithForward();
        } else {
        }
    }

    public ConstDef ConstDef() {
        token ident = null;
        List<ConstExp> constExps = new ArrayList<>();
        ConstInitVal constInitVal = null;
        if (nowType.equals("IDENFR")) {
            ident = tokens.get(position - 1);
            nextTypeWithForward();
            while (nowType.equals("LBRACK")) {
                nextTypeWithForward();
                constExps.add(ConstExp());
                if (nowType.equals("RBRACK")) {
                    nextTypeWithForward();
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "k"));
                }
            }
            if (nowType.equals("ASSIGN")) {
                nextTypeWithForward();
                constInitVal = ConstInitVal();
                token token = new token(line, position, type.GRAMMER, "<ConstDef>");
                grammer.add(token);
            } else {
            }
        } else {
        }
        ConstDef constDef = new ConstDef(ident, constExps, constInitVal);
        return constDef;
    }

    public ConstInitVal ConstInitVal() {
        ConstExp constExp = null;
        List<ConstInitVal> constInitVals = new ArrayList<>();
        //判断(、ident/number/+/-/!
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            constExp = ConstExp();
        } else if (nowType.equals("LBRACE")) {
            nextTypeWithForward();
            if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON") || nowType.equals("LBRACE")) {
                constInitVals.add(ConstInitVal());
                while (nowType.equals("COMMA")) {
                    nextTypeWithForward();
                    constInitVals.add(ConstInitVal());
                }
            }
            if (nowType.equals("RBRACE")) {
                nextTypeWithForward();
            }
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<ConstInitVal>");
        grammer.add(token);

        return new ConstInitVal(constExp, constInitVals);
    }

    public ConstExp ConstExp() {
        AddExp addExp = AddExp();
        token token = new token(line, position, type.GRAMMER, "<ConstExp>");
        grammer.add(token);
        ConstExp constExp = new ConstExp(addExp);
        return constExp;
    }

    public MulExp MulExp() {
//        token operator = null;
//        UnaryExp unaryExp = null;
//        MulExp mulExp = null;
//
//        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
//            unaryExp = UnaryExp();
//            //模仿AddExp
//            if (nowType.equals("MULT")) {
//                operator = tokens.get(position - 1);
//                nextTypeWithForward();
//                mulExp = MulExp();
//
//            } else if (nowType.equals("DIV")) {
//                operator = tokens.get(position - 1);
//                nextTypeWithForward();
//                mulExp = MulExp();
//
//            } else if (nowType.equals("MOD")) {
//                operator = tokens.get(position - 1);
//                nextTypeWithForward();
//                mulExp = MulExp();
//
//            }
//
//        } else {
//        }
//        return new MulExp(unaryExp, operator, mulExp);

        UnaryExp left = UnaryExp(); // 首先解析一个 UnaryExp
        MulExp result = new MulExp(left, null, null); // 创建一个初步的 MulExp 对象

        while (nowType.equals("MULT") || nowType.equals("DIV") || nowType.equals("MOD")) {
            token operator = tokens.get(position - 1);
            nextTypeWithForward();
            UnaryExp right = UnaryExp(); // 解析接下来的 UnaryExp
            result = new MulExp(right, operator, result); // 将当前结果和新的 UnaryExp 结合
        }

        return result;

    }

    public PrimaryExp PrimaryExp() {
        if (nowType.equals("LPARENT")) {
            nextTypeWithForward();
            Exp exp = Exp();
            token token = new token(line, position, type.GRAMMER, "<PrimaryExp>");
            grammer.add(token);
            ;
            if (nowType.equals("RPARENT")) {
                nextTypeWithForward();
                return new PrimaryExp(exp);
            } else {
                errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                return new PrimaryExp(exp);
            }
        } else if (nowType.equals("INTCON")) {
            Number_ number = Number();
            token token = new token(line, position, type.GRAMMER, "<PrimaryExp>");
            grammer.add(token);
            return new PrimaryExp(number);
        } else if (nowType.equals("IDENFR")) {
            LVal lVal = LVal();
            token token = new token(line, position, type.GRAMMER, "<PrimaryExp>");
            grammer.add(token);
            return new PrimaryExp(lVal);
        }
        return null;
    }

    public Number_ Number() {
        token number = null;
        if (nowType.equals("INTCON")) {
            number = tokens.get(position - 1);
            nextTypeWithForward();
        }
        token token = new token(line, position, type.GRAMMER, "<Number>");
        grammer.add(token);
        return new Number_(number);
    }

    //一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况| UnaryOp UnaryExp // 存在即可
    //单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中 // 三种均需覆盖
    public UnaryExp UnaryExp() {
        UnaryOp unaryOp = null;
        UnaryExp unaryExp = null;
        PrimaryExp primaryExp = null;
        token ident = null;
        FuncRParams funcRParams = null;
        if (nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT")) {
            unaryOp = UnaryOp();
            unaryExp = UnaryExp();
            return new UnaryExp(unaryOp, unaryExp);
        } else if (nowType.equals("IDENFR") && getNextType().equals("LPARENT")) {
            if (getNextType().equals("LPARENT")) {
                ident = tokens.get(position - 1);
                nextTypeWithForward();
                nextTypeWithForward();
                if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
                    funcRParams = FuncRParams();

                }
                if (nowType.equals("RPARENT")) {
                    nextTypeWithForward();
                    return new UnaryExp(ident, funcRParams);
                } else {
                    errorHandling.addError(new Error(tokens.get(position - 2).line, "j"));
                    return new UnaryExp(ident, funcRParams);
                }
            } else {
            }
        } else {
            primaryExp = PrimaryExp();
            return new UnaryExp(primaryExp);
        }
        token token = new token(line, position, type.GRAMMER, "<UnaryExp>");
        grammer.add(token);
        return null;
    }

    public FuncRParams FuncRParams() {
        List<Exp> exps = new ArrayList<>();
        if (nowType.equals("LPARENT") || nowType.equals("PLUS") || nowType.equals("MINU") || nowType.equals("NOT") || nowType.equals("IDENFR") || nowType.equals("INTCON")) {
            exps.add(Exp());
        }
        while (nowType.equals("COMMA")) {
            nextTypeWithForward();
            exps.add(Exp());
        }
        token token = new token(line, position, type.GRAMMER, "<FuncRParams>");
        grammer.add(token);
        return new FuncRParams(exps);
    }

    public UnaryOp UnaryOp() {
        token operator = null;
        if (nowType.equals("NOT") || nowType.equals("PLUS") || nowType.equals("MINU")) {
            operator = tokens.get(position - 1);
            nextTypeWithForward();
        } else {
        }
        token token = new token(line, position, type.GRAMMER, "<UnaryOp>");
        grammer.add(token);
        return new UnaryOp(operator);
    }

    public AddExp AddExp() {
        MulExp left = MulExp(); // 解析一个基本的 MulExp
        AddExp result = new AddExp(left, null, null); // 创建一个初步的 AddExp 对象

        while (nowType.equals("PLUS") || nowType.equals("MINU")) {
            token operator = tokens.get(position - 1);
            nextTypeWithForward();
            MulExp right = MulExp(); // 解析接下来的 MulExp
            result = new AddExp(right, operator, result); // 将当前结果和新的 MulExp 结合
        }

        return result;
    }

    public RelExp RelExp() {
        AddExp left = AddExp();
        RelExp result = new RelExp(left, null, null);

        while (nowType.equals("LSS") || nowType.equals("GRE") || nowType.equals("LEQ") || nowType.equals("GEQ")) {
            token operator = tokens.get(position - 1);
            nextTypeWithForward();
            AddExp right = AddExp();
            result = new RelExp(right, operator, result);
        }

        return result;
    }

    public EqExp EqExp() {
        RelExp left = RelExp();
        EqExp result = new EqExp(left, null, null);

        while (nowType.equals("EQL") || nowType.equals("NEQ")) {
            token operator = tokens.get(position - 1);
            nextTypeWithForward();
            RelExp right = RelExp();
            result = new EqExp(right, operator, result);
        }

        return result;
    }

    public LAndExp LAndExp() {
        EqExp left = EqExp();
        LAndExp result = new LAndExp(left, null);

        while (nowType.equals("AND")) {
            nextTypeWithForward();
            EqExp right = EqExp();
            result = new LAndExp( right,result);
        }

        return result;
    }

    public LOrExp LOrExp() {
        LAndExp left = LAndExp();
        LOrExp result = new LOrExp(left, null);

        while (nowType.equals("OR")) {
            nextTypeWithForward();
            LAndExp right = LAndExp();
            result = new LOrExp( right,result);
        }

        return result;
    }

    public Exp Exp() {
        AddExp addExp = AddExp();
        token token = new token(line, position, type.GRAMMER, "<Exp>");
        grammer.add(token);
        return new Exp(addExp);
    }

    public void printGrammer() {
        for (int i = 1; i < grammer.size(); i++) {
            if (grammer.get(i).type == type.GRAMMER) {
                System.out.println(grammer.get(i).value);
            } else {
                System.out.println(grammer.get(i).type + " " + grammer.get(i).value);
            }
        }
    }
}
