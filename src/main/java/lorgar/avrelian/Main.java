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
    private static final String DUMP_DIRECTORY = "D:";
    private static final Path CURRENT_PATH = Paths.get("").toAbsolutePath();
    private static final String AUTHOR = "tokovenko";
    private static int COUNTER = 1;

    public static void main(String[] args) {
        createScript();
    }

    private static void createScript() {
        final String dumpPath = DUMP_DIRECTORY + File.separator + DUMP_NAME;
        try {
            final String newDumpName = DUMP_NAME.substring(0, DUMP_NAME.lastIndexOf("-")) + ".sql";
            final Path resultPath = Path.of(CURRENT_PATH + File.separator + "result" + File.separator + newDumpName);
            if (!Files.exists(resultPath.getParent())) {
                Files.createDirectory(resultPath.getParent());
            }
            Files.deleteIfExists(resultPath);
            final File result = new File(resultPath.toUri());
            final FileWriter out = new FileWriter(result);
            final BufferedWriter writer = new BufferedWriter(out);
            addBaseHeader(writer);
            addUseCommand(writer, newDumpName);
            final File dump = new File(dumpPath);
            final Scanner scanner = new Scanner(dump);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.startsWith("/*") && !line.startsWith("--") && !line.startsWith("LOCK TABLES")
                        && !line.startsWith("UNLOCK TABLES") && !line.isBlank()) {
                    if (line.startsWith("DROP TABLE") || line.startsWith("CREATE TABLE")
                            || line.startsWith("INSERT INTO"))
                        addChangeset(writer);
                    if (!line.equals("DELIMITER ;;")) {
                        writer.write(line);
                        writer.newLine();
                    } else {
                        addProcedureChangeset(writer);
                    }
                }
            }
            scanner.close();
            writer.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addProcedureChangeset(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.write("-- changeset " + AUTHOR + ":" + COUNTER++ + ":createProcedure:");
        writer.newLine();
        writer.write("DELIMITER");
        writer.newLine();
    }

    private static void addUseCommand(BufferedWriter writer, String newDumpName) throws IOException {
        writer.newLine();
        writer.write("-- changeset " + AUTHOR + ":" + COUNTER++);
        writer.newLine();
        writer.write("USE " + newDumpName.substring(newDumpName.indexOf("-") + 1, newDumpName.indexOf(".")) + ";");
        writer.newLine();
    }

    private static void addBaseHeader(final BufferedWriter writer) throws IOException {
        writer.write("-- liquibase formatted sql");
        writer.newLine();
    }

    private static void addChangeset(BufferedWriter writer) throws IOException {
        writer.newLine();
        writer.write("-- changeset " + AUTHOR + ":" + COUNTER++);
        writer.newLine();
    }
}