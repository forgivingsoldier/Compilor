import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;
import parser.parser;
import lexer.lexer;

public class Compiler {
    public static void main(String[] args) {
        File filepath =new File("testfile.txt");
        String str;
        try {
            InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get("testfile.txt")));
            Scanner scanner = new Scanner(in);
            StringJoiner stringJoiner = new StringJoiner("\n");
            while (scanner.hasNextLine()) {
                stringJoiner.add(scanner.nextLine());
            }
            scanner.close();
            in.close();
            String content=stringJoiner.toString();

            lexer lexer = new lexer(content);
            lexer.makeTokens();
            lexer.printTokens();

            parser parser=new parser(lexer.tokens);
            parser.analyze();
            parser.printGrammerToFile();

            //String filePath1 = "output.txt";
            //tring filePath2 = "output1.txt";
            //compareFiles(filePath1, filePath2);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void compareFiles(String filePath1, String filePath2) {
        try (BufferedReader br1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader br2 = new BufferedReader(new FileReader(filePath2))) {

            String line1 = br1.readLine();
            String line2 = br2.readLine();

            int lineNum = 1;
            while (line1 != null || line2 != null) {
                if (line1 == null || line2 == null) {
                    System.out.println("Difference at line " + lineNum + ":");
                    System.out.println("\tFile1: " + line1);
                    System.out.println("\tFile2: " + line2);
                } else if (!line1.equals(line2)) {
                    System.out.println("Difference at line " + lineNum + ":");
                    System.out.println("\tFile1: " + line1);
                    System.out.println("\tFile2: " + line2);
                }

                line1 = br1.readLine();
                line2 = br2.readLine();
                lineNum++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
