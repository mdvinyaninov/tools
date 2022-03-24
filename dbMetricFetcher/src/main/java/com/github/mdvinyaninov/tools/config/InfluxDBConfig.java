package com.github.mdvinyaninov.tools.config;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;

public class InfluxDBConfig {
    @Setter
    private String url;
    @Setter
    private String database;
    @Setter
    private String retentionPolicy;
    @Setter
    private String username;
    @Setter
    private String password;

    @XmlElement(name = "url")
    public String getUrl() { return url; }
    @XmlElement(name = "database")
    public String getDatabase() { return database; }
    @XmlElement(name = "retentionPolicy")
    public String getRetentionPolicy() { return retentionPolicy; }
    @XmlElement(name = "username")
    public String getUsername() { return username; }
    @XmlElement(name = "password")
    public String getPassword() { return password; }
}
