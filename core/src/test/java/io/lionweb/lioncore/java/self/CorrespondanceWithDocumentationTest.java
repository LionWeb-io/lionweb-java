package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CorrespondanceWithDocumentationTest {

    public String getStringFromUrl(URL url) throws IOException {
        return inputStreamToString(urlToInputStream(url,null));
    }

    public String inputStreamToString(InputStream inputStream) throws IOException {
        try(ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(UTF_8);
        }
    }

    private InputStream urlToInputStream(URL url, Map<String, String> args) {
        HttpURLConnection con = null;
        InputStream inputStream = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            if (args != null) {
                for (Map.Entry<String, String> e : args.entrySet()) {
                    con.setRequestProperty(e.getKey(), e.getValue());
                }
            }
            con.connect();
            int responseCode = con.getResponseCode();
            /* By default the connection will follow redirects. The following
             * block is only entered if the implementation of HttpURLConnection
             * does not perform the redirect. The exact behavior depends to
             * the actual implementation (e.g. sun.net).
             * !!! Attention: This block allows the connection to
             * switch protocols (e.g. HTTP to HTTPS), which is <b>not</b>
             * default behavior. See: https://stackoverflow.com/questions/1884230
             * for more info!!!
             */
            if (responseCode < 400 && responseCode > 299) {
                String redirectUrl = con.getHeaderField("Location");
                try {
                    URL newUrl = new URL(redirectUrl);
                    return urlToInputStream(newUrl, args);
                } catch (MalformedURLException e) {
                    URL newUrl = new URL(url.getProtocol() + "://" + url.getHost() + redirectUrl);
                    return urlToInputStream(newUrl, args);
                }
            }
            /*!!!!!*/

            inputStream = con.getInputStream();
            return inputStream;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void lioncoreIsTheSameAsInTheOrganizationRepo() throws IOException {
        JsonSerialization jsonSer = JsonSerialization.getStandardSerialization();

        //URL url = new URL("https://raw.githubusercontent.com/LIonWeb-org/organization/niko/update-docs-june2/lioncore/metametamodel/lioncore.json");
        //String content = getStringFromUrl(url);
        // List<Node> nodes = jsonSer.unserializeToNodes(content);
        File file = new File("/Users/ftomassetti/Downloads/lioncore.json");
        List<Node> nodes = jsonSer.unserializeToNodes(file);

        Language unserializedLioncore = (Language) nodes.get(0);
    }

    @Test
    public void builtInIsTheSameAsInTheOrganizationRepo() throws FileNotFoundException {
        JsonSerialization jsonSer = JsonSerialization.getStandardSerialization();

        //URL url = new URL("https://raw.githubusercontent.com/LIonWeb-org/organization/niko/update-docs-june2/lioncore/metametamodel/builtins.json");
        //String content = getStringFromUrl(url);
        // List<Node> nodes = jsonSer.unserializeToNodes(content);
        File file = new File("/Users/ftomassetti/Downloads/builtins.json");
        List<Node> nodes = jsonSer.unserializeToNodes(file);

        Language unserializedBuiltins = (Language) nodes.get(0);
    }
}
