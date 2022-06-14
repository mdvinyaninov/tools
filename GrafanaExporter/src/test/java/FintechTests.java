import iteco.devops.GrafanaExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

public class FintechTests {
    private static Logger LOGGER = LogManager.getLogger(FintechTests.class);
    private static final String PATH = "\\\\smena2.delta.sbrf.ru\\vol2\\DBOYULLT\\testing\\lt1\\devops\\fintech\\lt1\\resources";
    private static final String[] CONFIGS = {
            "GEC_01_General.xml",
            "GEC_02_Utilization.xml",
            "GEC_03_SBBOL_1.xml",
            "GEC_04_SBBOL_2.xml"
    };

    private static final String START = "2022-03-09T13:00:00";
    private static final String END   = "2022-03-09T14:40:00";

    @Test
    void fetch() {
        final LocalDateTime start = LocalDateTime.parse(START, GrafanaExporter.DTF);
        final LocalDateTime end = LocalDateTime.parse(END, GrafanaExporter.DTF);

        try {
            for (int i = 0; i < 4; i++) {
                LOGGER.info("{}: {}", i, CONFIGS[i]);
                String configPath = PATH + File.separator + CONFIGS[i];

                GrafanaExporter.run(configPath, start, end);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
