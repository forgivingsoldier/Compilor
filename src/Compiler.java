import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;

import error.ErrorHandling;
import node.CompUnit;
import parser.parser;
import lexer.lexer;
import llvm.llvm;
import tool.*;

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

            filewritter.deleteFile();
            lexer lexer = new lexer(content);
            lexer.makeTokens();
            lexer.printTokens();

            parser parser=new parser(lexer.tokens);
            System.out.println("开始语法分析");
            CompUnit compUnit=parser.analyze();
            System.out.println("语法分析完成");

            ErrorHandling errorHandling=ErrorHandling.getErrorHandling();
            errorHandling.checkCompunit(compUnit);
            //parser.printGrammerToFile();
//            comparater comparater=new comparater("output.txt","output1.txt");
//            comparater.compare();
            llvm llvm=new llvm(compUnit);
            llvm.generate();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
