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
    private static int PART = 1;

    public static void main(String[] args) {
        createScript();
    }

    private static void createScript() {
        final String dumpPath = DUMP_DIRECTORY + File.separator + DUMP_NAME;
        try {
            BufferedWriter writer = getWriter(null);
            final File dump = new File(dumpPath);
            final Scanner scanner = new Scanner(dump);
            boolean shouldBeNextFile = false;
            boolean blocked = false;
            boolean skiped = false;
            while (scanner.hasNextLine()) {
                if (COUNTER % 100 == 0) {
                    shouldBeNextFile = true;
                }
                String line = scanner.nextLine();
                if (shouldBeNextFile && !blocked) {
                    writer = getWriter(writer);
                    shouldBeNextFile = false;
                }
                if (line.startsWith("SET")) skiped = !skiped;
                if (!line.startsWith("/*") && !line.startsWith("--") && !line.startsWith("LOCK TABLES")
                        && !line.startsWith("UNLOCK TABLES") && !line.isBlank() && !skiped) {

                    if (line.startsWith("DROP TABLE") || line.startsWith("CREATE TABLE")
                            || line.startsWith("INSERT INTO")) {
                        addChangeset(writer);
                    }

                    if (line.contains("DELIMITER ;;")) {
                        blocked = true;
                        addProcedureChangeset(writer);
                    } else if (line.contains("DELIMITER ;") && !line.contains("DELIMITER ;;")) {
                        blocked = false;
                        writeLine(writer, line);
                    } else {
                        writeLine(writer, line);
                    }
                }
            }
            scanner.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    private static BufferedWriter getWriter(BufferedWriter previous) throws IOException {
        if (previous != null) {
            previous.close();
        }
        final String newDumpName = DUMP_NAME.substring(DUMP_NAME.indexOf("-") + 1, DUMP_NAME.lastIndexOf("-")) + "_" + PART++ + ".sql";
        final Path resultPath = Path.of(CURRENT_PATH + File.separator + "result" + File.separator + newDumpName);
        if (!Files.exists(resultPath.getParent())) {
            Files.createDirectory(resultPath.getParent());
        }
        Files.deleteIfExists(resultPath);
        final File result = new File(resultPath.toUri());
        final BufferedWriter writer = new BufferedWriter(new FileWriter(result));
        addBaseHeader(writer);
        addUseCommand(writer, newDumpName);
        return writer;
    }

    private static void addProcedureChangeset(BufferedWriter writer) throws IOException {
        writer.newLine();
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++ + ":createProcedure:");
        writeLine(writer, "DELIMITER");
    }

    private static void addUseCommand(BufferedWriter writer, String newDumpName) throws IOException {
        writer.newLine();
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++);
        writeLine(writer, "USE " + newDumpName.substring(0, newDumpName.indexOf("_")) + ";");
    }

    private static void addBaseHeader(final BufferedWriter writer) throws IOException {
        writeLine(writer, "-- liquibase formatted sql");
    }

    private static void addChangeset(BufferedWriter writer) throws IOException {
        writer.newLine();
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++);
    }
}