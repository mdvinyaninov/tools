package com.github.mdvinyaninov.tools.config.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CDATAAdapter extends XmlAdapter<String, String> {
    private static final String CDATA_OPEN = "<![CDATA[";
    private static final String CDATA_CLOSE = "]]>";

    @Override
    public String unmarshal(String value) {
        String result;
        if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
            result = value.replace(CDATA_OPEN,"").replace(CDATA_CLOSE, "");
        }
        else result = value;
        return result;
    }

    @Override
    public String marshal(String value) {
        return CDATA_OPEN + value + CDATA_CLOSE;
    }
}
