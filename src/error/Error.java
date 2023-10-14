package error;

public class Error {
    public int line;
    public String errorType;
    public Error(int line, String errorType){
        this.line = line;
        this.errorType = errorType;
    }
}
