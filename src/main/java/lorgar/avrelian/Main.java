package lorgar.avrelian;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final List<String> DUMPS = new ArrayList<>(List.of(
            "dump-db_msp-202509231039.sql",
            "dump-db_msp_logs-202509231040.sql",
            "dump-db_msp_sysmng-202509231040.sql",
            "dump-nms_comp-202509231040.sql"
    ));
    private static String DUMP_NAME;
    private static final String DUMP_DIRECTORY = "D:";
    private static final Path CURRENT_PATH = Paths.get("").toAbsolutePath();
    private static final String AUTHOR = "tokovenko";
    private static String CURRENT_IP;
    private static int COUNTER = 1;
    private static int PART;

    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String CURRENT_IP = ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        for (String dump : DUMPS) {
            createScript(dump);
        }
        COUNTER = 1;
    }

    private static void createScript(String currentDump) {
        PART = 1;
        DUMP_NAME = currentDump;
        final String dumpPath = DUMP_DIRECTORY + File.separator + DUMP_NAME;
        try {
            BufferedWriter writer = getWriter(null);
            final File dump = new File(dumpPath);
            final Scanner scanner = new Scanner(dump);
            boolean shouldBeNextFile = false;
            boolean delimiterBlocked = false;
            boolean createBlocked = false;
            boolean skiped = false;
            while (scanner.hasNextLine()) {
                if (COUNTER % 100 == 0) {
                    shouldBeNextFile = true;
                }
                String line = scanner.nextLine()
                        .replaceAll("192.168.1.70", CURRENT_IP)
                        .replaceAll("com/nmscom/", "ru/opk_bulat/")
                        .replaceAll("com.nmscom", "ru.opk_bulat")
                        .replaceAll("nmscom", "opk_bulat");
                if (shouldBeNextFile && !delimiterBlocked && !createBlocked) {
                    writer = getWriter(writer);
                    shouldBeNextFile = false;
                }
                if (line.contains("CREATE") && line.contains("TRIGGER") && !line.contains("INSERT INTO") && !line.contains("VIEW")) {
                    line = "CREATE " + line.trim().substring(line.indexOf("TRIGGER"));
                }
                String lineCheck = line.trim().toUpperCase();
                if (lineCheck.contains("END */;;") || lineCheck.contains("END ;;")) line = "END;";
                if (lineCheck.equals("BEGIN")) line = "BEGIN";
                if (line.startsWith("SET") && !line.contains("SESSION")) skiped = !skiped;
                if (lineCheck.contains("CREATE DEFINER") && lineCheck.contains("FUNCTION"))
                    line += " DETERMINISTIC READS SQL DATA";
                if (!line.startsWith("/*") && !line.startsWith("--") && !line.startsWith("LOCK TABLES")
                        && !line.startsWith("UNLOCK TABLES") && !line.startsWith("SET") && !line.isBlank() && !skiped) {

                    if (line.startsWith("DROP TABLE") || line.startsWith("CREATE TABLE")
                            || line.startsWith("INSERT INTO")) {
                        addChangeset(writer);
                        createBlocked = true;
                    }

                    if (line.contains("DELIMITER ;;")) {
                        delimiterBlocked = true;
                        addProcedureChangeset(writer);
                    } else if (line.contains("DELIMITER ;") && !line.contains("DELIMITER ;;")) {
                        delimiterBlocked = false;
                        addProcedureEnd(writer);
                    } else {
                        writeLine(writer, line);
                    }

                    if (line.trim().endsWith(";")) createBlocked = false;
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
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++ + " runOnChange:true endDelimiter:/ stripComments:false");
    }

    private static void addProcedureEnd(BufferedWriter writer) throws IOException {
        writeLine(writer, "/");
        writer.newLine();
        writeLine(writer, "--rollback not required");
    }

    private static void addUseCommand(BufferedWriter writer, String newDumpName) throws IOException {
        writer.newLine();
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++);
        writeLine(writer, "USE " + newDumpName.substring(0, newDumpName.lastIndexOf("_")) + ";");
    }

    private static void addBaseHeader(final BufferedWriter writer) throws IOException {
        writeLine(writer, "-- liquibase formatted sql");
    }

    private static void addChangeset(BufferedWriter writer) throws IOException {
        writer.newLine();
        writeLine(writer, "-- changeset " + AUTHOR + ":" + COUNTER++);
    }
}