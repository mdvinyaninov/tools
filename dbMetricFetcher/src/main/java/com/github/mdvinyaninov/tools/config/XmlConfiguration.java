package com.github.mdvinyaninov.tools.config;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name = "configuration")
@XmlType(propOrder = {"queries", "collectors"})
public class XmlConfiguration {
    @Setter
    private List<QueryConfig> queries;
    @Setter
    private List<CollectorConfig> collectors;

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    public List<QueryConfig> getQueries() {
        return queries;
    }

    @XmlElementWrapper(name = "collectors")
    @XmlElement(name = "collector")
    public List<CollectorConfig> getCollectors() {
        return collectors;
    }
}
