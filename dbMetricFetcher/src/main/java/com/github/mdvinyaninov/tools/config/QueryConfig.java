package com.github.mdvinyaninov.tools.config;

import com.github.mdvinyaninov.tools.config.adapters.CDATAAdapter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

public class QueryConfig {
    @Setter
    private String name;
    @Setter
    private Boolean send;
    @Setter
    private Integer interval;
    @Setter
    private String text;
    @Setter
    private List<Column> columns;

    @XmlAttribute(name = "interval")
    public Integer getInterval() { return interval; }
    @XmlAttribute(name = "send")
    public Boolean getSend() { return send; }
    @XmlElement(name = "name")
    public String getName() {
        return name;
    }
    @XmlElement(name = "text")
    @XmlJavaTypeAdapter(CDATAAdapter.class)
    public String getText() {
        return text;
    }
    @XmlElementWrapper(name = "columns")
    @XmlElement(name = "column")
    public List<Column> getColumns() { return columns; }

    @Override
    public String toString() {
        return "QueryConfig{" +
                "name='" + name + '\'' +
                '}';
    }
}
