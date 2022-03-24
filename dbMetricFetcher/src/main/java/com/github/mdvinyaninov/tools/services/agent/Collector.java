package com.github.mdvinyaninov.tools.services.agent;

import com.github.mdvinyaninov.tools.config.QueryConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.github.mdvinyaninov.tools.config.CollectorConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.influxdb.InfluxDB;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class Collector {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private HikariDataSource dataSource;
    @Getter
    @Setter
    private EntityManagerFactory entityManagerFactory;
    @Getter
    @Setter
    private EntityManager entityManager;
    @Getter
    @Setter
    private InfluxDB influxDB;
    @Setter
    @Getter
    private CollectorConfig config;
    @Setter
    @Getter
    private List<QueryConfig> queryConfigs;
    @Setter
    @Getter
    private Map<String, Query> preparedQueries;

    public Collector(String name) {
        this.name = name;
    }
}
