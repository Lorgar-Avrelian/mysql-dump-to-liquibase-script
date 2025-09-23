package lorgar.avrelian;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    private static final String DUMP_NAME = "dump-db_msp-202509231039.sql";

    public static void main(String[] args) {
        final String dumpPath = "D:" + File.separator + DUMP_NAME;
        try {
            final File dump = new File(dumpPath);
            final Scanner scanner = new Scanner(dump);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}