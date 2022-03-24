package com.github.mdvinyaninov.tools.services.agent;

import com.github.mdvinyaninov.tools.XMLUtils;
import com.github.mdvinyaninov.tools.config.*;
import com.zaxxer.hikari.HikariDataSource;
import com.github.mdvinyaninov.tools.services.ServiceException;
import lombok.Getter;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigurationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class.getName());
    private static final String POSTGRESQL_DRIVER_CLASS = "org.postgresql.Driver";
    private static final String POSTGRESQL_PLATFORM = "postgresql";
    private static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";
    private static final String ORACLE_PLATFORM = "oracle";

    private static final int INFLUXDB_BATCH_SIZE = 2000;
    private static final int INFLUXDB_BATCH_INTERVAL_MS = 10000;

    private static final long DATASOURCE_CONN_TIMEOUT = 10000;
    private static final long DATASOURCE_POOL_IDLE_TIMEOUT = 120000;
    private static final int  DATASOURCE_POOL_SIZE_MAX = 10;

    @Value("${collector.configuration.file:collector.config.xml}")
    @Getter
    private String xmlConfigurationFile;

    private XmlConfiguration xmlConfiguration;

    @Getter
    private Map<String, Collector> collectors;

    @PostConstruct
    public void init() {
        try {
            loadXmlConfiguration();
            initCollectors();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("Error while performing configuration", e);
        }
    }

    public XmlConfiguration loadXmlConfiguration() {
        try {
            LOGGER.trace("collector.configuration.file={}", xmlConfigurationFile);
            xmlConfiguration = (XmlConfiguration) XMLUtils.loadResource(
                    XmlConfiguration.class, xmlConfigurationFile
            );
        }
        catch (IOException e) {
            throw new ServiceException("Error while loading configuration file", e);
        }
        return xmlConfiguration;
    }

    public void initCollectors() {
        // parsing queries
        final Map<String, QueryConfig> queries = new HashMap<>();
        for (QueryConfig queryConfig : xmlConfiguration.getQueries()) {
            queries.put(queryConfig.getName(), queryConfig);
        }

        collectors = new ConcurrentHashMap<>();
        // parsing collectors
        for (CollectorConfig collectorConfig : xmlConfiguration.getCollectors()) {
            final String collectorName = collectorConfig.getName();
            LOGGER.info("Collector: " + collectorName);

            final PostgreSQLSourceConfig postgreSQLConfig = collectorConfig.getPostgreSQLConfig();
            final OracleSourceConfig oracleConfig = collectorConfig.getOracleConfig();

            HikariDataSource dataSource;
            LocalContainerEntityManagerFactoryBean emFactoryBean;
            EntityManagerFactory entityManagerFactory;
            EntityManager entityManager;

            if (postgreSQLConfig != null && oracleConfig != null) {
                throw new ServiceException("Multiple types of database source specified.");
            }
            // postgresql
            else if (postgreSQLConfig != null) {
                LOGGER.info("Source: postgresql");
                dataSource = postgreDataSource(postgreSQLConfig);
                emFactoryBean = postgreEMFactoryBean(dataSource);
                entityManagerFactory = entityManagerFactory(emFactoryBean);
                entityManager = entityManagerFactory.createEntityManager();
            }
            // oracle
            else if (oracleConfig != null) {
                LOGGER.info("Source: oracle");
                dataSource = oracleDataSource(oracleConfig);
                emFactoryBean = oracleEMFactoryBean(dataSource);
                entityManagerFactory = entityManagerFactory(emFactoryBean);
                entityManager = entityManagerFactory.createEntityManager();
            }
            else {
                throw new ServiceException("No database sources specified.");
            }

            InfluxDB influxDB = null;
            if (collectorConfig.getInfluxDBConfig() == null) {
                LOGGER.warn("No InfluxDB destination specified");
            }
            else {
                LOGGER.info("Destination: influxdb");
                //influxdb
                final InfluxDBConfig influxDBConfig = collectorConfig.getInfluxDBConfig();
                influxDB = influxDB(influxDBConfig);
            }

            //queries
            List<QueryConfig> queryList = new ArrayList<>();
            Map<String, Query> preparedQueries = new ConcurrentHashMap<>();
            for (String queryRef : collectorConfig.getQueryRefs()) {
                LOGGER.info("Query Ref: {}", queryRef);
                QueryConfig queryConfig = queries.get(queryRef);
                if (queryConfig != null) {
                    queryList.add(queryConfig);
                    // native queries
                    Query preparedQuery = entityManager.createNativeQuery(queryConfig.getText());
                    preparedQueries.put(queryConfig.getName(), preparedQuery);
                }
                else throw new ExceptionInInitializerError("Query not found by reference '" + queryRef + "'");
            }

            final Collector collector = new Collector(collectorName);
            collector.setConfig(collectorConfig);
            collector.setEntityManagerFactory(entityManagerFactory);
            collector.setEntityManager(entityManager);
            collector.setPreparedQueries(preparedQueries);
            collector.setQueryConfigs(queryList);
            collector.setQueryConfigs(queryList);
            collector.setDataSource(dataSource);
            collector.setInfluxDB(influxDB);
            collectors.put(collectorName, collector);
        }
    }

    private HikariDataSource postgreDataSource(PostgreSQLSourceConfig config) {
        String url = config.getUrl();
        // "prepared statement 'S_*' already exist' error fix when reusing connection ;
        if (!url.contains("?prepareThreshold=0"))
            url = url + "?prepareThreshold=0";
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(POSTGRESQL_DRIVER_CLASS)
                .url(url)
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
        dataSource.setPoolName("hikari");
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(DATASOURCE_POOL_SIZE_MAX);
        dataSource.setIdleTimeout(DATASOURCE_POOL_IDLE_TIMEOUT);
        dataSource.setConnectionTimeout(DATASOURCE_CONN_TIMEOUT);
        return dataSource;
    }

    private HikariDataSource oracleDataSource(OracleSourceConfig config) {
        String url = config.getUrl();
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(ORACLE_DRIVER_CLASS)
                .url(url)
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
        dataSource.setPoolName("hikari");
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(DATASOURCE_POOL_SIZE_MAX);
        dataSource.setIdleTimeout(DATASOURCE_POOL_IDLE_TIMEOUT);
        dataSource.setConnectionTimeout(DATASOURCE_CONN_TIMEOUT);
        return dataSource;
    }

    private LocalContainerEntityManagerFactoryBean postgreEMFactoryBean(HikariDataSource dataSource) {
        final HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.POSTGRESQL);
        final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("iteco.lt");
        factoryBean.setPersistenceUnitName("postgre");
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    private LocalContainerEntityManagerFactoryBean oracleEMFactoryBean(HikariDataSource dataSource) {
        final HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.ORACLE);
        final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("iteco.lt");
        factoryBean.setPersistenceUnitName("oracle");
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    private EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean emFactoryBean) {
        return emFactoryBean.getObject();
    }

    private PlatformTransactionManager buildTransactionManager(EntityManagerFactory emFactoryBean) {
        return new JpaTransactionManager(emFactoryBean);
    }

    private InfluxDB influxDB(InfluxDBConfig config) {
        final String url = config.getUrl();
        final String username = config.getUsername();
        InfluxDB influx;
        if (username != null && !username.isEmpty()) {
            influx = InfluxDBFactory.connect(url, config.getUsername(), config.getPassword());
        }
        else influx = InfluxDBFactory.connect(url);
        influx.setRetentionPolicy(config.getRetentionPolicy());
        influx.setDatabase(config.getDatabase());
        influx.enableBatch(INFLUXDB_BATCH_SIZE, INFLUXDB_BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        return influx;
    }
}
