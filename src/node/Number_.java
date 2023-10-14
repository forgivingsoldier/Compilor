package node;
import lexer.token;
import tool.filewritter;

public class Number_ extends node{
    public token token;
    public Number_(token token) {
        this.token = token;
    }

    public void printToFile() {
        filewritter.printToken("INTCON", token.value);
        filewritter.printGrammer("Number");
    }
}
