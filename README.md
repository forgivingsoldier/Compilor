> 魏浩哲 21371470

[TOC]

## 一.参考编译器介绍

作为一个较为庞大的项目，参考他人编译器的优点，尤其是结构上面的优点是很有必要的。在这次实验中，我共参考了两个学长的编译器设计，分别是胡峻诚学长，王子懿助教（这两位学长选择的都是生成llvm作为中间代码，与我一开始的目标相同）。我对他们编译器的结构和功能实现进行了深入的研究，下面是我对他们编译器的介绍。

胡峻诚学长将编译器结构分为了前端（词法+语法），语法节点（语法分析中语法树的节点），符号表，中间代码生成，错误处理和后端（目标代码生成和优化）这几个部分。我认为他的结构设计的还是相当一目了然的，符合课内的讲解逻辑。对于接口调用，他采用的是**单例模式**，在compiler.java中调用对应的单例方法去进行编译,并通过控制配置文件去判断是否输出到文件，具体如下：

```java
public static void main(String[] args) throws IOException {
        Config.init();

        Lexer.getInstance().analyze(IOUtils.read(Config.fileInPath));
        if (Config.lexer) {
            Lexer.getInstance().printLexAns();
        }

        Parser.getInstance().setTokens(Lexer.getInstance().getTokens());
        Parser.getInstance().analyze();
        if (Config.parser) {
            Parser.getInstance().printParseAns();
        }

        ErrorHandler.getInstance().compUnitError(Parser.getInstance().getCompUnitNode());

        if (Config.error) {
            ErrorHandler.getInstance().printErrors();
        }

        if (!ErrorHandler.getInstance().getErrors().isEmpty()) {
            return;
        }

        if (Config.ir) {
            LLVMGenerator.getInstance().visitCompUnit(Parser.getInstance().getCompUnitNode());
            IOUtils.llvm_ir_raw(IRModule.getInstance().toString());
            PassModule.getInstance().runIRPasses();
            IOUtils.llvm_ir(IRModule.getInstance().toString());
        }

        if (Config.mips) {
            MipsGenModule.getInstance().loadIR();
            MipsGenModule.getInstance().genMips();
        }
    }
```

当然在研究他的代码的时候，我也发现了他代码结构可以进行优化的地方，他的符号表在错误处理和llvm生成的时候是不同的，也就是说他在进行错误分析的时候设计了一个适用于错误分析的符号表，在llvm生成的时候又使用了另外一种结构的符号表。

王子懿助教将编译器结构分为了词法分析，语法分析+错误处理，符号表和中间代码生成这几部分。他的结构相对来说不太那么整洁，尤其是将所有语法成分都使用一种节点类型，但是他从始至终使用的都是一个符号表，而且对于llvm生成这部分的代码更适合像我这样的小白去阅读理解。对于接口的设计而言，采用类似**工厂模式**的设计方法，即每一个部分都有一个入口程序，然后在入口程序中调用相应的分析器，分析器中调用相应的工厂类（词总表/符号表），工厂类中调用相应的基础类（词）同时实现类中调用相应的方法。每一个工厂的入口程序暴露给总入口 **`Compiler.java`** ，总入口程序调用相应的入口程序，然后调用相应的分析器。

这是他的compiler.java的主方法调用:

```java

public static void main(String[] args)throws Exception{
    BufferedReader filereader=new BufferedReader(new FileReader("testfile.txt"));
    FileWriter fw =new FileWriter("output.txt", false);
    int n=1;
    int check=0;
    String  str;
    Split sentence = new Split();
    //按行读入
    while((str=filereader.readLine())!=null){
        //词法分析
        sentence.setSentence(str,check,n);
        sentence.output();
        check=sentence.getCheck();
        //行号递增
        n+=1;
    }
    //语法分析，使用词法得到的Token表格
    SyntaxMain syntax = new SyntaxMain(sentence.getBank());
    syntax.analyze();
    //语义分析，使用词法得到的Token表格，包含语法，语法+语义+错误一遍处理
    //SemanticMain semantic = new SemanticMain(sentence.getBank());
    //semantic.analyze();
    //生成中间代码
    LLvmMain llvmMain = new LLvmMain(syntax.getAst());
    llvmMain.generate();
    filereader.close();
}

```



## 二.我的编译器总体介绍

### 1.总体结构

我的结构按照实验顺序进行划分，分为词法分析，语法分析，语法树节点，符号表，错误处理和llvm生成，具体建包结构如下图：

<img src="https://raw.githubusercontent.com/forgivingsoldier/image/main/teat/1703042026922.png" style="zoom:50%;" />

在主函数中依次调用各类的方法，每一个部分接收上一个部分的输入，然后把输出传递给下一个部分，这样就可以方便的进行每一步单元测试，且一个模块的重构不会影响其他模块，主函数代码如下：

```java
public class Compiler {
    public static void main(String[] args) {
        try {
            //读取testfile
            InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get("testfile.txt")));
            Scanner scanner = new Scanner(in);
            StringJoiner stringJoiner = new StringJoiner("\n");
            while (scanner.hasNextLine()) {
                stringJoiner.add(scanner.nextLine());
            }
            scanner.close();
            in.close();
            String content=stringJoiner.toString();
            filewritter.deleteFile();
            //词法分析
            lexer lexer = getLexer(content);
            lexer.makeTokens();
            lexer.printTokens();
			//语法分析
            parser parser=getParser(lexer.tokens);
            CompUnit compUnit=parser.analyze();
			//错误处理
            ErrorHandling errorHandling=ErrorHandling.getErrorHandling();
            errorHandling.checkCompunit(compUnit);
			//llvm生成
            if(errorHandling.errors.isEmpty()){
                llvm llvm=getllvm(compUnit);
                llvm.generate();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
```

### 2.接口设计

接口设计采用了单例模式，在主函数调用获取单例的接口，之后可以通过获取的单例调用对应的处理接口，具体如下：

```java
			//词法分析
            lexer lexer = getLexer(content);
            lexer.makeTokens();
            lexer.printTokens();
			//语法分析
            parser parser=getParser(lexer.tokens);
            CompUnit compUnit=parser.analyze();
			//错误处理
            ErrorHandling errorHandling=ErrorHandling.getErrorHandling();
            errorHandling.checkCompunit(compUnit);
			//llvm生成
            if(errorHandling.errors.isEmpty()){
                llvm llvm=getllvm(compUnit);
                llvm.generate();
            }
```

同时，我对输出到对应文件进行了一个工具类的设计，分别有输出词法分析，语法分析，错误处理和llvm的方法：

```java
public class filewritter {
    public static void deleteFile(){
        File file = new File("output.txt");
        if (file.exists()) {
            file.delete();
        }
        file = new File("error.txt");
        if (file.exists()) {
            file.delete();
        }
        file = new File("llvm_ir.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void printGrammer(String grammer){
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file, true);
            fileWriter.write("<" + grammer + ">"+"\n");
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("写入文件出错");
            e.printStackTrace();
        }
    }
    public static void printToken(String type, String value) {
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file, true);
            fileWriter.write(type + " " + value + "\n");
            fileWriter.close();

        } catch (Exception e) {
            System.out.println("写入文件出错");
            e.printStackTrace();
        }
    }

    public static void printError(List<Error> errors){
        File file = new File("error.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file);
            for (Error error : errors) {
                fileWriter.write( error.line + " "+error.errorType+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void printIR(String ir){
        File file = new File("llvm_ir.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file,true);
            fileWriter.write(ir);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 3.文件组织

具体文件结构如下：

```
│  Compiler.java
│  
├─error
│      CheckError.java
│      Error.java
│      ErrorHandling.java
│      
├─lexer
│      lexer.java
│      Map.java
│      token.java
│      type.java
│      
├─llvm
│      llvm.java
│      Symbol.java
│      SymbolTable.java
│      Value.java
│      
├─node
│      AddExp.java
│      Block.java
│      BlockItem.java
│      CompUnit.java
│      Cond.java
│      ConstDecl.java
│      ConstDef.java
│      ConstExp.java
│      ConstInitVal.java
│      Decl.java
│      EqExp.java
│      Exp.java
│      ForStmt.java
│      FuncDef.java
│      FuncFParam.java
│      FuncFParams.java
│      FuncRParams.java
│      FuncType.java
│      InitVal.java
│      LAndExp.java
│      LOrExp.java
│      LVal.java
│      MainFuncDef.java
│      MulExp.java
│      node.java
│      Number_.java
│      PrimaryExp.java
│      RelExp.java
│      Stmt.java
│      UnaryExp.java
│      UnaryOp.java
│      VarDecl.java
│      VarDef.java
│      
├─parser
│      parser.java
│      
├─symbolTable
│      ConstSym.java
│      FuncSym.java
│      Param.java
│      Symbol.java
│      SymbolTableItem.java
│      VarSym.java
│      
└─tool
        comparater.java
        filewritter.java
        Pair.java
```

## 三.词法分析设计

### 1.编码前的设计

我的词法分析的处理方法和课内理论的方法一模一样，流程如下:

<img src="https://raw.githubusercontent.com/forgivingsoldier/image/main/teat/202209142348825.png" style="zoom:50%;" />

### 2.编码完成之后的修改

对于这部分，为了实现更好的结构和后续增删改，我又做了一些设计，具体而言，我将识别的符号作为一个token类并记录单词的行号等后续错误处理要用到的信息，对单词的保留字和字符类型做了一个enum枚举类，并基于这个类对保留关键字创建了三个hashmap（一个是单字母符号，一个是双字母符号，一个是保留字）便于直接识别查找。

```java
//token类
public class token {
    public int line;
    public int position;
    public String value;
    public type type;
    public token(int  line, int position, type type, String value) {
        this.line = line;
        this.position = position;
        this.type = type;
        this.value = value;
    }
}

//保留字枚举类
public enum type {
    IDENFR,
    INTCON,
    STRCON,
    MAINTK,
    CONSTTK,
    INTTK,
    BREAKTK,
    CONTINUETK,
    IFTK,
    ELSETK,
    NOT,
    AND,
    OR,
    FORTK,
    GETINTTK,
    PRINTFTK,
    RETURNTK,
    PLUS,
    MINU,
    MULT,
    DIV,
    MOD,
    LSS,
    LEQ,
    GRE,
    GEQ,
    EQL,
    NEQ,
    ASSIGN,
    SEMICN,
    COMMA,
    LPARENT,
    RPARENT,
    LBRACK,
    RBRACK,
    LBRACE,
    RBRACE,
    VOIDTK,
    GRAMMER
}

//hash图
private static HashMap<String, type> reserved = new HashMap<String, type>() {
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
    private static HashMap<String, type> single_symbol = new HashMap<String, type>() {
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
    private static HashMap<String, type> double_symbol = new HashMap<String, type>() {
        {
            put("&&", type.AND);
            put("||", type.OR);
            put("<=", type.LEQ);
            put(">=", type.GEQ);
            put("==", type.EQL);
            put("!=", type.NEQ);
        }
    };
```

## 四.语法分析设计

### 1.编码前的设计

语法分析就是将存起来的各个token按顺序识别成对应的语法成分并按照语法树存起来，我采用的方法是课内的递归下降分析法，为了避免回溯，我使用了预读一个单词和预读两个单词的方法。一开始我的算数运算表达式addexp和mulexp建立的是将文法改写成右递归的语法树，后来在llvm生成的时候，右递归会出现运算顺序错误的情况（不符合先乘除后加减），所以我在那时将算术运算改成了通过预读生成的左递归树。当然，左递归树也是可以做到llvm生成，只是那时的我没有想到解决办法。

对于打印输出到文件，我将打印语句放到了每个节点的方法中（因为打印并不需要大量的更改代码需求，没必要像递归下降分析过程的时候放在一个文件，太乱了），具体例子如下：

```java
//constinitval节点
public class ConstInitVal extends node{
    public ConstExp constExp;
    public List<ConstInitVal> constInitVals;

    public ConstInitVal(ConstExp constExp, List<ConstInitVal> constInitVals) {
        this.constExp = constExp;
        this.constInitVals = constInitVals;
    }
    
    // 打印
    //ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public void printToFile() {
        if (constExp != null) {
            constExp.printToFile();
        } else {
            filewritter.printToken("LBRACE", "{");
            for (ConstInitVal constInitVal : constInitVals) {
                constInitVal.printToFile();
                if (constInitVals.indexOf(constInitVal) != constInitVals.size() - 1)
                    filewritter.printToken("COMMA", ",");
            }
            filewritter.printToken("RBRACE", "}");
        }
        filewritter.printGrammer("ConstInitVal");
    }
}
```

### 2.编码完成之后的修改

在做llvm代码生成的时候，为了继承属性和综合属性的传递，我又为需要的节点继承了一个node类，这个node类本身也继承了需要递归传递的一些属性值，具体如下：

```java
public class node extends Value{
    public String regId;
    public String value;
    public node(){
        this.regId = "";
        this.value = "";
    }
}


public class Value {
    public  int dim_depth=0;
    public int dim_num1=0;
    public int dim_num2=0;
    public String [] d1Value = null;
    public String [][] d2Value = null;
    public String AddrType="i32";
    public String initValue="";
    public Value(){
        this.dim_depth = 0;
        this.dim_num1 = 0;
        this.dim_num2 = 0;
    }

    public void setD1(int d1){
        this.dim_num1=d1;
        if(d1==0){d1Value = new String[10000];}
        else{d1Value = new String[d1];}
    }

    public void setD2(int d2){
        this.dim_num2=d2;
        if(this.dim_num1==0){d2Value = new String[10000][d2];}
        else{d2Value = new String[this.dim_num1][d2];}
    }

}

```

总的来说，在做语法分析的时候一开始甚至连建语法树的意识都没有，是直接打印到文件中的。后来才发现需要建语法树，对代码进行了大量的修改，再到llvm和错误处理的时候也发现大量需要用到的信息都没有记录，大量前面没考虑到的问题导致后面编码难以实现，最后也只能进行缝缝补补似的修改，总的来说这部分实现的有好有坏。

## 五.错误处理设计

### 1.编码前的设计

对于错误处理，我是边建立符号表边进行错误分析，我先说说我的符号表设计。我建立了一个符号类，和一个符号表项类，在错误处理程序中可以直接使用。类似语法分析，我在做错误处理的时候是遍历语法树节点。

```java
    public CheckError checkError = new CheckError();//工具类
    public List<Error> errors = new ArrayList<>();//错误表
    public List<SymbolTableItem> symbolTable = new ArrayList<>();//符号表
	
	//递归下降分析
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
	

```

其中CheckError是我写的一个检测错误工具类，负责检测除了i,j,k这三个的其余错误,因为ijk这三个错误如果在语法分析的时候不做处理的话会影响建树，所以这三个错误是在语法分析的时候处理的，具体来讲就是一旦发现终结符不存在就直接忽略，读下一个token。

```java
//非法符号	a	格式字符串中出现非法字符报错行号为 <FormatString> 所在行数。	<FormatString> → ‘“‘{<Char>}’”
    public boolean checkA(String source, int position) {
        ...
    }

    //名字重定义	b	函数名或者变量名在当前作用域下重复定义。注意，变量一定是同一级作用域下才会判定出错，不同级作用域下，内层会覆盖外层定义。报错行号为 <Ident> 所在行数。	<ConstDef>→<Ident> …
    //<VarDef>→<Ident> … <Ident> …
    //<FuncDef>→<FuncType><Ident> …
    //<FuncFParam> → <BType> <Ident> …
    public boolean checkB(String ident, List<SymbolTableItem> symbolTableItems) {
	   ...
    }

    public boolean checkC(String ident, List<SymbolTableItem> symbolTableItems) {
       ...
    }

    public String checkDandE(UnaryExp unaryExp, List<SymbolTableItem> symbolTable) {
        ...
    }

    //无返回值的函数存在不匹配的return语句	f	报错行号为 ‘return’ 所在行号。	<Stmt>→‘return’ {‘[’<Exp>’]’}‘;’
    public boolean checkF(List<SymbolTableItem> symbolTableItems, Stmt stmt) {
    	...
    }

    //有返回值的函数缺少return语句	g	只需要考虑函数末尾是否存在return语句，无需考虑数据流。报错行号为函数结尾的’}’ 所在行号。	<FuncDef> → <FuncType> <Ident> ‘(’ [<FuncFParams>] ‘)’ <Block>
    //<MainFuncDef> → ‘int’ ‘main’ ‘(’ ‘)’ <Block>
    public boolean checkG(List<SymbolTableItem> symbolTableItems, Block block) {
        ...
    }

    //不能改变常量的值	h	<LVal>为常量时，不能对其修改。报错行号为 <LVal> 所在行号。	<Stmt>→<LVal>‘=’ <Exp>‘;’<Stmt>→<LVal>‘=’ ‘getint’ ‘(’ ‘)’ ‘;’
    public boolean checkH(String ident, List<SymbolTableItem> symbolTableItems) 	{			...
    }

    //printf中格式字符与表达式个数不匹配	l	报错行号为 ‘printf’ 所在行号。	<Stmt> →‘printf’‘(’<FormatString>{,<Exp>}’)’‘;’
    public boolean checkL(String formatString, int ExpCount) {
       ...
    }
```

### 2.编码完成之后的修改

这部分代码在编码完成之后就没有修改了，唯一的使用就是在llvm生成之前先判断错误是否为0，是的话就不生成llvm代码

```java
 if(errorHandling.errors.isEmpty()){
                llvm llvm=new llvm(compUnit);
                llvm.generate();
 }
```

## 六.代码生成设计

### 编码前的设计和编码时的处理

我生成的是llvm代码，同时因为我是第一次接触llvm也并没有生成mip代码的意向，我并没有按照标准的institution,value等标准的建包建类格式去完成，也并没有将llvm代码作为一种数据结构去存起来，而是直接将llvm代码打印到文件中,我参考了[软院的教程]([LLVM 相关内容 · GitBook (buaa-se-compiling.github.io)](https://buaa-se-compiling.github.io/miniSysY-tutorial/pre/llvm.html) )。代码生成1 的实验包含lab1，2，3，5，8。 代码生成2 的实验包含lab4，6，7。总的来说，我还是采用了遍历语法树节点的方法，下面我就依次说下我每个Lab的生成逻辑。

对于符号表，我采用了和错误处理一样的结构，并定义了一些操作符号表的函数，之所以使用符号表是因为左值函数需要使用定义过的变量，以及对于全局变量和局部变量的处理生成方式会不一样。采用我这样的方式，第一个符号表项就是定义全局变量的符号表项，便于识别。

```java
	private List<SymbolTableItem> symbolTable;
	public SymbolTable nowTable() {
        return symbolTable.get(symbolTable.size() - 1);
    }

    public void pushTable() {
        symbolTable.add(new SymbolTable(new HashMap<>(), symbolTable.size()));
    }

    public void popTable() {
        symbolTable.remove(symbolTable.size() - 1);
    }

    public Pair<Symbol, Integer> getSymbol(String value) {
        for (int i = symbolTable.size() - 1; i >= 0; i--) {
            if (symbolTable.get(i).table.containsKey(value)) {
                // 正确创建 Pair 对象并返回
                return new Pair<>(symbolTable.get(i).table.get(value), symbolTable.get(i).level);
            }
        }
        return null;
    }
```

对lab1的main函数，其实就是进入mainfuncdef的时候打印`define dso_local i32 @main()`,倒没什么好说的。唯一的关键是return的值的传递，这也是课内综合属性传递的意思，调用逻辑是通过block的stmt的return再调用exp，我们计划在处理return的时候打印exp的值，那么就需要exp将计算的值返回过来（无论是寄存器还是数字），这里就用到了语法树节点继承的node来实现，具体return代码示例如下

```java
// 'return' [Exp] ';' // 1.有Exp 2.无Exp
        else if (type.equals("RETURN")) {
            if (stmt.exp != null) {
                exp(stmt.exp);//调用exp去处理，记得在exp中存值
                printf("ret i32 " + stmt.exp.value + "\n");
            }
        }
```

对于lab2的常量表达式，也是需要综合属性的传递，此外由于在定义全局变量的时候不需要输出计算过程，所以需要进行当前符号表的level的判断，具体addexp代码如下，其余类似：

```java
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
```

对于lab3的非数组局部变量定义，其实就是先alloc ，如果存在initval就进行store，然后把这个变量放在符号表中。对于const的处理也一样，只不过constinitval一定是存在的罢了。示例代码如下:

```java
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
        } 
        else{//数组}
}
```

对于lab5的基本块，其实就是在进入block后新建一个符号表罢了

```java
private void block(Block block, Boolean isFunc) {
        if (!isFunc)
            pushTable();
      
        for (BlockItem blockItem : block.blockItems) {
            
            blockItem(blockItem);
        }
        popTable();
    }
```

对于lab4的条件语句，因为需要短路求值，所以我在cond语法节点中设置了yeslabel,nolabel,nextlabel这三个标签变量，分别对应着条件为1，为0和结束之后需要跳转到标签，下为处理代码

```java
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
```

对于lab6涉及的for循环`'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt`，我设置了 forstmt1Label， condLabel，
 forstmt2Label，stmtLabel， nextLabel这几个标签，因为break和continue的存在，我需要block ，blockitem也要有这些标签（因为break和continue都会在for语句的block中·，而且break和continue需要知道自己外层for的标签以此来跳转），for的代码示例如下

```java
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
```

对于数组部分，主要是数组出现在变量定义，函数形参和左值lval的引用，形参的话主要是处理在函数中的形参会出现`a[][5]`的情况，需要储存在符号表的时候为第一维的长度设置一个足够大的值，其次对于左值，也就是对数组的使用，最关键的是定义的函数中使用数组左值和主函数使用数组左值的时候需要分开处理。所以lval调用数组会有多种可能性，需要枚举处理。其中一个例子如下

```java
//函数中的左值，1维数组取0维
//函数中的左值，1维数组取0维
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
```



