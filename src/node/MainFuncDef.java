package node;

import tool.filewritter;

public class MainFuncDef {
    public Block block;
    public MainFuncDef(Block block){
        this.block = block;
    }
    // MainFuncDef -> 'int' 'main' '(' ')' Block
    public void printToFile() {

        filewritter.printToken("INTTK","int");
        filewritter.printToken("MAINTK","main");
        filewritter.printToken("LPARENT","(");
        filewritter.printToken("RPARENT",")");
        block.printToFile();
        filewritter.printGrammer("MainFuncDef");
    }
}
