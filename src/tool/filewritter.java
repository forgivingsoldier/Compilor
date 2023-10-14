package tool;

import error.Error;

import java.io.File;
import java.util.List;

public class filewritter {
    public static void deleteFile(){
        File file = new File("output.txt");
        if (file.exists()) {
            file.delete();
        }
        file = new File("error.txt");
        if (file.exists()) {
            file.delete();
        }
        file = new File("llvm_ir.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public static void printGrammer(String grammer){
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file, true);
            fileWriter.write("<" + grammer + ">"+"\n");
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("写入文件出错");
            e.printStackTrace();
        }
    }
    public static void printToken(String type, String value) {
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file, true);
            fileWriter.write(type + " " + value + "\n");
            fileWriter.close();

        } catch (Exception e) {
            System.out.println("写入文件出错");
            e.printStackTrace();
        }
    }

    public static void printError(List<Error> errors){
        File file = new File("error.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file);
            for (Error error : errors) {
                fileWriter.write( error.line + " "+error.errorType+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void printIR(String ir){
        File file = new File("llvm_ir.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            java.io.FileWriter fileWriter = new java.io.FileWriter(file,true);
            fileWriter.write(ir);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
