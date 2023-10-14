package node;
import lexer.token;
import tool.filewritter;

public class UnaryOp {
    public token token;
    public UnaryOp(token token) {
        this.token = token;
    }

    public void printToFile() {
        filewritter.printToken(token.type.toString(), token.value);
        filewritter.printGrammer("UnaryOp");
    }
}
