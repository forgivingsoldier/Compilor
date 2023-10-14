package lexer;

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
