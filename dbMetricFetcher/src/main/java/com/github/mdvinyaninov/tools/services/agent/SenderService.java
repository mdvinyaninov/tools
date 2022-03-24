package com.github.mdvinyaninov.tools.services.agent;

import com.github.mdvinyaninov.tools.data.MeasurementData;
import lombok.Getter;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
@Configuration
public class SenderService {
    private static Logger LOGGER = LoggerFactory.getLogger(AgentService.class.getName());

    @Value("${async.pool.size.core:1}")
    @Getter
    private int asyncPoolSizeCore;

    @Value("${async.pool.size.max:2}")
    @Getter
    private int asyncPoolSizeMax;

    @Async("asyncThreadPoolTaskExecutor")
    public CompletableFuture<Long> send(Collector collector, List<MeasurementData> data) {
        long count = 0;
        try {
            if (collector.getInfluxDB() != null) {
                LOGGER.debug("Sending...");
                for (MeasurementData item: data) {
                    final long unixtime = item.getTimestamp().toInstant().toEpochMilli();
                    final Point point = Point.measurement(item.getMeasurement())
                            .time(unixtime, TimeUnit.MILLISECONDS)
                            .tag("source", item.getSource())
                            .tag(item.getTags())
                            .fields(item.getFields())
                            .build();
                    collector.getInfluxDB().write(point);
                    count++;
                }
                LOGGER.debug("Sending completed. Measurements sent: {}", count);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(count);
    }

    @Bean(name = "asyncThreadPoolTaskExecutor")
    public Executor asyncThreadPoolTaskExecutor() {
        LOGGER.debug("asyncThreadPoolSize: core={} max={}", asyncPoolSizeCore, asyncPoolSizeMax);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncPoolSizeCore);
        executor.setMaxPoolSize(asyncPoolSizeMax);
        executor.setThreadNamePrefix("async-");
        return executor;
    }
}
