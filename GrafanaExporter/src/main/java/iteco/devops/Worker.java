package iteco.devops;

import iteco.devops.config.PanelConfig;
import iteco.devops.config.RootConfiguration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class Worker implements Callable<Integer> {
    public static final ZoneId ZONE = ZoneId.systemDefault();
    private static Logger LOGGER = LogManager.getLogger(Worker.class);
    private int attempts = 3;
    private CountDownLatch latch;
    private String requestURI;
    private String path;
    private String file;
    private CloseableHttpClient httpclient;
    private String auth;
    private String name;
    private int result;

    Worker(RootConfiguration root_config, PanelConfig panel_config, LocalDateTime starttime, LocalDateTime endtime, CountDownLatch latch){
        this.latch = latch;
        this.auth = "Bearer " + root_config.getApikey();
        this.attempts = root_config.getDownloadAttempts();
        this.name = panel_config.getName();

        try {
            long starttimeEpoch = starttime.atZone(ZONE).toInstant().toEpochMilli();
            long endtimeEpoch = endtime.atZone(ZONE).toInstant().toEpochMilli();

            GrafanaUrlBuilder b = new GrafanaUrlBuilder()
                .host(root_config.getHost())
                .dashboard(root_config.getDashboard())
                .dashboardUID(root_config.getDashboardUID())
                .panelId(panel_config.getId())
                .from(starttimeEpoch)
                .to(endtimeEpoch)
                .width(panel_config.getWidth())
                .height(panel_config.getHeight())
                .tz(root_config.getTimezone())
                .vars(panel_config.getVars())
                .timeout(root_config.getTimeout())
                .build();

            requestURI = b.getURI();
            path = root_config.getDestination();
            file = panel_config.getName() + ".png";

            httpclient = HTTPClientFactory.getClient();
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Integer call() {
        result = -1;


        LOGGER.info("Downloading '{}' graph ({})...", name, latch.getCount());

        try {
            for (int i = 1; i <= attempts; i++) {
                LOGGER.info("Attempt {} out of {} for '{}'...", i, attempts, name);
                long start = System.currentTimeMillis();
                HttpGet req = new HttpGet(requestURI);
                req.addHeader("Authorization", auth);
                LOGGER.info(req.toString());
                Handler h = new Handler(path, file);
                httpclient.execute(req, h);
                result = h.getResult();

                long duration = System.currentTimeMillis() - start;
                LOGGER.info("Action took {} ms.", duration);

                if (result == 0) {
                    break;
                }
            }
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
            latch.countDown();
        }

        return result;
    }

    public int getResult() {
        return result;
    }
}