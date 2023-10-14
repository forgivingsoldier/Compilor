package lexer;

import java.util.HashMap;

public class Map{
    private static HashMap<String, type> reserved = new HashMap<String, type>(){
        {
            put("const", type.CONSTTK);
            put("int", type.INTTK);
            put("void", type.VOIDTK);
            put("if", type.IFTK);
            put("else", type.ELSETK);
            put("break", type.BREAKTK);
            put("continue", type.CONTINUETK);
            put("return", type.RETURNTK);
            put("main", type.MAINTK);
            put("getint", type.GETINTTK);
            put("printf", type.PRINTFTK);
            put("for", type.FORTK);
        }
    };
    private static HashMap<String,type> single_symbol = new HashMap<String, type>(){
    {
        put("+", type.PLUS);
        put("-", type.MINU);
        put("*", type.MULT);
        put("!", type.NOT);
        put("/", type.DIV);
        put("%", type.MOD);
        put("<", type.LSS);
        put(">", type.GRE);
        put("=", type.ASSIGN);
        put(";", type.SEMICN);
        put(",", type.COMMA);
        put("(", type.LPARENT);
        put(")", type.RPARENT);
        put("[", type.LBRACK);
        put("]", type.RBRACK);
        put("{", type.LBRACE);
        put("}", type.RBRACE);
    }
};
    private static HashMap<String,type> double_symbol =new HashMap<String, type>(){
        {
            put("&&", type.AND);
            put("||", type.OR);
            put("<=", type.LEQ);
            put(">=", type.GEQ);
            put("==", type.EQL);
            put("!=", type.NEQ);
        }
    };
    public static HashMap<String, type> getReservedMap() {
        return reserved;
    }

    public static HashMap<String, type> getSingleSymbolMap() {
        return single_symbol;
    }

    public static HashMap<String, type> getDoubleSymbolMap() {
        return double_symbol;
    }
}
