package com.github.mdvinyaninov.tools.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@NoArgsConstructor
@AllArgsConstructor
public class Column {
    @Setter
    private ColumnType type;
    @Setter
    private DataType dataType;
    @Setter
    private String name;

    @XmlAttribute(name = "type")
    public ColumnType getType() {
        return type;
    }

    @XmlAttribute(name = "dataType")
    public DataType getDataType() { return dataType; }

    @XmlValue
    public String getName() {
        return name;
    }
}
