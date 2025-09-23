package lorgar.avrelian;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    private static final String DUMP_NAME = "dump-db_msp-202509231039.sql";

    public static void main(String[] args) {
        final String dumpPath = "D:" + File.separator + DUMP_NAME;
        final Path currentPath = Paths.get("").toAbsolutePath();
        try {
            final String newDumpName = DUMP_NAME.substring(0, DUMP_NAME.lastIndexOf("-")) + ".sql";
            final Path resultPath = Path.of(currentPath + File.separator + "result" + File.separator + newDumpName);
            if (!Files.exists(resultPath.getParent())) {
                Files.createDirectory(resultPath.getParent());
            }
            Files.deleteIfExists(resultPath);
            final File result = new File(resultPath.toUri());
            final BufferedWriter writer = new BufferedWriter(new FileWriter(result));
            final File dump = new File(dumpPath);
            final Scanner scanner = new Scanner(dump);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.startsWith("/*") && !line.startsWith("--")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            scanner.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}