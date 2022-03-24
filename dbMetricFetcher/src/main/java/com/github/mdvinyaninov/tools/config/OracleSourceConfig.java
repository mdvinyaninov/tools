package com.github.mdvinyaninov.tools.config;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;

public class OracleSourceConfig {
    @Setter
    private String url;
    @Setter
    private String username;
    @Setter
    private String password;

    @XmlElement(name = "url")
    public String getUrl() { return url; }
    @XmlElement(name = "username")
    public String getUsername() { return username; }
    @XmlElement(name = "password")
    public String getPassword() { return password; }
}
