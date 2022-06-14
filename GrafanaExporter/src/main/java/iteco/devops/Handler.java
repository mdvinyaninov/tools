package iteco.devops;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Handler implements ResponseHandler<Integer> {
    private static Logger LOGGER = LogManager.getLogger(Handler.class);
    private String path;
    private String file;
    private int result;

    public Handler(String path, String file) {
        this.path = path;
        this.file = file;
    }

    public Integer handleResponse(HttpResponse response) {
        InputStream in = null;
        OutputStream out = null;
        result = -1;

        try {
            LOGGER.debug("Response Status line: " + response.getStatusLine());
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                LOGGER.info("Success.");
                HttpEntity e = response.getEntity();
                long l = e.getContentLength();
                LOGGER.debug("Response Content length: " + l);

                if (e != null && l != 0L) {
                    in = e.getContent();
                    byte[] content = IOUtils.toByteArray(in);

                    File f = new File(path);
                    f.mkdirs();

                    String dest = path + File.separator + file;

                    f = new File(dest);
                    if (f.exists()) {
                        LOGGER.info("File exists.");
                        f.delete();
                        f.createNewFile();
                    }

                    out = new FileOutputStream(dest);
                    LOGGER.info("Writing content to file '" + dest + "'...");
                    IOUtils.write(content, out);
                    LOGGER.info("Success.");
                    result = 0;
                }
            }
            else {
                LOGGER.info("Failed to download graph.");
            }
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null)
                    in.close();

                if (out != null)
                    out.close();
            }
            catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }

        return result;
    }

    public int getResult() {
        return result;
    }
}
