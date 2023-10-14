package node;

import lexer.token;
import tool.filewritter;

import java.util.List;

public class Block {
    public List<BlockItem> blockItems;
    public token rightBrace;
    public Block(List<BlockItem> blockItems,token rightBrace){
        this.blockItems = blockItems;
        this.rightBrace = rightBrace;
    }

    public void printToFile() {
        filewritter.printToken("LBRACE","{");
        for (BlockItem blockItem : blockItems) {
            blockItem.printToFile();
        }
        filewritter.printToken("RBRACE","}");
        filewritter.printGrammer("Block");
    }
}
