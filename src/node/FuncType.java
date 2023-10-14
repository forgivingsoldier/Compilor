package node;
import lexer.token;
import lexer.type;
import tool.filewritter;

public class FuncType {
    public token type;

    public FuncType(token type) {
        this.type = type;
    }

    public void printToFile() {
        filewritter.printToken(type.type.toString(), type.value);
        filewritter.printGrammer("FuncType");
    }
}
