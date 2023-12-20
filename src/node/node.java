package node;

import llvm.Value;

public class node extends Value{
    public String regId;
    public String value;
    public node(){
        this.regId = "";
        this.value = "";
    }
}
