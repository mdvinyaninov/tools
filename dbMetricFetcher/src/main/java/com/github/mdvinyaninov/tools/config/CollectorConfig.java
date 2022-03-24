package com.github.mdvinyaninov.tools.config;

import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class CollectorConfig {
    @Setter
    private String name;
    @Setter
    private Integer interval = 60000;
    @Setter
    private List<String> queryRefs;
    @Setter
    private PostgreSQLSourceConfig postgreSQLConfig;
    @Setter
    private OracleSourceConfig oracleConfig;

    @Setter
    private InfluxDBConfig influxDBConfig;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }
    @XmlAttribute(name = "interval")
    public Integer getInterval() { return interval; }
    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "queryRef")
    public List<String> getQueryRefs() { return queryRefs; }
    @XmlElement(name = "postgresql")
    public PostgreSQLSourceConfig getPostgreSQLConfig() { return postgreSQLConfig; }
    @XmlElement(name = "oracle")
    public OracleSourceConfig getOracleConfig() { return oracleConfig; }
    @XmlElement(name = "influxdb")
    public InfluxDBConfig getInfluxDBConfig() { return influxDBConfig; }
}
