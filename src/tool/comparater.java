package tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class comparater {
    public String filepath1;
    public String filepath2;
    public comparater(String filepath1, String filepath2){
        this.filepath1 = filepath1;
        this.filepath2 = filepath2;
    }
    public void compare(){
        compareFiles(filepath1,filepath2);
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
