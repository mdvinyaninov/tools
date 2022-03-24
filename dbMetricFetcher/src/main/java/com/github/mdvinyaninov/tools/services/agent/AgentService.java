package com.github.mdvinyaninov.tools.services.agent;

import com.github.mdvinyaninov.tools.config.QueryConfig;
import com.github.mdvinyaninov.tools.data.MeasurementData;
import com.github.mdvinyaninov.tools.services.ServiceException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Service
@Configuration
public class AgentService {
    private static Logger LOGGER = LoggerFactory.getLogger(AgentService.class.getName());

    @Value("${fetch.pool.size.core:2}")
    @Getter
    private int executorPoolSizeCore;

    private ConfigurationService configurationService;
    private FetcherService fetcherService;
    private SenderService senderService;
    private ScheduledExecutorService executorService;

    @Autowired
    public AgentService(
            ConfigurationService configurationService,
            FetcherService fetcherService,
            SenderService senderService) {
        this.configurationService = configurationService;
        this.fetcherService = fetcherService;
        this.senderService = senderService;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing...");
        LOGGER.debug("executorPoolSize: core={}", executorPoolSizeCore);
        executorService = Executors.newScheduledThreadPool(executorPoolSizeCore);
        this.scheduleAll();
    }

    public void cancel() {
        LOGGER.info("Canceling...");
        if (executorService != null)
            executorService.shutdown();
    }

    public void fetchTask(Collector collector, QueryConfig queryConfig) {
        try {
            List<MeasurementData> data = fetcherService.fetch(collector, queryConfig);
            if (data != null && data.size() != 0) {
                Boolean send = queryConfig.getSend();
                if (send == null || send) {
                    senderService.send(collector, data);
                }
                else {
                    LOGGER.trace("not sending");
                }
            }
            else {
                LOGGER.trace("no data");
            }
        }
        catch (Exception e) {
            LOGGER.error("Error", e);
        }
    }

    public void scheduleQuery(Collector collector, QueryConfig queryConfig) {
        try {
            long interval;

            if (queryConfig.getInterval() != null) {
                interval = queryConfig.getInterval();
            } else if (collector.getConfig().getInterval() != null) {
                interval = collector.getConfig().getInterval();
            } else {
                throw new ServiceException("Query fetch interval has not been set.");
            }

            LOGGER.info("Scheduling query '{}' with interval {} ms...",
                    queryConfig.getName(), interval
            );

            executorService.scheduleAtFixedRate(
                    () -> fetchTask(collector, queryConfig), 0, interval, TimeUnit.MILLISECONDS
            );
        }
        catch (Exception e) {
            LOGGER.error("Error", e);
            e.printStackTrace();
        }
    }

    public void scheduleAll() {
        Collection<Collector> collectors = configurationService.getCollectors().values();
        for (Collector collector : collectors) {
            List<QueryConfig> queries = collector.getQueryConfigs();
            for (QueryConfig queryConfig : queries) {
                scheduleQuery(collector, queryConfig);
            }
        }
    }
}
