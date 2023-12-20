package llvm;


import java.util.Map;

public class Symbol {

    public String regId;
    public String value;
    public String returnType;
    public int dim_depth;
    public int dim_num1;
    public int dim_num2;
    public String funcIdent;
    public String addrType;
    public String [] d1Value = null;
    public String [][] d2Value = null;
    public String initValue="";
    //数组
    public Symbol(String regId, String value, int dim_depth, int dim_num1, int dim_num2){
        this.regId = regId;
        this.value = value;
        this.dim_depth = dim_depth;
        this.dim_num1 = dim_num1;
        this.dim_num2 = dim_num2;
    }
    //一维
    public Symbol(String regId, String value){
        this.regId = regId;
        this.value = value;
        this.dim_depth = 0;
        this.dim_num2 = 0;
        this.dim_num1 = 0;
    }
    //函数
    public Symbol(String regId, String value, String returnType, String funcIdent){
        this.regId = regId;
        this.value = value;
        this.returnType = returnType;
        this.funcIdent = funcIdent;
    }
    //函数参数
    public Symbol(String regId, String value, String addrType,int dim_depth, int dim_num1, int dim_num2){
        this.regId = regId;
        this.value = value;
        this.addrType = addrType;
        this.dim_depth = dim_depth;
        this.dim_num1 = dim_num1;
        this.dim_num2 = dim_num2;
    }
    public void setD1(int dim_num1){
        this.dim_num1=dim_num1;
        if(dim_num1==0){d1Value = new String[10000];}
        else{d1Value = new String[dim_num1];}
    }

    public void setD2(int dim_num2){
        this.dim_num2=dim_num2;
        if(this.dim_num1==0){d2Value = new String[10000][dim_num2];}
        else{d2Value = new String[this.dim_num1][dim_num2];}
    }

}
