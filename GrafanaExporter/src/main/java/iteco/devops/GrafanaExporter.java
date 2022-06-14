package iteco.devops;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import iteco.devops.config.PanelConfig;
import iteco.devops.config.RootConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GrafanaExporter {
    private static Logger LOGGER = LogManager.getLogger(GrafanaExporter.class);
    public static final ZoneId ZONE = ZoneId.systemDefault();
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZONE);

    @Parameter(
            names = {"--starttime", "-s"},
            description = "Test start time"
    )
    private static String starttime = null;
    @Parameter(
            names = {"--endtime", "-e"},
            description = "Test end time"
    )
    private static String endtime = null;
    @Parameter(
            names = {"--config"},
            description = "XML Configuration file"
    )
    private static String config = null;
    @Parameter(
            names = {"--help"},
            description = "Help"
    )
    private static Boolean help = false;

    public static void run(String config, LocalDateTime s, LocalDateTime e) throws Exception  {
        LOGGER.info("Initializing...");
        RootConfiguration root = RootConfiguration.loadConfig(config);
        LOGGER.debug(root);

        int size = root.getPanels().size();
        LOGGER.info("Panels in config: {}", size);

        LOGGER.info("Processing...");
        List<Worker> workers = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(size);

        for (PanelConfig panel : root.getPanels()) {
            Worker w = new Worker(root, panel, s, e, latch);
            workers.add(w);
            w.call();
        }

        latch.await();

        int total = workers.size();
        int passed = 0;
        int failed = 0;

        for (Worker w : workers) {
            int res = w.getResult();

            if (res == 0)
                passed++;
            else
                failed++;
        }

        LOGGER.info(String.format("Total: %d Passed: %d Failed: %d", total, passed, failed));
    }

    public static void main(String[] args) {
        LOGGER.debug("Logging started.");
        GrafanaExporter a = new GrafanaExporter();
        JCommander c = JCommander.newBuilder().programName("GrafanaExporter").addObject(a).build();
        c.parse(args);

        if(!help.booleanValue()) {
            boolean set = true;

            if (starttime == null) {
                LOGGER.error("'starttime' is not specified.");
                set = false;
            }

            if (endtime == null) {
                LOGGER.error("'endtime' is not specified.");
                set = false;
            }

            if (config == null) {
                LOGGER.error("'config' is not specified.");
                set = false;
            }

            if(!set) {
                c.usage();
                System.exit(ResultCodes.FAIL_EXIT_CODE);
            }

            LOGGER.info("config: " + config);
            LOGGER.info("starttime: " + starttime);
            LOGGER.info("endtime: " + endtime);

            LocalDateTime s = null;
            LocalDateTime e = null;

            try {
                s = LocalDateTime.parse(starttime, DTF);
                e = LocalDateTime.parse(endtime, DTF);
            }
            catch (Exception ex) {
                LOGGER.info("Error during parsing parameters.");
                LOGGER.error(ex.getMessage());
                ex.printStackTrace();
                System.exit(ResultCodes.FAIL_EXIT_CODE);
            }

            try {
                run(config, s, e);
            }
            catch (Exception ex) {
                LOGGER.error("Error during execution.");
                LOGGER.error(ex.getMessage());
                ex.printStackTrace();
                System.exit(ResultCodes.FAIL_EXIT_CODE);
            }

            System.exit(ResultCodes.SUCCESS_EXIT_CODE);
        }
        else {
            c.usage();
            System.exit(ResultCodes.HELP_EXIT_CODE);
        }
    }
}
