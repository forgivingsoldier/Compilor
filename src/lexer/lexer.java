package lexer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import error.*;
import error.Error;

public class lexer {
    public ArrayList<token> tokens;
    public int position;
    public int line;
    public String source;
    public int number;
    public String token;
    public int length;
    public lexer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
        this.tokens = new ArrayList<token>();
        this.token="";
        this.length = source.length();
    }

    public void makeTokens() {
        CheckError checkError=new CheckError();
        ErrorHandling errorHandling=ErrorHandling.getErrorHandling();
        HashMap<String, type> reserved = Map.getReservedMap();
        HashMap<String, type> single_symbol = Map.getSingleSymbolMap();
        HashMap<String, type> double_symbol = Map.getDoubleSymbolMap();

        while(checkEOF(position)) {
            boolean find = false;
            while (checkSpaceAndTab() || checkEnter());
            if (!checkEOF(position)) break;
            token="";
            char current_char = source.charAt(position);
            if(Character.isLetter(current_char)||current_char=='_'){
                token+=current_char;
                position++;
                while(checkEOF(position)&&(Character.isLetterOrDigit(source.charAt(position))||source.charAt(position)=='_')){
                    token+=source.charAt(position);
                    position++;
                }
                tokens.add(new token(line,position,reserved.getOrDefault(token,type.IDENFR),token));
            }
            else if(Character.isDigit(current_char)){
                token+=current_char;
                position++;
                while(checkEOF(position)&&Character.isDigit(source.charAt(position))){
                    token+=source.charAt(position);
                    position++;
                }
                tokens.add(new token(line,position,type.INTCON,token));
                number=Integer.parseInt(token);
            }
            else if(current_char=='"'){
                token+=current_char;
                position++;
                while(checkEOF(position)&&source.charAt(position)!='"'){
                    if(checkError.checkA(source,position)){
                        errorHandling.addError(new Error(line,"a"));
                    }
                    token+=source.charAt(position);
                    position++;
                }
                if(checkEOF(position)){
                    token+=source.charAt(position);
                    position++;
                    tokens.add(new token(line,position,type.STRCON,token));
                }
                else{
                    //error
                }
            }
            //判断+、-、*、%、{}、[]、()、；、，
            else if(current_char=='+'||current_char=='-'||current_char=='*'||current_char=='%'||current_char=='{'||current_char=='}'||current_char=='['||current_char==']'||current_char=='('||current_char==')'||current_char==';'||current_char==','){
                token+=current_char;
                position++;
                tokens.add(new token(line,position,single_symbol.get(token),token));
            }
            //判断<、>、=、！
            else if(current_char=='<'||current_char=='>'||current_char=='='||current_char=='!'){
                token+=current_char;
                position++;
                if(checkEOF(position)&&source.charAt(position)=='='){
                    token+=source.charAt(position);
                    position++;
                    tokens.add(new token(line,position,double_symbol.get(token),token));
                }
                else{
                    tokens.add(new token(line,position,single_symbol.get(token),token));
                }
            }
            else if(current_char=='&'||current_char=='|'){
                token+=current_char;
                position++;
                if(checkEOF(position)&&source.charAt(position)==current_char){
                    token+=source.charAt(position);
                    position++;
                    tokens.add(new token(line,position,double_symbol.get(token),token));
                }
                else{
                    //error
                }
            }
            else if(current_char == '/') { // 第一个 /
                //System.out.println("first /");
                token += current_char;
                position++;
                if (checkEOF(position) && source.charAt(position) == '/') { //
                    // 第二个 /
                    position++;
                    while (checkEOF(position) && source.charAt(position) != '\n') {
                       // System.out.println("yyy");
                        // 非换行字符
                        position++;
                    }
                    if (checkEOF(position)) { // \n 或 直接结束
                        ; // 单行注释末尾的\n
                    }
                }

                else if(checkEOF(position) && source.charAt(position) == '*') {
                    //System.out.println("****");
                    // /* 跨行注释 用状态机判断
                    while(checkEOF(position)&& !find) {  // 状态转换循环（直至末尾）
                        while (checkEOF(position) && source.charAt(position) != '*') {
                        // 非*字符 对应状态q
                        position++;
                        if(checkEnter())
                            {
                                 // 多行注释中 每行最后的回车
                                //System.out.println("fine /n");
                            }
                        }
                        // *
                        while(checkEOF(position) && source.charAt(position) == '*') {
                        // *字符 对应状态q6 如果没有转移到q7，则会在循环中转移到q5
                            position++;
                        }
                        //System.out.println("find *");
                        if (checkEOF(position) && source.charAt(position) == '/') {
                        // /字符 对应状态q7
                            position++;
                            find = true;
                            //if(find)
                               // System.out.println("find /");
                        }
                    }
                }
                else {
                    // 单个 / 对应状态q1
                    tokens.add(new token(line, position, single_symbol.get(token), token));
                    //System.out.println(" single / ");
                }
            }
        }
    }
    public void printTokens(){
        //输出到output.txt
        File file = new File("lexer.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file);
            for (token token : tokens) {
                fileWriter.write(token.type + " " + token.value + " "+token.line+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean checkSpaceAndTab(){
        if (checkEOF(position) &&(source.charAt(position)==' '||source.charAt(position)=='\t')){
            position++;
            return true;
        }
        return false;
    }
    public boolean checkEnter(){
        if (checkEOF(position) && (source.charAt(position)=='\n')){
            line++;
            position++;
            return true;
        }
        return false;
    }
    public boolean checkEOF(int position){
        if (position<length){
            return true;
        }
        return false;
    }
}
