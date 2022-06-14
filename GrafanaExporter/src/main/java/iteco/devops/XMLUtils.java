package iteco.devops;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class XMLUtils {

    public static Object loadLocal(Class _class, String filename) {
        ClassLoader cl = XMLUtils.class.getClassLoader();
        InputStream inputStream = cl.getResourceAsStream(filename);
        return parseXml(_class, inputStream);
    }

    public static Object loadRemote(Class _class, String filepath) throws IOException {
        File file = new File(filepath);
        InputStream inputStream = FileUtils.openInputStream(file);
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

    public static String produceXml(Class cl, Object o, boolean fragment, boolean formatted) {
        String xml = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(cl);
            Marshaller marshaller = jc.createMarshaller();
            if (formatted)
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (fragment)
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(o, writer);
            xml = writer.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return xml;
    }

    public static String produceXml(Class cl, Object o) {
        return produceXml(cl, o, true, true);
    }
}
