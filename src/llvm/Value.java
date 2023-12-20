package llvm;

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
