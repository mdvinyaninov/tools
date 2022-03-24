package com.github.mdvinyaninov.tools.test;

import com.github.mdvinyaninov.tools.config.adapters.CDATAAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tests.class.getName());

    @Test
    void testCDATAAdapter() {
        final String cdata = "<![CDATA[<2342342>]]>";
        LOGGER.debug(cdata);
        final String result = new CDATAAdapter().unmarshal(cdata);
        LOGGER.debug(result);
        Assertions.assertEquals("<2342342>", result);
    }

}
