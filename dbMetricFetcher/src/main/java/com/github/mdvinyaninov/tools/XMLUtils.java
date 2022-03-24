package com.github.mdvinyaninov.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class XMLUtils {

    public static Object loadRemote(Class _class, String filepath) throws IOException {
        File file = new File(filepath);
        InputStream inputStream = FileUtils.openInputStream(file);
        return parseXml(_class, inputStream);
    }

    public static Object loadResource(Class _class, String filepath) throws IOException {
        ClassLoader cl = XMLUtils.class.getClassLoader();
        InputStream inputStream = cl.getResourceAsStream(filepath);
        return parseXml(_class, inputStream);
    }

    public static Object parseXml(Class _class, String xml) {
        InputStream inputStream = IOUtils.toInputStream(xml, StandardCharsets.UTF_8);
        return parseXml(_class, inputStream);
    }

    public static Object parseXml(Class _class, InputStream inputStream) {
        Object obj = null;

        if (inputStream != null) {
            try {
                JAXBContext jc = JAXBContext.newInstance(_class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                obj = unmarshaller.unmarshal(inputStream);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return obj;
    }
}
